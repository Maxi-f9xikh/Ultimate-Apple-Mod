package de.maxi.ultimate_apple_mod.fabric.mixin;

import de.maxi.ultimate_apple_mod.item.CoalAppleItem;
import de.maxi.ultimate_apple_mod.item.ShakeItem;
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
 * return the correct per-stack burn time for shake items:
 * <ul>
 *   <li>{@code isCoalFuel=true} → reads {@code coalFuelBurnTime} from NBT
 *       (doubled when mixed with Longevity Apple) or falls back to the base
 *       shake burn time.</li>
 *   <li>All other shakes → 0 (not usable as fuel).</li>
 * </ul>
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public class FurnaceFuelMixin {

    @Inject(method = "getBurnDuration", at = @At("RETURN"), cancellable = true)
    private static void uam_coalShakeFuel(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        // Only intercept shake items — everything else uses its own registration path
        if (!(stack.getItem() instanceof ShakeItem)) return;
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.getBoolean("isCoalFuel")) {
            // Read the per-stack burn time written by the Mixer (supports Coal+Longevity doubling)
            int burnTime = tag.contains("coalFuelBurnTime")
                ? tag.getInt("coalFuelBurnTime")
                : CoalAppleItem.SHAKE_BURN_TIME;
            cir.setReturnValue(burnTime);
        } else {
            // Non-coal shake: explicitly return 0 so isFuel() returns false
            cir.setReturnValue(0);
        }
    }
}
