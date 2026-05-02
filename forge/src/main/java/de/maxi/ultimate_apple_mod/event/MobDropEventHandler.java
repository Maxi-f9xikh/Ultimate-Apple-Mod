package de.maxi.ultimate_apple_mod.event;

import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

/**
 * Adds custom apple drops to vanilla mobs.
 *
 * Already handled elsewhere:
 *   Baby Zombie   → Rotten Apple  (babyzombiedroppt.java,  10 %)
 *   Blaze         → Blaze Apple   (Blazedropptapfel.java,  25 %)
 *   Enderman      → Ender Pearl Apple (Endermandroppt.java, 25 %)
 *   Elder Guardian / Drowned → Prism Apple (this file, see below)
 *
 * New drops added here:
 *   Adult Zombie   →  Rotten Apple       5 %
 *   Skeleton       →  Iron Apple         8 %
 *   Creeper        →  Dirt Apple        15 %
 *   Witch          →  Honey Apple       12 %
 *   Phantom        →  Moon Apple        10 %
 *   Iron Golem     →  Iron Apple        20 %
 *   Pillager       →  Apple Bomb         5 %
 *   Evoker         →  Totem Apple       15 %
 *   Shulker        →  Void Apple         8 %
 *   Wither (boss)  →  Wither Apple     100 %
 *   Elder Guardian →  Prism Apple       15 %  (kept from original)
 *   Drowned        →  Prism Apple        5 %  (kept from original)
 */
@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MobDropEventHandler {

    private static final Random RNG = new Random();

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        var entity = event.getEntity();

        // ── Overworld mobs ────────────────────────────────────────────────────

        if (entity instanceof Zombie zombie && !zombie.isBaby()) {
            // Baby zombies are handled in babyzombiedroppt.java (10 %)
            roll(event, ultimate_apple_modForge.ROTTEN_APPLE.get(), 0.05);
        }
        else if (entity instanceof Skeleton) {
            roll(event, ultimate_apple_modForge.IRON_APPLE.get(), 0.08);
        }
        else if (entity instanceof Creeper) {
            roll(event, ultimate_apple_modForge.DIRT_APPLE.get(), 0.15);
        }
        else if (entity instanceof Witch) {
            roll(event, ultimate_apple_modForge.HONEY_APPLE.get(), 0.12);
        }
        else if (entity instanceof Phantom) {
            roll(event, ultimate_apple_modForge.MOON_APPLE.get(), 0.10);
        }
        else if (entity instanceof IronGolem) {
            roll(event, ultimate_apple_modForge.IRON_APPLE.get(), 0.20);
        }

        // ── Pillager / Raid mobs ──────────────────────────────────────────────

        else if (entity instanceof Pillager) {
            roll(event, ultimate_apple_modForge.APPLE_BOMB.get(), 0.05);
        }
        else if (entity instanceof Evoker) {
            roll(event, ultimate_apple_modForge.TOTEM_APPLE.get(), 0.15);
        }

        // ── End mobs ──────────────────────────────────────────────────────────

        else if (entity instanceof Shulker) {
            roll(event, ultimate_apple_modForge.VOID_APPLE.get(), 0.08);
        }

        // ── Boss mobs ─────────────────────────────────────────────────────────

        else if (entity instanceof WitherBoss) {
            // Guaranteed drop — the Wither Apple is the whole point of fighting it
            addDrop(event, new ItemStack(ultimate_apple_modForge.WITHER_APPLE.get()));
        }

        // ── Ocean mobs (kept from original) ───────────────────────────────────

        else if (entity instanceof ElderGuardian) {
            roll(event, ultimate_apple_modForge.PRISM_APPLE.get(), 0.15);
        }
        else if (entity instanceof Drowned) {
            roll(event, ultimate_apple_modForge.PRISM_APPLE.get(), 0.05);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void roll(LivingDropsEvent event, Item item, double chance) {
        if (RNG.nextDouble() < chance) {
            addDrop(event, new ItemStack(item));
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
