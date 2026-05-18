package de.maxi.ultimate_apple_mod.event;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Platform-neutral decay constants shared by Forge and Fabric.
 */
public class DecayHelper {

    public static final String DECAY_TAG = "uam:decay_start";

    public static final long APPLE_DECAY_TICKS = 20L * 60 * 30;  // 30 min

    /** Returns the decay threshold in ticks for the given item, or 0 if it does not decay. */
    public static long getDecayThreshold(Item item) {
        if (item == Items.APPLE) return APPLE_DECAY_TICKS;
        return 0;
    }
}
