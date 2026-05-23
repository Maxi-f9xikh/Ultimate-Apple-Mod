package de.maxi.ultimate_apple_mod.mixin;

import de.maxi.ultimate_apple_mod.ModRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fixes the camera height when the player is shrunk by CurseOfRotten.
 *
 * <p>{@code Player.getEyeHeight(Pose, EntityDimensions)} always returns 1.62 f
 * for STANDING pose regardless of the supplied dimensions, so even after
 * {@link LivingEntitySizeMixin} sets the hitbox to 0.6 f tall the camera stays
 * at the normal 1.62 f height.  This mixin intercepts the return and scales it
 * proportionally to the shrunk height.
 *
 * <p>Normal ratio: 1.62 / 1.8 = 0.9 → shrunk eye height: 0.6 × 0.9 = 0.54 f.
 */
@Mixin(Player.class)
public class PlayerEyeHeightMixin {

    @Inject(
        method = "getEyeHeight(Lnet/minecraft/world/entity/Pose;Lnet/minecraft/world/entity/EntityDimensions;)F",
        at = @At("RETURN"),
        cancellable = true
    )
    private void uam_shrunkEyeHeight(Pose pose, EntityDimensions dimensions,
                                      CallbackInfoReturnable<Float> cir) {
        Player player = (Player) (Object) this;
        try {
            if (ModRegistries.CURSE_OF_ROTTEN == null) return;
            if (player.hasEffect(ModRegistries.CURSE_OF_ROTTEN.get())) {
                // Scale eye height proportionally to the shrunk height (ratio: 1.62/1.8 = 0.9)
                cir.setReturnValue(dimensions.height * 0.9f);
            }
        } catch (NullPointerException ignored) {}
    }
}
