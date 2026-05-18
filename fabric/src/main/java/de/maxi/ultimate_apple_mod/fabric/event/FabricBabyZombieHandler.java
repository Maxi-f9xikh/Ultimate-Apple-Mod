package de.maxi.ultimate_apple_mod.fabric.event;

import de.maxi.ultimate_apple_mod.ModRegistries;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;

public class FabricBabyZombieHandler {

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof Zombie zombie) || !zombie.isBaby()) return;
            if (!entity.level().isClientSide && Math.random() < 0.1) {
                entity.level().addFreshEntity(new ItemEntity(
                    entity.level(), entity.getX(), entity.getY(), entity.getZ(),
                    new ItemStack(ModRegistries.ROTTEN_APPLE.get())));
            }
        });
    }
}
