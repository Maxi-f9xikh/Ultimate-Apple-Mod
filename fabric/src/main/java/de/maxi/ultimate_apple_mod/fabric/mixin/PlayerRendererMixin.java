package de.maxi.ultimate_apple_mod.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxi.ultimate_apple_mod.ModRegistries;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

    @Inject(method = "renderRightHand", at = @At("HEAD"))
    private void uam_scaleWhenCursedHand(PoseStack poseStack, MultiBufferSource buffer,
                                          int packedLight, AbstractClientPlayer player, CallbackInfo ci) {
        // Intentionally empty — scale is applied by render() injection below
    }

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"))
    private void uam_scalePlayer(AbstractClientPlayer player, float entityYaw, float partialTick,
                                  PoseStack poseStack, MultiBufferSource buffer,
                                  int packedLight, CallbackInfo ci) {
        try {
            if (ModRegistries.CURSE_OF_ROTTEN != null
                    && player.hasEffect(ModRegistries.CURSE_OF_ROTTEN.get())) {
                poseStack.scale(0.35f, 0.35f, 0.35f);
            }
        } catch (NullPointerException ignored) {}
    }
}
