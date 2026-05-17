package de.maxi.ultimate_apple_mod.fabric.event;

import de.maxi.ultimate_apple_mod.ModRegistries;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Random;

public class FabricMobDropHandler {

    private static final Random RNG = new Random();

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof WitherBoss) {
                if (RNG.nextDouble() < 0.5) {
                    spawnAt(entity, new ItemStack(ModRegistries.WITHER_APPLE.get()));
                    spawnAt(entity, new ItemStack(ModRegistries.NETHER_STAR_APPLE.get()));
                }
            } else if (entity instanceof Evoker) {
                ItemStack reward = RNG.nextDouble() < 0.5
                    ? new ItemStack(Items.TOTEM_OF_UNDYING)
                    : new ItemStack(ModRegistries.TOTEM_APPLE.get());
                spawnAt(entity, reward);
            }
        });
    }

    private static void spawnAt(net.minecraft.world.entity.LivingEntity entity, ItemStack stack) {
        entity.level().addFreshEntity(new ItemEntity(
            entity.level(), entity.getX(), entity.getY(), entity.getZ(), stack));
    }
}
