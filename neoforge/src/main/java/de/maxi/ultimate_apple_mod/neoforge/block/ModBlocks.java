package de.maxi.ultimate_apple_mod.neoforge.block;

import static de.maxi.ultimate_apple_mod.ultimate_apple_mod.MOD_ID;

import de.maxi.ultimate_apple_mod.block.MixerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final RegistryObject<Block> MIXER =
        BLOCKS.register("mixer", () -> new MixerBlock() {
            @Override
            public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
                return new MixerBlockEntity(pos, state);
            }
        });

    public static final RegistryObject<Item> MIXER_ITEM =
        ITEMS.register("mixer", () -> new BlockItem(MIXER.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
}
