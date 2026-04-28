package de.maxi.ultimate_apple_mod.forge.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;

public class MixerBlock extends BaseEntityBlock {

    public static final BooleanProperty HAS_JAR   = BooleanProperty.create("has_jar");
    public static final BooleanProperty HAS_SHAKE  = BooleanProperty.create("has_shake");

    // Custom hitbox slightly smaller than a full block so it matches the model
    private static final VoxelShape SHAPE = box(2, 0, 2, 14, 12, 14);

    public MixerBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(3.5f)
            .sound(SoundType.METAL)
            .noOcclusion()
            .requiresCorrectToolForDrops());
        registerDefaultState(stateDefinition.any()
            .setValue(HAS_JAR,  false)
            .setValue(HAS_SHAKE, false));
    }

    // ── Block state ────────────────────────────────────────────────────────

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_JAR, HAS_SHAKE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // ── Rendering: show the custom Blockbench model ──────────────────────

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // ── Block entity ───────────────────────────────────────────────────────

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MixerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type,
            ultimate_apple_modForge.MIXER_BLOCK_ENTITY.get(),
            MixerBlockEntity::serverTick);
    }

    // ── Interaction: open GUI ──────────────────────────────────────────────

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MixerBlockEntity mixer) {
            player.openMenu(mixer);
        }
        return InteractionResult.CONSUME;
    }

    // ── Drop inventory on block break ──────────────────────────────────────

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MixerBlockEntity mixer) {
                Containers.dropContents(level, pos, mixer);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
