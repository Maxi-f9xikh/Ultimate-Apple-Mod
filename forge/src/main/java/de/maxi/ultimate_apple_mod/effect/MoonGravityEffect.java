package de.maxi.ultimate_apple_mod.effect;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Moon Gravity — reduces gravitational acceleration to ~20 % while the entity is airborne.
 *
 * Normal gravity: −0.08 blocks/tick per tick.
 * Moon Gravity:   counteracts +0.064/tick → effective gravity ≈ −0.016/tick (20 % normal).
 *
 * Critically, the counteraction is applied on BOTH the rising AND falling phase.
 * This creates symmetric lunar physics: the jump arc is perfectly mirrored —
 * the entity rises just as slowly as it falls. Combined with Jump Boost I, this
 * produces a noticeably higher jump with a smooth, floaty feel on both sides.
 *
 * Physics:  vanilla jump v₀ ≈ 0.42 + JB-I bonus → effective gravity 0.016/tick
 *           → peak height ≈ 8 blocks, total hang time ≈ 50+ ticks.
 *
 * Motion is synced every tick to prevent the client's own gravity simulation
 * from fighting the server correction, which would cause a snapping/stuttering feel.
 */
public class MoonGravityEffect extends MobEffect {

    public MoonGravityEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xC8C8FF); // pale blue-white
    }

    /** Fire every tick so the gravity counteraction is smooth. */
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return;
        if (entity.onGround()) return;  // no effect while standing

        Vec3 v = entity.getDeltaMovement();
        // Counteract 80 % of vanilla gravity every tick.
        // Applied to both rising (v.y > 0) AND falling (v.y < 0) for a symmetric arc —
        // the entity decelerates going up at the same rate it accelerates going down.
        entity.setDeltaMovement(v.x, v.y + 0.064, v.z);

        // Sync every tick so the client never diverges and causes snapping.
        if (entity instanceof ServerPlayer sp) {
            sp.connection.send(new ClientboundSetEntityMotionPacket(
                sp.getId(), entity.getDeltaMovement()));
        }
    }
}
