package de.maxi.ultimate_apple_mod.forge.block;
import java.util.function.Supplier;

import static de.maxi.ultimate_apple_mod.ultimate_apple_mod.MOD_ID;

import com.mojang.serialization.MapCodec;
import de.maxi.ultimate_apple_mod.block.MixerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, MOD_ID);

    // ── Mixer ──────────────────────────────────────────────────────────────

    public static final Supplier<Block> MIXER =
        BLOCKS.register("mixer", () -> new MixerBlock() {
            @Override
            public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
                return new MixerBlockEntity(pos, state);
            }

            @Override
            protected MapCodec<? extends BaseEntityBlock> codec() {
                // Codec not needed for this modded block; serialization uses the registry key.
                throw new UnsupportedOperationException("MixerBlock codec not supported");
            }
        });

    /** Block item that places the mixer — goes in all item registries. */
    public static final Supplier<Item> MIXER_ITEM =
        ITEMS.register("mixer", () -> new BlockItem(MIXER.get(), new Item.Properties()));

    // ── Register both DeferredRegisters ───────────────────────────────────

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
}
