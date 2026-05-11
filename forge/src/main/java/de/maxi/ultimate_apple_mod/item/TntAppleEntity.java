package de.maxi.ultimate_apple_mod.item;

import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class TntAppleEntity extends ThrowableItemProjectile {

    public TntAppleEntity(EntityType<? extends TntAppleEntity> type, Level level) {
        super(type, level);
    }

    public TntAppleEntity(LivingEntity thrower, Level level) {
        super(ultimate_apple_modForge.TNT_APPLE_ENTITY.get(), thrower, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ultimate_apple_modForge.TNT_APPLE.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        detonate();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        detonate();
    }

    private void detonate() {
        if (level().isClientSide()) return;
        // Power 4.0 = vanilla TNT explosion strength, breaks blocks and damages entities
        level().explode(this, getX(), getY(), getZ(), 4.0f, Level.ExplosionInteraction.TNT);
        discard();
    }
}
