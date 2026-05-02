package de.maxi.ultimate_apple_mod.effect;

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

    private static final String FROZEN_KEY = "uam:time_frozen";
    private static final double RADIUS     = 40.0;

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
                // Disable AI — this stops all movement, pathfinding, attacks, etc.
                mob.setNoAi(true);
                // Mark so we can restore it when the effect expires
                mob.getPersistentData().putBoolean(FROZEN_KEY, true);
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
     */
    @Override
    public void removeAttributeModifiers(LivingEntity caster, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(caster, attributeMap, amplifier);
        if (!caster.level().isClientSide()) {
            List<Mob> frozen = caster.level().getEntitiesOfClass(
                Mob.class,
                caster.getBoundingBox().inflate(RADIUS),
                mob -> mob.getPersistentData().contains(FROZEN_KEY)
            );
            for (Mob mob : frozen) {
                mob.setNoAi(false);
                mob.getPersistentData().remove(FROZEN_KEY);
            }
        }
    }
}
