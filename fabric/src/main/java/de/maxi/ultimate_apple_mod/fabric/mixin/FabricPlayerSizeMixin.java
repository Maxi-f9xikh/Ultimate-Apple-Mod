package de.maxi.ultimate_apple_mod.fabric.mixin;

import de.maxi.ultimate_apple_mod.ModRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lowers the first-person camera (eyeHeight) to match the shrunken player
 * model when Curse of Rotten is active.
 *
 * <p>Without this mixin, {@code Player.getStandingEyeHeight(Pose, EntityDimensions)}
 * always returns the hardcoded standing value 1.62 f regardless of the entity
 * dimensions that {@code LivingEntitySizeMixin} reports (0.25 × 0.6).
 * {@code LivingEntity.refreshDimensions()} stores the result of this method
 * in the {@code eyeHeight} field, so the camera stays near the ceiling until
 * this override returns the correct scaled value.
 *
 * <p>The scale factor 0.35 f matches the visual render scale in
 * {@link PlayerRendererMixin} and the Forge {@code EntityEvent.Size} handler,
 * giving {@code 1.62 × 0.35 ≈ 0.567 f}.
 *
 * <p>This mixin targets {@code Player} (not {@code LivingEntity}) because
 * {@code Player} overrides {@code getEyeHeight} with hardcoded pose values
 * that ignore the {@code EntityDimensions} parameter entirely.
 */
@Mixin(Player.class)
public class FabricPlayerSizeMixin {

    private static final float ROTTEN_SCALE = 0.35f;

    @Inject(
        method = "getStandingEyeHeight(Lnet/minecraft/world/entity/Pose;Lnet/minecraft/world/entity/EntityDimensions;)F",
        at = @At("RETURN"),
        cancellable = true
    )
    private void uam_rottenEyeHeight(Pose pose, EntityDimensions dims,
                                      CallbackInfoReturnable<Float> cir) {
        try {
            if (ModRegistries.CURSE_OF_ROTTEN != null
                    && ((Player) (Object) this).hasEffect(ModRegistries.CURSE_OF_ROTTEN.get())) {
                cir.setReturnValue(1.62f * ROTTEN_SCALE);
            }
        } catch (NullPointerException ignored) {}
    }
}
