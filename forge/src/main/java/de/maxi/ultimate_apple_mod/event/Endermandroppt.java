package de.maxi.ultimate_apple_mod.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID)
public class Endermandroppt {

    @SubscribeEvent
    public static void onEndermanDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        Level level = entity.level();

        if (!level.isClientSide && entity instanceof EnderMan EnderMan) {
            if (Math.random() < 0.25) { // 25% Dropchance
                EnderMan.spawnAtLocation(new ItemStack(ultimate_apple_modForge.ENDER_PEARL_APPLE.get()));
            }
        }
    }
}
