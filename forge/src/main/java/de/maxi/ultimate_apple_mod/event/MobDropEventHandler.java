package de.maxi.ultimate_apple_mod.event;

import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
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
 *   Baby Zombie → Rotten Apple  10 %  (babyzombiedroppt.java)
 *   Evoker      → Totem of Undying OR Totem Apple  50/50
 *                 (replaces the vanilla guaranteed Totem of Undying drop)
 */
@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MobDropEventHandler {

    private static final Random RNG = new Random();

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        var entity = event.getEntity();

        // ── Evoker: 50 % Totem of Undying, 50 % Totem Apple ──────────────────
        // Remove the vanilla guaranteed Totem of Undying, then flip a coin.
        if (entity instanceof Evoker) {
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
