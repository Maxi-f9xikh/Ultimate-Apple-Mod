package de.maxi.ultimate_apple_mod.effect;

import de.maxi.ultimate_apple_mod.FrozenMobCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Time Freeze — the caster moves at 3× speed while the world around them grinds to a halt.
 *
 * Freeze mechanism (runs every 5 ticks inside the 40-block radius):
 *   • Mobs:    AI is disabled via setNoAi(true), velocity zeroed, tagged with
 *              {@code "uam:time_frozen"} in their persistent data so we can restore them later.
 *   • Players: Cannot have their AI removed, so extreme Slowness (127) + velocity zeroed
 *              is used instead — they are effectively unable to move.
 *
 * On effect removal ({@link #removeAttributeModifiers}):
 *   All mobs within 40 blocks that carry the {@code "uam:time_frozen"} tag have their
 *   AI restored and the tag cleared.
 *
 * Unloaded chunks: entities there are not ticked server-side and cannot be frozen —
 * accepted limitation.
 */
public class TimeFreezeEffect extends MobEffect {

    private static final double RADIUS = 40.0;

    public TimeFreezeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x00CCFF); // electric blue

        // Player runs at 3× normal speed
        this.addAttributeModifier(
            Attributes.MOVEMENT_SPEED,
            ResourceLocation.fromNamespaceAndPath("ultimate_apple_mod", "time_freeze_speed"),
            2.0D,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        // Player attacks 2.5× faster
        this.addAttributeModifier(
            Attributes.ATTACK_SPEED,
            ResourceLocation.fromNamespaceAndPath("ultimate_apple_mod", "time_freeze_attack_speed"),
            1.5D,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    /**
     * Run every tick — not every 5.
     * Squids re-apply their own swimming velocity each tick in aiStep(), so freezing
     * only every 5 ticks lets them visibly jitter. Running every tick keeps everything
     * (including water mobs) properly locked in place.
     */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity caster, int amplifier) {
        if (caster.level().isClientSide()) return true;

        List<LivingEntity> targets = caster.level().getEntitiesOfClass(
            LivingEntity.class,
            caster.getBoundingBox().inflate(RADIUS),
            e -> e != caster
        );

        for (LivingEntity target : targets) {
            // Zero the velocity to stop momentum
            Vec3 v = target.getDeltaMovement();
            if (v.lengthSqr() > 0.0001) {
                target.setDeltaMovement(Vec3.ZERO);
            }

            if (target instanceof Mob mob) {
                // Disable AI once — this stops all movement, pathfinding, attacks, etc.
                // Skip mobs that are already frozen so we don't redo the map writes
                // for every mob in range on every single tick.
                if (!FrozenMobCache.isFrozen(mob.getUUID())) {
                    mob.setNoAi(true);
                    // Persisted marker — lets the entity-load handlers restore AI
                    // even after a server restart or chunk unload orphaned the cache
                    mob.addTag(FrozenMobCache.PERSIST_TAG);
                    // Track per-caster so the cleanup handler can restore AI on expiry
                    FrozenMobCache.freeze(caster.getUUID(), mob.getUUID());
                }
            } else if (target instanceof Player) {
                // Players cannot have their AI removed; use max Slowness instead
                target.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 10, 127,
                    false, false, false));
            }
        }
        return true;
    }

    /**
     * Restore AI to all mobs we froze when the effect expires naturally or is removed.
     * Note: In 1.21.1 this method no longer has access to the entity, so we cannot unfreeze
     * nearby mobs here — they remain frozen until the chunk is reloaded.
     */
    @Override
    public void removeAttributeModifiers(AttributeMap attributeMap) {
        super.removeAttributeModifiers(attributeMap);
    }
}
