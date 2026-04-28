package de.maxi.ultimate_apple_mod.item;

import net.minecraft.world.item.Item;

/**
 * Empty cup — placed in the Mixer's cup slot to start mixing.
 * Crafted from 5 glass in a U-shape.
 */
public class CupItem extends Item {

    public CupItem() {
        super(new Item.Properties().stacksTo(16));
    }
}
