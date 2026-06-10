package de.maxi.ultimate_apple_mod.item;

import de.maxi.ultimate_apple_mod.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.phys.Vec3;

/**
 * Projectile thrown by {@link NuclearAppleItem}.
 * On impact (block, entity, or entering water/lava), removes every block in
 * the 16×16 column of the hit chunk from {@link Level#getMinBuildHeight()} to
 * {@link Level#getMaxBuildHeight()}.
 *
 * The wipe is spread across multiple ticks ({@value #LAYERS_PER_TICK} Y-layers
 * per tick, top-down) so a full-height chunk (~98 000 blocks) doesn't stall the
 * server for a whole tick.  The entity hovers in place while wiping and is
 * discarded when the bottom layer is reached.
 *
 * Uses block-update flag 18 (SEND_TO_CLIENT | NO_NEIGHBOR_UPDATE) during the
 * mass-removal loop to avoid cascading neighbour reactions and reduce lag.
 */
public class NuclearAppleEntity extends ThrowableItemProjectile {

    /** Flag: send update to client + skip neighbour block notifications. */
    private static final int FLAG_MASS_REMOVE = 2 | 16;

    /** Y-layers cleared per tick — 384 layers / 32 ≈ 12 ticks for a full wipe. */
    private static final int LAYERS_PER_TICK = 32;

    /** Prevents double-detonation if multiple triggers fire on the same tick. */
    private boolean hasDetonated = false;

    // Wipe progress — persisted to NBT so an in-progress wipe survives saves.
    private int wipeY;      // next Y layer to clear (counts down)
    private int wipeMinY;   // lowest layer (inclusive)
    private int originX;    // chunk-local origin (west edge)
    private int originZ;    // chunk-local origin (north edge)

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
        if (level().isClientSide()) return;

        if (hasDetonated) {
            // Hover in place while the staged wipe runs
            setDeltaMovement(Vec3.ZERO);
            wipeStep();
        } else if (isInWater() || isInLava()) {
            startDetonation();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        startDetonation();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        startDetonation();
    }

    // ── Chunk wipe (staged across ticks) ──────────────────────────────────────

    private void startDetonation() {
        Level world = level();
        if (world.isClientSide() || hasDetonated) return;
        hasDetonated = true;
        setNoGravity(true);
        setDeltaMovement(Vec3.ZERO);

        // Chunk coordinates of the impact point
        int chunkX = (int) Math.floor(getX()) >> 4;
        int chunkZ = (int) Math.floor(getZ()) >> 4;
        originX  = chunkX << 4;                    // chunk-local x = 0
        originZ  = chunkZ << 4;                    // chunk-local z = 0
        wipeMinY = world.getMinBuildHeight();      // -64 in overworld
        wipeY    = world.getMaxBuildHeight() - 1;  //  319 in overworld

        // Loud explosion sound at impact for feedback (no block damage — we clear manually)
        world.playSound(null, getX(), getY(), getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 5.0f, 0.6f);

        // Grant advancement to the thrower
        if (getOwner() instanceof ServerPlayer thrower) {
            TntAppleItem.grantAdvancement(thrower, "nuclear_apple");
        }
    }

    /** Clears the next {@value #LAYERS_PER_TICK} Y-layers, top-down. */
    private void wipeStep() {
        Level world = level();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int target = Math.max(wipeMinY, wipeY - LAYERS_PER_TICK + 1);
        for (int y = wipeY; y >= target; y--) {
            for (int x = originX; x < originX + 16; x++) {
                for (int z = originZ; z < originZ + 16; z++) {
                    pos.set(x, y, z);
                    if (!world.getBlockState(pos).isAir()) {
                        world.setBlock(pos, Blocks.AIR.defaultBlockState(), FLAG_MASS_REMOVE);
                    }
                }
            }
        }
        wipeY = target - 1;
        if (wipeY < wipeMinY) {
            discard();
        }
    }

    // ── Persistence — resume an in-progress wipe after save/load ─────────────

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("uamDetonated", hasDetonated);
        tag.putInt("uamWipeY", wipeY);
        tag.putInt("uamWipeMinY", wipeMinY);
        tag.putInt("uamOriginX", originX);
        tag.putInt("uamOriginZ", originZ);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        hasDetonated = tag.getBoolean("uamDetonated");
        wipeY        = tag.getInt("uamWipeY");
        wipeMinY     = tag.getInt("uamWipeMinY");
        originX      = tag.getInt("uamOriginX");
        originZ      = tag.getInt("uamOriginZ");
    }
}
