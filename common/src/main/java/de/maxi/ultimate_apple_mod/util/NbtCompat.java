package de.maxi.ultimate_apple_mod.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

/**
 * Compatibility helpers for accessing legacy NBT-style data on ItemStacks
 * via the new DataComponents system (MC 1.20.5+).
 */
public final class NbtCompat {
    private NbtCompat() {}

    /** Returns a copy of the stack's custom NBT data, or null if none is set. */
    @Nullable
    public static CompoundTag getTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || data.isEmpty()) return null;
        return data.copyTag();
    }

    /** Returns true if the stack has any custom NBT data. */
    public static boolean hasTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty();
    }

    /** Stores the given compound as the stack's custom NBT data. */
    public static void setTag(ItemStack stack, CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    /** Returns the existing tag or a new empty one (without storing it). */
    public static CompoundTag getOrCreateTag(ItemStack stack) {
        CompoundTag existing = getTag(stack);
        return existing != null ? existing : new CompoundTag();
    }
}
