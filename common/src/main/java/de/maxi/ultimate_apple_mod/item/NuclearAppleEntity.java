package de.maxi.ultimate_apple_mod.item;

import de.maxi.ultimate_apple_mod.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Projectile thrown by {@link NuclearAppleItem}.
 * On impact (block, entity, or entering water/lava), removes every block in
 * the 16×16 column of the hit chunk from {@link Level#getMinBuildHeight()} to
 * {@link Level#getMaxBuildHeight()}.
 *
 * Uses block-update flag 18 (SEND_TO_CLIENT | NO_NEIGHBOR_UPDATE) during the
 * mass-removal loop to avoid cascading neighbour reactions and reduce lag.
 */
public class NuclearAppleEntity extends ThrowableItemProjectile {

    /** Flag: send update to client + skip neighbour block notifications. */
    private static final int FLAG_MASS_REMOVE = 2 | 16;

    /** Prevents double-detonation if multiple triggers fire on the same tick. */
    private boolean hasDetonated = false;

    public NuclearAppleEntity(EntityType<? extends NuclearAppleEntity> type, Level level) {
        super(type, level);
    }

    public NuclearAppleEntity(LivingEntity thrower, Level level) {
        super(ModRegistries.NUCLEAR_APPLE_ENTITY.get(), thrower, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModRegistries.NUCLEAR_APPLE.get();
    }

    /** Trigger on contact with water or lava (in addition to normal impact). */
    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && !hasDetonated && (isInWater() || isInLava())) {
            wipeChunk();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        wipeChunk();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        wipeChunk();
    }

    // ── Chunk wipe ────────────────────────────────────────────────────────────

    private void wipeChunk() {
        Level world = level();
        if (world.isClientSide() || hasDetonated) return;
        hasDetonated = true;

        // Chunk coordinates of the impact point
        int chunkX  = (int) Math.floor(getX()) >> 4;
        int chunkZ  = (int) Math.floor(getZ()) >> 4;
        int originX = chunkX << 4;         // chunk-local x = 0
        int originZ = chunkZ << 4;         // chunk-local z = 0
        int minY    = world.getMinBuildHeight();   // -64 in overworld
        int maxY    = world.getMaxBuildHeight();   //  320 in overworld

        // Sweep top-down so falling blocks don't cascade
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = maxY - 1; y >= minY; y--) {
            for (int x = originX; x < originX + 16; x++) {
                for (int z = originZ; z < originZ + 16; z++) {
                    pos.set(x, y, z);
                    if (!world.getBlockState(pos).isAir()) {
                        world.setBlock(pos, Blocks.AIR.defaultBlockState(), FLAG_MASS_REMOVE);
                    }
                }
            }
        }

        // Loud explosion sound at impact for feedback (no block damage — we already cleared)
        world.playSound(null, getX(), getY(), getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 5.0f, 0.6f);

        discard();
    }
}
