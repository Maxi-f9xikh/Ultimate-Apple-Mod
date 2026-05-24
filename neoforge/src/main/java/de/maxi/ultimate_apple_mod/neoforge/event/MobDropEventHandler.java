package de.maxi.ultimate_apple_mod.neoforge.event;

import de.maxi.ultimate_apple_mod.neoforge.ultimate_apple_modNeoForge;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.Random;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.NEOFORGE)
public class MobDropEventHandler {

    private static final Random RNG = new Random();

    private static void addDrop(LivingDropsEvent event, ItemStack stack) {
        var e = event.getEntity();
        event.getDrops().add(new ItemEntity(e.level(), e.getX(), e.getY(), e.getZ(), stack));
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        var entity = event.getEntity();

        if (entity instanceof WitherBoss) {
            if (RNG.nextDouble() < 0.5) {
                addDrop(event, new ItemStack(ultimate_apple_modNeoForge.WITHER_APPLE.get()));
                addDrop(event, new ItemStack(ultimate_apple_modNeoForge.NETHER_STAR_APPLE.get()));
            }
        } else if (entity instanceof Evoker) {
            event.getDrops().removeIf(drop -> drop.getItem().getItem() == Items.TOTEM_OF_UNDYING);
            ItemStack reward = RNG.nextDouble() < 0.3
                ? new ItemStack(ultimate_apple_modNeoForge.TOTEM_APPLE.get())
                : new ItemStack(Items.TOTEM_OF_UNDYING);
            event.getDrops().add(new ItemEntity(
                entity.level(),
                entity.getX(), entity.getY(), entity.getZ(),
                reward));
        }
    }
}
