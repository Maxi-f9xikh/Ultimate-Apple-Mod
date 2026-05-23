package de.maxi.ultimate_apple_mod.mixin;

import de.maxi.ultimate_apple_mod.ModRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntitySizeMixin {

    /** Inherited from Entity — Mixin resolves parent-class private fields via @Shadow. */
    @Shadow protected float eyeHeight;

    /**
     * Shrinks the player's hitbox to 0.25 × 0.6 while CurseOfRotten is active.
     */
    @Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
    private void uam_shrinkWhenCursed(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if (!((Object) this instanceof Player player)) return;
        try {
            if (ModRegistries.CURSE_OF_ROTTEN == null) return;
            if (player.hasEffect(ModRegistries.CURSE_OF_ROTTEN.get())) {
                cir.setReturnValue(EntityDimensions.scalable(0.25f, 0.6f));
            }
        } catch (NullPointerException ignored) {}
    }

    /**
     * Fixes the camera height after {@code refreshDimensions()} runs.
     *
     * <p>Problem: {@code Player.getEyeHeight(Pose, EntityDimensions)} ignores the
     * supplied dimensions and always returns 1.62 f for STANDING pose, so even
     * after the hitbox is shrunk to 0.6 f the camera stays at 1.62 f.
     *
     * <p>Solution: after {@code refreshDimensions()} has finished (which calls
     * {@code getDimensions} and then {@code getEyeHeight} and stores the result
     * in {@code eyeHeight}), we override the stored value for shrunk players to
     * {@code 0.54 f} (= 0.6 × 0.9, matching the normal 1.62/1.8 eye-height ratio).
     */
    @Inject(method = "refreshDimensions", at = @At("TAIL"))
    private void uam_fixEyeHeight(CallbackInfo ci) {
        if (!((Object) this instanceof Player player)) return;
        try {
            if (ModRegistries.CURSE_OF_ROTTEN == null) return;
            if (player.hasEffect(ModRegistries.CURSE_OF_ROTTEN.get())) {
                this.eyeHeight = 0.54f; // 0.6 f height × 0.9 (normal ratio 1.62/1.8)
            }
        } catch (NullPointerException ignored) {}
    }
}
