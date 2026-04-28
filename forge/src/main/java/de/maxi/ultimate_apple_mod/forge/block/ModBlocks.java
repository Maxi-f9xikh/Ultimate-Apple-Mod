package de.maxi.ultimate_apple_mod.forge.block;

import static de.maxi.ultimate_apple_mod.ultimate_apple_mod.MOD_ID;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    // ── Mixer ──────────────────────────────────────────────────────────────

    public static final RegistryObject<Block> MIXER =
        BLOCKS.register("mixer", MixerBlock::new);

    /** Block item that places the mixer — goes in all item registries. */
    public static final RegistryObject<Item> MIXER_ITEM =
        ITEMS.register("mixer", () -> new BlockItem(MIXER.get(), new Item.Properties()));

    // ── Register both DeferredRegisters ───────────────────────────────────

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
}
