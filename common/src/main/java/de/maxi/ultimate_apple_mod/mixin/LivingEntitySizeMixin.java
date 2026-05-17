package de.maxi.ultimate_apple_mod.mixin;

import de.maxi.ultimate_apple_mod.ModRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntitySizeMixin {

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
}
