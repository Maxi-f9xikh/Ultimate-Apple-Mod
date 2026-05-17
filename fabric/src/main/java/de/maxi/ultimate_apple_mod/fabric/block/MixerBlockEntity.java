package de.maxi.ultimate_apple_mod.fabric.block;

import de.maxi.ultimate_apple_mod.block.MixerBlockEntityBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fabric subclass of MixerBlockEntityBase.
 * Implements WorldlyContainer (Fabric's sided inventory equivalent)
 * so hoppers and other automation can interact with the mixer.
 * No slot restrictions — any item can enter or exit any slot from any side.
 */
public class MixerBlockEntity extends MixerBlockEntityBase implements WorldlyContainer {

    private static final int[] ALL_SLOTS = {SLOT_CUP, SLOT_ING1, SLOT_ING2, SLOT_OUTPUT};

    public MixerBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return ALL_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return true;
    }
}
