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
 * Shrinks the player's bounding box to 0.25 × 0.6 when Curse of Rotten is active,
 * allowing the player to pass through a 1-block-tall gap.
 *
 * <p>In MC 1.21.1 {@code Player.getDimensions(Pose)} was renamed to
 * {@code getDefaultDimensions(Pose)}, and eye-height is now baked into the
 * returned {@link EntityDimensions} record via {@code withEyeHeight()}.
 *
 * <p><b>Why Player.class (not LivingEntity):</b>
 * {@code Player} overrides {@code getDefaultDimensions(Pose)} with a hardcoded POSES
 * map that never calls {@code super.getDefaultDimensions()}.  A mixin on
 * {@code LivingEntity} therefore never fires for player entities.  Targeting
 * {@code Player} directly ensures the injection fires for every pose look-up,
 * including the {@code canEnterPose()} checks inside {@code updatePlayerPose()}.
 */
@Mixin(Player.class)
public class LivingEntitySizeMixin {

    private static final float ROTTEN_SCALE = 0.35f;

    @Inject(
        method = "getDefaultDimensions(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/entity/EntityDimensions;",
        at = @At("RETURN"),
        cancellable = true
    )
    private void uam_shrinkWhenCursed(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        try {
            if (ModRegistries.CURSE_OF_ROTTEN == null) return;
            if (((Player) (Object) this).hasEffect(ModRegistries.CURSE_OF_ROTTEN.get())) {
                cir.setReturnValue(
                    EntityDimensions.scalable(0.25f, 0.6f)
                        .withEyeHeight(1.62f * ROTTEN_SCALE));
            }
        } catch (NullPointerException ignored) {}
    }
}
