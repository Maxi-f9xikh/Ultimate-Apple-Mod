package de.maxi.ultimate_apple_mod.effect;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Moon Gravity — symmetric low-gravity physics while the entity is airborne.
 *
 * Vertical axis
 * ─────────────
 * Normal Minecraft gravity: −0.08 blocks/tick, drag 0.98 → net −0.0784/tick.
 * Moon counteraction: +0.064/tick applied BEFORE physics each tick.
 * Effective net gravity per tick ≈ (v − 0.016) × 0.98, terminal velocity ≈ −0.8 m/tick.
 * Applied symmetrically on both the rising and falling phase → perfectly mirrored arc.
 * With normal jump (v₀ = 0.42, no Jump Boost): peak ≈ 4–5 blocks, air time ≈ 35 ticks.
 *
 * Horizontal axis
 * ───────────────
 * Vanilla air drag removes 9 % of horizontal speed per tick (×0.91).
 * Over a 35-tick moon jump this decays sprint speed to near zero — the player
 * can barely move forward. We compensate by mostly cancelling the horizontal drag
 * so that the player retains ~96 % of horizontal speed per tick while airborne.
 * The net factor applied here is (0.96 / 0.91) ≈ 1.055; physics then multiplies
 * by 0.91 again → effective net horizontal drag 0.96 per tick (vs 0.91 vanilla).
 *
 * Motion is synced every tick so the client never diverges and causes snapping.
 */
public class MoonGravityEffect extends MobEffect {

    // 0.064 counteracts 80 % of vanilla gravity → net gravity ~20 % of normal
    private static final double GRAVITY_COUNTERACTION = 0.064;

    // Mostly cancels the 0.91 vanilla air drag → net horizontal retention = 0.96/tick
    private static final double H_FACTOR = 0.96 / 0.91; // ≈ 1.055

    public MoonGravityEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xC8C8FF); // pale blue-white
    }

    /** Fire every tick for a smooth, continuous counteraction. */
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return;
        if (entity.onGround()) return;

        Vec3 v = entity.getDeltaMovement();
        entity.setDeltaMovement(
            v.x * H_FACTOR,                  // preserve horizontal momentum
            v.y + GRAVITY_COUNTERACTION,      // symmetric arc — same for rise & fall
            v.z * H_FACTOR
        );

        // Sync every tick so the client's own gravity simulation stays in lockstep.
        if (entity instanceof ServerPlayer sp) {
            sp.connection.send(new ClientboundSetEntityMotionPacket(
                sp.getId(), entity.getDeltaMovement()));
        }
    }
}
