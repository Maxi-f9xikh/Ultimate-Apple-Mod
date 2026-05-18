package de.maxi.ultimate_apple_mod.forge.event;

import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

/**
 * Handles mob drops that require runtime event logic and cannot be expressed
 * as static loot-table entries.
 *
 * All other custom apple drops are registered via {@link LootTableHandler}
 * using {@code LootTableLoadEvent} so that JER can display them automatically.
 *
 * Remaining event-only drops:
 *   Baby Zombie → Rotten Apple  10 %  (babyzombiedroppt.java - kept as backup)
 *   Evoker      → Totem of Undying OR Totem Apple  50/50
 *                 (replaces the vanilla guaranteed Totem of Undying drop)
 *   Wither      → Wither Apple + Nether Star Apple  50 % (both together or neither)
 *                 (vanilla always drops 1 Nether Star regardless)
 */
@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MobDropEventHandler {

    private static final Random RNG = new Random();

    /** Spawns an ItemEntity for {@code stack} at the dying entity's position. */
    private static void addDrop(LivingDropsEvent event, ItemStack stack) {
        var e = event.getEntity();
        event.getDrops().add(new ItemEntity(e.level(), e.getX(), e.getY(), e.getZ(), stack));
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        var entity = event.getEntity();

        // ── Wither: 50 % chance to also drop Wither Apple + Nether Star Apple ──
        // Vanilla always drops 1 Nether Star. We add a 50 % bonus pack on top.
        if (entity instanceof WitherBoss) {
            if (RNG.nextDouble() < 0.5) {
                addDrop(event, new ItemStack(ultimate_apple_modForge.WITHER_APPLE.get()));
                addDrop(event, new ItemStack(ultimate_apple_modForge.NETHER_STAR_APPLE.get()));
            }
        }

        // ── Evoker: 50 % Totem of Undying, 50 % Totem Apple ──────────────────
        // Remove the vanilla guaranteed Totem of Undying, then flip a coin.
        else if (entity instanceof Evoker) {
            event.getDrops().removeIf(drop -> drop.getItem().getItem() == Items.TOTEM_OF_UNDYING);
            ItemStack reward = RNG.nextDouble() < 0.5
                ? new ItemStack(Items.TOTEM_OF_UNDYING)
                : new ItemStack(ultimate_apple_modForge.TOTEM_APPLE.get());
            event.getDrops().add(new ItemEntity(
                entity.level(),
                entity.getX(), entity.getY(), entity.getZ(),
                reward));
        }
    }
}
