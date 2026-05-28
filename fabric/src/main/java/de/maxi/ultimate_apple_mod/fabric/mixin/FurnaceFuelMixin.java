package de.maxi.ultimate_apple_mod.fabric.mixin;

import de.maxi.ultimate_apple_mod.item.CoalAppleItem;
import de.maxi.ultimate_apple_mod.item.ShakeItem;
import de.maxi.ultimate_apple_mod.util.NbtCompat;
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
 * per-stack NBT check), so we intercept both {@code isFuel} and
 * {@code getBurnDuration} directly:
 * <ul>
 *   <li>{@code isFuel} — lets the coal-shake pass the slot-acceptance check.
 *       Fabric's {@code FuelRegistryImpl} overrides {@code isFuel} and checks
 *       its registry <em>without</em> delegating to {@code getBurnDuration},
 *       so hooking only {@code getBurnDuration} is insufficient.</li>
 *   <li>{@code getBurnDuration} — returns the correct per-stack burn time once
 *       the item is in the fuel slot.</li>
 * </ul>
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public class FurnaceFuelMixin {

    /**
     * Let coal-infused shakes pass the fuel-slot acceptance check.
     * Fires after Fabric's FuelRegistry has already had its say; if the item
     * is not already accepted we check our NBT tag.
     */
    @Inject(method = "isFuel", at = @At("RETURN"), cancellable = true)
    private static void uam_isCoalShakeFuel(ItemStack stack,
                                             CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return; // already accepted — leave it alone
        if (!(stack.getItem() instanceof ShakeItem)) return;
        CompoundTag tag = NbtCompat.getTag(stack);
        if (tag != null && tag.getBoolean("isCoalFuel")) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Return the correct per-stack burn time for coal-infused shakes.
     * Reads {@code coalFuelBurnTime} from NBT (written by the Mixer;
     * doubled when mixed with Longevity Apple).
     */
    @Inject(method = "getBurnDuration", at = @At("RETURN"), cancellable = true)
    private static void uam_coalShakeFuel(ItemStack stack,
                                          CallbackInfoReturnable<Integer> cir) {
        // Only intercept shake items — everything else uses its own registration path
        if (!(stack.getItem() instanceof ShakeItem)) return;
        CompoundTag tag = NbtCompat.getTag(stack);
        if (tag != null && tag.getBoolean("isCoalFuel")) {
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
