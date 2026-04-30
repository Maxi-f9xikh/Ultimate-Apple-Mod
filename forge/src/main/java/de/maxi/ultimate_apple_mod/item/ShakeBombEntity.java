package de.maxi.ultimate_apple_mod.item;

import de.maxi.ultimate_apple_mod.event.RewindTracker;
import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import de.maxi.ultimate_apple_mod.item.OrchardCallerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * A throwable shake produced by mixing any apple with the Apple Bomb.
 *
 * The shake's NBT (effects, dragonCharges, special flags) is stored in the
 * carried ItemStack via ThrowableItemProjectile's built-in item persistence.
 *
 * On direct entity hit → effects applied to that entity only.
 * On block hit       → effects applied to all living entities within 4 blocks.
 *
 * Special flags behave as follows when applied to a target:
 *  - voidLaunch   : launches the hit entity upward (any LivingEntity)
 *  - orchardSpawn : plants up to 6 trees at the impact location (ServerLevel)
 *  - rewindEffect : teleports the hit player back 5 s in position history
 *  - enderTeleport: teleports the hit player in their current look direction
 *  - witherCurse  : applies Wither II to the hit entity
 *  - lifesteal    : grants Lifesteal to the THROWER (the entity that owns this)
 *  - dragonCharges: gives charges to the THROWER
 */
public class ShakeBombEntity extends ThrowableItemProjectile {

    public ShakeBombEntity(EntityType<? extends ShakeBombEntity> type, Level level) {
        super(type, level);
    }

    /** Called from ShakeItem.use() when throwing a bomb shake. */
    public ShakeBombEntity(LivingEntity thrower, Level level, ItemStack shakeStack) {
        super(ultimate_apple_modForge.SHAKE_BOMB_ENTITY.get(), thrower, level);
        setItem(shakeStack);
    }

    @Override
    protected Item getDefaultItem() {
        return ultimate_apple_modForge.SHAKE_ITEM.get();
    }

    // ── Impact handlers ──────────────────────────────────────────────────────

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide()) return;
        Entity hit = result.getEntity();
        if (hit instanceof LivingEntity living) {
            applyToTarget(living, getShakeTag(), getX(), getY(), getZ());
        }
        explode();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (level().isClientSide()) return;
        // AOE splash — apply to every living entity within 4 blocks
        CompoundTag tag = getShakeTag();
        double cx = getX(), cy = getY(), cz = getZ();
        level().getEntitiesOfClass(LivingEntity.class, new AABB(cx, cy, cz, cx, cy, cz).inflate(4.0))
            .forEach(e -> applyToTarget(e, tag, cx, cy, cz));
        explode();
    }

    // ── Effect application ───────────────────────────────────────────────────

    /**
     * Applies all effects stored in the shake tag to the given target.
     * Special-flag effects (voidLaunch, rewindEffect, …) are handled here too.
     */
    private void applyToTarget(LivingEntity target, @Nullable CompoundTag tag,
                                double cx, double cy, double cz) {
        if (tag == null || !(level() instanceof ServerLevel serverLevel)) return;

        // ── clearsEffects ────────────────────────────────────────────────────
        if (tag.getBoolean("clearsEffects")) {
            target.removeAllEffects();
            return; // nothing else
        }

        // ── Regular mob effects ──────────────────────────────────────────────
        ListTag effectsList = tag.getList("effects", Tag.TAG_COMPOUND);
        for (int i = 0; i < effectsList.size(); i++) {
            CompoundTag et = effectsList.getCompound(i);
            ResourceLocation id = ResourceLocation.tryParse(et.getString("id"));
            if (id == null) continue;
            MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(id);
            if (effect != null) {
                target.addEffect(new MobEffectInstance(
                    effect, et.getInt("duration"), et.getInt("amplifier")));
            }
        }

        // ── Wither curse ─────────────────────────────────────────────────────
        if (tag.getBoolean("witherCurse")) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 30, 1));
        }

        // ── Void launch — launch the hit entity upward ───────────────────────
        if (tag.getBoolean("voidLaunch")) {
            Vec3 motion = target.getDeltaMovement();
            boolean falling = motion.y < -0.05;
            if (falling) {
                target.setDeltaMovement(motion.x * 0.2, 6.5, motion.z * 0.2);
            } else {
                target.setDeltaMovement(motion.x, 2.5, motion.z);
            }
            target.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 15, 0));
            if (target instanceof ServerPlayer sp) {
                sp.connection.send(new ClientboundSetEntityMotionPacket(
                    sp.getId(), target.getDeltaMovement()));
            }
        }

        // ── Orchard spawn — plant trees at impact location ───────────────────
        if (tag.getBoolean("orchardSpawn")) {
            BlockPos impactPos = new BlockPos((int) cx, (int) cy, (int) cz);
            OrchardCallerItem.plantTrees(serverLevel, impactPos, serverLevel.getRandom(), 6);
        }

        // ── Player-only effects ──────────────────────────────────────────────
        if (target instanceof ServerPlayer player) {

            // Rewind: teleport back 5 seconds
            if (tag.getBoolean("rewindEffect")) {
                Vec3 oldPos = RewindTracker.getPositionFiveSecondsAgo(player);
                if (oldPos != null) {
                    player.teleportTo(oldPos.x, oldPos.y, oldPos.z);
                    player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(
                            "message.ultimate_apple_mod.rewind"), true);
                }
            }

            // Ender teleport: teleport in look direction
            if (tag.getBoolean("enderTeleport")) {
                performEnderTeleport(player);
            }

            // Dragon charges go to the THROWER, not the hit player
        }

        // ── Thrower-only rewards ─────────────────────────────────────────────
        Entity owner = getOwner();
        if (owner instanceof ServerPlayer thrower) {
            int dragonCharges = tag.getInt("dragonCharges");
            if (dragonCharges > 0) {
                int existing = thrower.getPersistentData().getInt("dragonBreathCharges");
                thrower.getPersistentData().putInt(
                    "dragonBreathCharges", existing + dragonCharges);
            }
            if (tag.getBoolean("lifesteal")) {
                thrower.addEffect(new MobEffectInstance(
                    ultimate_apple_modForge.LIFESTEAL_EFFECT.get(), 20 * 60, 0));
            }
        }
    }

    // ── Ender teleport helper ────────────────────────────────────────────────
    // Mirrors EnderPearlAppleItem — teleports the player up to 256 blocks
    // in their current look direction to the nearest safe standing spot.

    public static void performEnderTeleport(ServerPlayer player) {
        Level level = player.level();
        Vec3 start = player.getEyePosition();
        Vec3 look  = player.getLookAngle();
        Vec3 end   = start.add(look.scale(256));

        BlockHitResult hit = level.clip(new ClipContext(
            start, end,
            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos   = hit.getBlockPos();
            BlockPos teleportPos = findSafe(level, hitPos.relative(hit.getDirection()), hit.getDirection());
            if (teleportPos != null) {
                player.teleportTo(
                    teleportPos.getX() + 0.5,
                    teleportPos.getY(),
                    teleportPos.getZ() + 0.5);
                player.hurt(level.damageSources().fall(), 5.0f);
                level.playSound(null, player.blockPosition(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }
    }

    private static @Nullable BlockPos findSafe(Level level, BlockPos start,
                                                net.minecraft.core.Direction dir) {
        if (isSafe(level, start)) return start;
        for (int y = -3; y <= 3; y++)
            for (int x = -1; x <= 1; x++)
                for (int z = -1; z <= 1; z++) {
                    BlockPos p = start.offset(x, y, z);
                    if (isSafe(level, p)) return p;
                }
        return null;
    }

    private static boolean isSafe(Level level, BlockPos pos) {
        return level.isEmptyBlock(pos)
            && level.isEmptyBlock(pos.above())
            && !level.isEmptyBlock(pos.below());
    }

    // ── Impact VFX ───────────────────────────────────────────────────────────

    private void explode() {
        if (!(level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.SPLASH,
            getX(), getY(), getZ(), 40, 0.5, 0.5, 0.5, 0.15);
        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER,
            getX(), getY(), getZ(), 12, 0.4, 0.4, 0.4, 0.08);
        level().playSound(null, getX(), getY(), getZ(),
            SoundEvents.SPLASH_POTION_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
        discard();
    }

    // ── NBT helpers ──────────────────────────────────────────────────────────

    @Nullable
    private CompoundTag getShakeTag() {
        ItemStack stack = getItem();
        return stack.hasTag() ? stack.getTag() : null;
    }
}
