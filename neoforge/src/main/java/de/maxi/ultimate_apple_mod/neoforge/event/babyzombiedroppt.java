package de.maxi.ultimate_apple_mod.neoforge.event;

import de.maxi.ultimate_apple_mod.neoforge.ultimate_apple_modNeoForge;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.NEOFORGE)
public class babyzombiedroppt {

    @SubscribeEvent
    public static void onZombieDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        Level level = entity.level();

        if (level != null && !level.isClientSide && entity instanceof Zombie zombie && zombie.isBaby()) {
            if (Math.random() < 0.1) {
                zombie.spawnAtLocation(new ItemStack(ultimate_apple_modNeoForge.ROTTEN_APPLE.get()));
            }
        }
    }
}
