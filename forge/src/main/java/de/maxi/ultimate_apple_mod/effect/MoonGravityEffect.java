package de.maxi.ultimate_apple_mod.effect;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Moon Gravity — reduces downward acceleration by ~80 % while the player is airborne.
 *
 * Normal gravity: −0.08 blocks/tick per tick.
 * Moon Gravity:   counteracts +0.064/tick → effective gravity ≈ −0.016/tick (20 % normal).
 *
 * The result: jumps reach much greater heights and falling feels slow & floaty,
 * like the real lunar surface — but NOT the feather-falling of Slow_Falling.
 * Combined with Jump Boost II in the Moon Apple food properties, it creates an
 * authentic moon-jump feel.
 *
 * Motion is synced to the client every tick to prevent the client from simulating
 * full gravity between corrections, which would cause stuttery snapping.
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

        Vec3 v = entity.getDeltaMovement();
        // Only counteract gravity while the entity is airborne and falling
        if (!entity.onGround() && v.y < 0) {
            // +0.064 counteracts 80 % of vanilla gravity (0.08/tick)
            double newY = Math.min(v.y + 0.064, 0.0); // never push upward
            entity.setDeltaMovement(v.x, newY, v.z);

            // Sync every tick — sending every 2 ticks lets the client simulate
            // full gravity in between, causing a harsh snapping feel.
            if (entity instanceof ServerPlayer sp) {
                sp.connection.send(new ClientboundSetEntityMotionPacket(
                    sp.getId(), sp.getDeltaMovement()));
            }
        }
    }
}
