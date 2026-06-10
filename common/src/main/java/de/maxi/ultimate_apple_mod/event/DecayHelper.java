package de.maxi.ultimate_apple_mod.event;

import de.maxi.ultimate_apple_mod.ModRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Platform-neutral decay constants and replacement chain shared by Forge and Fabric.
 *
 * Decay chain (each stage gets a fresh timer when it appears in the inventory):
 *   Enchanted Golden Apple → Golden Apple  after 60 min
 *   Golden Apple           → Apple         after 45 min
 *   Apple                  → Rotten Apple  after 30 min
 */
public class DecayHelper {

    public static final String DECAY_TAG = "uam:decay_start";

    public static final long APPLE_DECAY_TICKS           = 20L * 60 * 30;  // 30 min
    public static final long GOLDEN_APPLE_DECAY_TICKS    = 20L * 60 * 45;  // 45 min
    public static final long ENCHANTED_APPLE_DECAY_TICKS = 20L * 60 * 60;  // 60 min

    /** Returns the decay threshold in ticks for the given item, or 0 if it does not decay. */
    public static long getDecayThreshold(Item item) {
        if (item == Items.APPLE)                  return APPLE_DECAY_TICKS;
        if (item == Items.GOLDEN_APPLE)           return GOLDEN_APPLE_DECAY_TICKS;
        if (item == Items.ENCHANTED_GOLDEN_APPLE) return ENCHANTED_APPLE_DECAY_TICKS;
        return 0;
    }

    /**
     * Returns what the given stack decays INTO (next stage down the chain),
     * or {@link ItemStack#EMPTY} if the item does not decay.
     * The replacement carries no decay tag, so its own timer starts fresh.
     */
    public static ItemStack getDecayReplacement(ItemStack original) {
        int count = original.getCount();
        Item item = original.getItem();
        if (item == Items.APPLE)                  return new ItemStack(ModRegistries.ROTTEN_APPLE.get(), count);
        if (item == Items.GOLDEN_APPLE)           return new ItemStack(Items.APPLE, count);
        if (item == Items.ENCHANTED_GOLDEN_APPLE) return new ItemStack(Items.GOLDEN_APPLE, count);
        return ItemStack.EMPTY;
    }
}
