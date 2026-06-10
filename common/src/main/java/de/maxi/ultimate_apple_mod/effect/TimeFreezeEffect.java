package de.maxi.ultimate_apple_mod.effect;

import de.maxi.ultimate_apple_mod.FrozenMobCache;
import net.minecraft.server.MinecraftServer;
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
 * Freeze mechanism (runs every tick inside the 40-block radius):
 *   • Mobs:    AI is disabled via setNoAi(true), velocity zeroed, tracked per caster
 *              in {@link FrozenMobCache} so we can restore them later.
 *   • Players: Cannot have their AI removed, so extreme Slowness (127) + velocity zeroed
 *              is used instead — they are effectively unable to move.
 *
 * On effect removal ({@link #removeAttributeModifiers}):
 *   {@link FrozenMobCache#releaseAll} restores AI for every mob this caster froze,
 *   looked up by UUID across all dimensions — works no matter how far the caster
 *   travelled while the effect was active.
 *
 * Player disconnect with the effect still active is handled by the platform
 * logout handlers (Forge PlayerLoggedOutEvent / Fabric DISCONNECT), which also
 * call releaseAll().
 */
public class TimeFreezeEffect extends MobEffect {

    private static final double RADIUS = 40.0;

    public TimeFreezeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x00CCFF); // electric blue

        // Player runs at 3× normal speed  (MULTIPLY_TOTAL +2.0 → base × 3.0)
        this.addAttributeModifier(
            Attributes.MOVEMENT_SPEED,
            "F9E6F56B-C834-4E6C-9A80-E453F49A3D3A",
            2.0D,
            AttributeModifier.Operation.MULTIPLY_TOTAL
        );
        // Player attacks 2.5× faster (MULTIPLY_TOTAL +1.5 → base × 2.5)
        this.addAttributeModifier(
            Attributes.ATTACK_SPEED,
            "B2D6B491-E5A7-4B92-A31E-C9F79B7FAD4A",
            1.5D,
            AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    /**
     * Run every tick — not every 5.
     * Squids re-apply their own swimming velocity each tick in aiStep(), so freezing
     * only every 5 ticks lets them visibly jitter. Running every tick keeps everything
     * (including water mobs) properly locked in place.
     */
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity caster, int amplifier) {
        if (caster.level().isClientSide()) return;

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
                    // Track per-caster so cleanup can restore AI on expiry
                    FrozenMobCache.freeze(caster.getUUID(), mob.getUUID());
                }
            } else if (target instanceof Player) {
                // Players cannot have their AI removed; use max Slowness instead
                target.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 10, 127,
                    false, false, false));
            }
        }
    }

    /**
     * Restore AI to all mobs we froze when the effect expires naturally or is removed.
     * releaseAll() finds the mobs by UUID across all dimensions, so this also works
     * when the caster ran far away (3× speed!) or switched dimensions meanwhile.
     */
    @Override
    public void removeAttributeModifiers(LivingEntity caster, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(caster, attributeMap, amplifier);
        if (!caster.level().isClientSide()) {
            MinecraftServer server = caster.level().getServer();
            if (server != null) {
                FrozenMobCache.releaseAll(server, caster.getUUID());
            }
        }
    }
}
