package de.maxi.ultimate_apple_mod.fabric.block;

import de.maxi.ultimate_apple_mod.block.MixerBlock;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ModBlocks {

    public static Block MIXER;
    public static Item  MIXER_ITEM;

    public static void register() {
        MIXER = Registry.register(BuiltInRegistries.BLOCK,
            new ResourceLocation(ultimate_apple_mod.MOD_ID, "mixer"),
            new MixerBlock() {
                @Override
                public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
                    return new de.maxi.ultimate_apple_mod.fabric.block.MixerBlockEntity(pos, state);
                }
            });
        MIXER_ITEM = Registry.register(BuiltInRegistries.ITEM,
            new ResourceLocation(ultimate_apple_mod.MOD_ID, "mixer"),
            new BlockItem(MIXER, new Item.Properties()));
    }
}
