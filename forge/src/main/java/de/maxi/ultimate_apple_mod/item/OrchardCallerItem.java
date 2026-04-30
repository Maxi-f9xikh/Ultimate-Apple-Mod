package de.maxi.ultimate_apple_mod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class OrchardCallerItem extends Item {

    public OrchardCallerItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(level instanceof ServerLevel serverLevel) || !(livingEntity instanceof Player player)) {
            return super.finishUsingItem(stack, level, livingEntity);
        }

        BlockPos playerPos = player.blockPosition();

        boolean hasSolidGround = false;
        outer:
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                if (serverLevel.getBlockState(playerPos.offset(dx, -1, dz)).isSolid()) {
                    hasSolidGround = true;
                    break outer;
                }
            }
        }

        if (!hasSolidGround) {
            player.displayClientMessage(
                Component.translatable("message.ultimate_apple_mod.nothing_can_grow"), true);
            return stack;
        }

        plantTrees(serverLevel, playerPos, serverLevel.getRandom(), 4);
        return super.finishUsingItem(stack, level, livingEntity);
    }

    /**
     * Plants up to {@code maxTrees} oak trees around {@code center}.
     * Returns the number of trees actually planted.
     * Called both from eating the Orchard Apple directly and from shakes/bombs.
     */
    public static int plantTrees(ServerLevel serverLevel, BlockPos center,
                                  RandomSource rng, int maxTrees) {
        var featureOpt = serverLevel.registryAccess()
            .registry(Registries.CONFIGURED_FEATURE)
            .flatMap(reg -> reg.getHolder(TreeFeatures.OAK));
        if (featureOpt.isEmpty()) return 0;
        var treeFeature = featureOpt.get().value();

        int treesPlanted = 0;
        int attempts     = 0;
        int maxAttempts  = maxTrees * 15;
        while (treesPlanted < maxTrees && attempts < maxAttempts) {
            attempts++;
            int dx = rng.nextIntBetweenInclusive(-6, 6);
            int dz = rng.nextIntBetweenInclusive(-6, 6);
            if (dx == 0 && dz == 0) continue;

            BlockPos base = center.offset(dx, 0, dz);
            for (int dy = 2; dy >= -3; dy--) {
                BlockPos groundPos = base.offset(0, dy - 1, 0);
                BlockPos treePos   = base.offset(0, dy,     0);

                if (!serverLevel.getBlockState(groundPos).isSolid()) continue;

                // Oak trees need at least 7 blocks of free vertical space
                boolean enoughSpace = true;
                for (int h = 0; h < 7; h++) {
                    if (!serverLevel.isEmptyBlock(treePos.above(h))) {
                        enoughSpace = false;
                        break;
                    }
                }
                if (!enoughSpace) continue;

                // TreeFeature requires dirt or grass directly below the trunk
                BlockState ground = serverLevel.getBlockState(groundPos);
                if (!ground.is(BlockTags.DIRT)) {
                    serverLevel.setBlock(groundPos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                }

                // Place the tree directly via ConfiguredFeature — no sapling, guaranteed growth
                boolean grew = treeFeature.place(
                    serverLevel,
                    serverLevel.getChunkSource().getGenerator(),
                    rng,
                    treePos
                );
                if (grew) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        treePos.getX() + 0.5, treePos.getY() + 1.5, treePos.getZ() + 0.5,
                        20, 1.5, 1.5, 1.5, 0.1);
                    treesPlanted++;
                    break;
                }
            }
        }
        return treesPlanted;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("§aEating this apple calls an orchard!")
            .withStyle(ChatFormatting.GREEN));
        components.add(Component.literal("§7Spawns up to 4 oak trees around you.")
            .withStyle(ChatFormatting.GRAY));
    }
}
