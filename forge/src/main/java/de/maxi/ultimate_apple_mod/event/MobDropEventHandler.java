package de.maxi.ultimate_apple_mod.event;

import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

/**
 * Adds custom loot drops to specific mobs.
 *
 * Prism Apple:
 *  - Drowned: 5 % chance per death (loot-independent)
 *  - Elder Guardian: 15 % chance per death
 */
@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MobDropEventHandler {

    private static final Random RNG = new Random();

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        double roll = RNG.nextDouble();

        if (event.getEntity() instanceof ElderGuardian) {
            if (roll < 0.15) {  // 15 %
                addDrop(event, new ItemStack(ultimate_apple_modForge.PRISM_APPLE.get()));
            }
        } else if (event.getEntity() instanceof Drowned) {
            if (roll < 0.05) {  // 5 %
                addDrop(event, new ItemStack(ultimate_apple_modForge.PRISM_APPLE.get()));
            }
        }
    }

    private static void addDrop(LivingDropsEvent event, ItemStack stack) {
        var entity = event.getEntity();
        event.getDrops().add(new ItemEntity(
            entity.level(),
            entity.getX(), entity.getY(), entity.getZ(),
            stack));
    }
}
