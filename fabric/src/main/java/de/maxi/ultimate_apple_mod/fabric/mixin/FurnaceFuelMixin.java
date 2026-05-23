package de.maxi.ultimate_apple_mod.fabric.mixin;

import de.maxi.ultimate_apple_mod.item.CoalAppleItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Allows coal-infused shakes (NBT tag {@code isCoalFuel=true}) to be used as
 * furnace fuel on Fabric, where Forge's {@code IForgeItem.getBurnTime()} hook
 * is not available.
 *
 * <p>Fabric's {@code FuelRegistry} only supports item-level registration (no
 * per-stack NBT check), so we intercept {@code getBurnDuration} directly and
 * return the coal-shake burn time when the tag is set.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public class FurnaceFuelMixin {

    @Inject(method = "getBurnDuration", at = @At("RETURN"), cancellable = true)
    private static void uam_coalShakeFuel(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        // Only act if no burn time is already registered for this item
        if (cir.getReturnValue() > 0) return;
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.getBoolean("isCoalFuel")) {
            cir.setReturnValue(CoalAppleItem.SHAKE_BURN_TIME);
        }
    }
}
