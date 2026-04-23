package de.maxi.ultimate_apple_mod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;

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

        int treesPlanted = 0;
        int attempts = 0;
        while (treesPlanted < 4 && attempts < 30) {
            attempts++;
            int dx = serverLevel.getRandom().nextIntBetweenInclusive(-5, 5);
            int dz = serverLevel.getRandom().nextIntBetweenInclusive(-5, 5);
            if (dx == 0 && dz == 0) continue;

            BlockPos base = playerPos.offset(dx, 0, dz);
            for (int dy = 2; dy >= -3; dy--) {
                BlockPos groundPos = base.offset(0, dy - 1, 0);
                BlockPos saplingPos = base.offset(0, dy, 0);

                if (!serverLevel.getBlockState(groundPos).isSolid()) continue;
                if (!serverLevel.isEmptyBlock(saplingPos)) continue;
                if (!serverLevel.isEmptyBlock(saplingPos.above())) continue;
                if (!serverLevel.isEmptyBlock(saplingPos.above(2))) continue;

                var groundState = serverLevel.getBlockState(groundPos);
                if (!groundState.is(BlockTags.DIRT)) {
                    // non-dirt solid ground is converted to grass so the sapling can grow — intentional design
                    serverLevel.setBlock(groundPos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                }

                serverLevel.setBlock(saplingPos, Blocks.OAK_SAPLING.defaultBlockState(), 3);
                var saplingState = serverLevel.getBlockState(saplingPos);
                if (saplingState.getBlock() instanceof SaplingBlock saplingBlock) {
                    for (int attempt = 0; attempt < 10; attempt++) {
                        saplingBlock.performBonemeal(serverLevel, serverLevel.getRandom(), saplingPos, saplingState);
                        if (!(serverLevel.getBlockState(saplingPos).getBlock() instanceof SaplingBlock)) break;
                    }
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        saplingPos.getX() + 0.5, saplingPos.getY() + 0.5, saplingPos.getZ() + 0.5,
                        5, 0.5, 0.5, 0.5, 0.1);
                    treesPlanted++;
                    break;
                }
            }
        }

        return super.finishUsingItem(stack, level, livingEntity);
    }
}
