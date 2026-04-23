package de.maxi.ultimate_apple_mod.item;

import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class AppleBombEntity extends ThrowableItemProjectile {

    public AppleBombEntity(EntityType<? extends AppleBombEntity> type, Level level) {
        super(type, level);
    }

    public AppleBombEntity(LivingEntity thrower, Level level) {
        super(ultimate_apple_modForge.APPLE_BOMB_ENTITY.get(), thrower, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ultimate_apple_modForge.APPLE_BOMB.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        applyExplosion();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        applyExplosion();
    }

    private void applyExplosion() {
        if (level().isClientSide()) return;
        ServerLevel serverLevel = (ServerLevel) level();

        serverLevel.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(4.0))
            .forEach(entity -> {
                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0));
            });

        serverLevel.sendParticles(ParticleTypes.CRIT,
            getX(), getY(), getZ(), 20, 0.5, 0.5, 0.5, 0.3);
        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
            getX(), getY(), getZ(), 15, 0.5, 0.5, 0.5, 0.1);
        level().playSound(null, getX(), getY(), getZ(),
            SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.5f, 2.0f);

        discard();
    }
}
