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
 * Shrinks the player's hitbox and camera when Curse of Rotten is active.
 *
 * <p>In MC 1.21.1 the eye-height is baked into {@link EntityDimensions} via
 * {@link EntityDimensions#eyeHeight()}.  We override
 * {@code getDefaultDimensions(Pose)} (the single source-of-truth for both
 * hitbox and camera in the new system) to return scaled dimensions when the
 * effect is active.
 */
@Mixin(Player.class)
public class FabricPlayerSizeMixin {

    private static final float ROTTEN_SCALE = 0.35f;

    @Inject(
        method = "getDefaultDimensions(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/entity/EntityDimensions;",
        at = @At("RETURN"),
        cancellable = true
    )
    private void uam_rottenDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        try {
            if (ModRegistries.CURSE_OF_ROTTEN != null
                    && ((Player) (Object) this).hasEffect(ModRegistries.CURSE_OF_ROTTEN.get())) {
                cir.setReturnValue(
                    EntityDimensions.scalable(0.25f, 0.6f)
                        .withEyeHeight(1.62f * ROTTEN_SCALE));
            }
        } catch (NullPointerException ignored) {}
    }
}
