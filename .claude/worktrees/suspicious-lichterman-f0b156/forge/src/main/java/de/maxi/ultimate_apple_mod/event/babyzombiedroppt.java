package de.maxi.ultimate_apple_mod.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID)
public class babyzombiedroppt {

    @SubscribeEvent
    public static void onZombieDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        Level level = entity.level();

        if (level != null && !level.isClientSide && entity instanceof Zombie zombie && zombie.isBaby()) {
            if (Math.random() < 0.1) { // 10% Dropchance
                zombie.spawnAtLocation(new ItemStack(ultimate_apple_modForge.ROTTEN_APPLE.get()));
            }
        }
    }
}
