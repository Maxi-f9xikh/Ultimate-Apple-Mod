package de.maxi.ultimate_apple_mod.event;

import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Adds custom apple drops to vanilla structure chest loot tables.
 *
 * Each structure gets one extra pool (1 roll) whose entries are
 * weighted so that the total weight across all items + empty
 * controls rarity. A high empty weight means the apple rarely
 * appears; a low one means it shows up often.
 *
 * Distribution overview
 * ─────────────────────────────────────────────────────────────────
 *  Simple Dungeon      Iron Apple, Rotten Apple, Baked Apple  (30 %)
 *  Mineshaft           Copper Apple, Roasted Apple, Iron Apple (30 %)
 *  Village Weaponsmith Iron Apple, Copper Apple               (25 %)
 *  Village Armorer     Iron Apple, Diamond Apple              (25 %)
 *  Desert Pyramid      Honey Apple, Redstone Apple, Baked Ap. (35 %)
 *  Jungle Temple       Orchard Apple, Ender Pearl Apple,
 *                      Echo Apple                             (30 %)
 *  Woodland Mansion    Totem Apple, Diamond Apple, Moon Apple (30 %)
 *  Pillager Outpost    Apple Bomb, Iron Apple                 (25 %)
 *  Buried Treasure     Diamond Apple, Prism Apple             (50 %)
 *  Shipwreck Supply    Prism Apple, Banana, Baked Apple       (50 %)
 *  Stronghold Corridor Emerald Apple, Ender Pearl Apple       (40 %)
 *  Stronghold Library  Dragon Apple, Longevity Apple          (30 %)
 *  Ancient City        Echo Apple, Void Apple, Time Freeze Ap.(30 %)
 *  Ruined Portal       Blaze Apple, Netherite Apple (rare)    (25 %)
 *  Nether Fortress     Blaze Apple, Wither Apple              (50 %)
 *  Bastion Treasure    Netherite Apple, Dragon Apple,
 *                      Nether Star Apple (rare)               (50 %)
 *  End City            Void Apple, Dragon Apple, Quantum Apple (60 %)
 * ─────────────────────────────────────────────────────────────────
 */
@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LootTableHandler {

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation name = event.getName();

        // ── Overworld: dungeons / caves ──────────────────────────────────────

        if (name.equals(rl("chests/simple_dungeon"))) {
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_dungeon")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.IRON_APPLE.get())
                    .setWeight(3).apply(count(1, 2)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.ROTTEN_APPLE.get())
                    .setWeight(2).apply(count(1, 2)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.BAKED_APPLE.get())
                    .setWeight(2).apply(count(1, 2)))
                .add(EmptyLootItem.emptyItem().setWeight(13))
                .build());
        }

        else if (name.equals(rl("chests/mineshaft"))) {
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_mineshaft")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.COPPER_APPLE.get())
                    .setWeight(3).apply(count(1, 2)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.ROASTED_APPLE.get())
                    .setWeight(2).apply(count(1, 2)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.IRON_APPLE.get())
                    .setWeight(2).apply(count(1, 1)))
                .add(EmptyLootItem.emptyItem().setWeight(13))
                .build());
        }

        // ── Overworld: villages ──────────────────────────────────────────────

        else if (name.equals(rl("chests/village/village_weaponsmith"))) {
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_village_weaponsmith")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.IRON_APPLE.get())
                    .setWeight(3).apply(count(1, 2)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.COPPER_APPLE.get())
                    .setWeight(2).apply(count(1, 2)))
                .add(EmptyLootItem.emptyItem().setWeight(15))
                .build());
        }

        else if (name.equals(rl("chests/village/village_armorer"))) {
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_village_armorer")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.IRON_APPLE.get())
                    .setWeight(4).apply(count(1, 2)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.DIAMOND_APPLE.get())
                    .setWeight(1).apply(count(1, 1)))
                .add(EmptyLootItem.emptyItem().setWeight(15))
                .build());
        }

        // ── Overworld: structures ────────────────────────────────────────────

        else if (name.equals(rl("chests/desert_pyramid"))) {
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_desert_pyramid")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.HONEY_APPLE.get())
                    .setWeight(3).apply(count(1, 2)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.REDSTONE_APPLE.get())
                    .setWeight(2).apply(count(1, 1)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.BAKED_APPLE.get())
                    .setWeight(2).apply(count(1, 2)))
                .add(EmptyLootItem.emptyItem().setWeight(13))
                .build());
        }

        else if (name.equals(rl("chests/jungle_temple"))) {
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_jungle_temple")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.ORCHARD_APPLE.get())
                    .setWeight(3).apply(count(1, 2)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.ENDER_PEARL_APPLE.get())
                    .setWeight(2).apply(count(1, 1)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.ECHO_APPLE.get())
                    .setWeight(2).apply(count(1, 1)))
                .add(EmptyLootItem.emptyItem().setWeight(13))
                .build());
        }

        else if (name.equals(rl("chests/woodland_mansion"))) {
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_woodland_mansion")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.TOTEM_APPLE.get())
                    .setWeight(2).apply(count(1, 1)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.DIAMOND_APPLE.get())
                    .setWeight(2).apply(count(1, 1)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.MOON_APPLE.get())
                    .setWeight(3).apply(count(1, 1)))
                .add(EmptyLootItem.emptyItem().setWeight(13))
                .build());
        }

        else if (name.equals(rl("chests/pillager_outpost"))) {
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_pillager_outpost")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.APPLE_BOMB.get())
                    .setWeight(3).apply(count(1, 3)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.IRON_APPLE.get())
                    .setWeight(2).apply(count(1, 2)))
                .add(EmptyLootItem.emptyItem().setWeight(15))
                .build());
        }

        // ── Overworld: ocean / coastal ───────────────────────────────────────

        else if (name.equals(rl("chests/buried_treasure"))) {
            // Buried treasure is rare to find — reward generously
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_buried_treasure")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.DIAMOND_APPLE.get())
                    .setWeight(2).apply(count(1, 1)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.PRISM_APPLE.get())
                    .setWeight(3).apply(count(1, 2)))
                .add(EmptyLootItem.emptyItem().setWeight(5))
                .build());
        }

        else if (name.equals(rl("chests/shipwreck/supply"))) {
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_shipwreck_supply")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.PRISM_APPLE.get())
                    .setWeight(4).apply(count(1, 2)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.BANANA.get())
                    .setWeight(3).apply(count(1, 3)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.BAKED_APPLE.get())
                    .setWeight(3).apply(count(1, 2)))
                .add(EmptyLootItem.emptyItem().setWeight(10))
                .build());
        }

        // ── Overworld: stronghold ────────────────────────────────────────────

        else if (name.equals(rl("chests/stronghold_corridor"))) {
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_stronghold_corridor")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.EMERALD_APPLE.get())
                    .setWeight(2).apply(count(1, 1)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.ENDER_PEARL_APPLE.get())
                    .setWeight(3).apply(count(1, 2)))
                .add(EmptyLootItem.emptyItem().setWeight(5))
                .build());
        }

        else if (name.equals(rl("chests/stronghold_library"))) {
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_stronghold_library")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.DRAGON_APPLE.get())
                    .setWeight(1).apply(count(1, 1)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.LONGEVITY_APPLE.get())
                    .setWeight(2).apply(count(1, 1)))
                .add(EmptyLootItem.emptyItem().setWeight(7))
                .build());
        }

        // ── Deep Dark: ancient city ──────────────────────────────────────────

        else if (name.equals(rl("chests/ancient_city"))) {
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_ancient_city")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.ECHO_APPLE.get())
                    .setWeight(3).apply(count(1, 2)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.VOID_APPLE.get())
                    .setWeight(2).apply(count(1, 1)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.TIME_FREEZE_APPLE.get())
                    .setWeight(2).apply(count(1, 1)))
                .add(EmptyLootItem.emptyItem().setWeight(13))
                .build());
        }

        // ── Ruined portal ────────────────────────────────────────────────────

        else if (name.equals(rl("chests/ruined_portal"))) {
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_ruined_portal")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.BLAZE_APPLE.get())
                    .setWeight(4).apply(count(1, 2)))
                // Netherite Apple is deliberately rare even here
                .add(LootItem.lootTableItem(ultimate_apple_modForge.NETHERITE_APPLE.get())
                    .setWeight(1).apply(count(1, 1)))
                .add(EmptyLootItem.emptyItem().setWeight(15))
                .build());
        }

        // ── Nether ───────────────────────────────────────────────────────────

        else if (name.equals(rl("chests/nether_bridge"))) {
            // Nether Fortress — fire-themed, decent loot
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_nether_bridge")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.BLAZE_APPLE.get())
                    .setWeight(4).apply(count(1, 2)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.WITHER_APPLE.get())
                    .setWeight(1).apply(count(1, 1)))
                .add(EmptyLootItem.emptyItem().setWeight(5))
                .build());
        }

        else if (name.equals(rl("chests/bastion_treasure"))) {
            // Bastion Treasure — only the richest piglin loot; very high-tier
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_bastion_treasure")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.NETHERITE_APPLE.get())
                    .setWeight(3).apply(count(1, 1)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.DRAGON_APPLE.get())
                    .setWeight(2).apply(count(1, 1)))
                // Nether Star Apple: one of the hardest-to-get items in the mod
                .add(LootItem.lootTableItem(ultimate_apple_modForge.NETHER_STAR_APPLE.get())
                    .setWeight(1).apply(count(1, 1)))
                .add(EmptyLootItem.emptyItem().setWeight(4))
                .build());
        }

        // ── End ──────────────────────────────────────────────────────────────

        else if (name.equals(rl("chests/end_city_treasure"))) {
            // End City treasure — top-tier loot, appears often
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_end_city")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.VOID_APPLE.get())
                    .setWeight(3).apply(count(1, 2)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.DRAGON_APPLE.get())
                    .setWeight(2).apply(count(1, 1)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.QUANTUM_APPLE.get())
                    .setWeight(2).apply(count(1, 1)))
                .add(EmptyLootItem.emptyItem().setWeight(3))
                .build());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Returns a Minecraft-namespace ResourceLocation for a loot-table path. */
    private static ResourceLocation rl(String path) {
        return new ResourceLocation("minecraft", path);
    }

    /** Shorthand for a SetItemCountFunction that sets a uniform random count. */
    private static SetItemCountFunction.Builder count(int min, int max) {
        return SetItemCountFunction.setCount(UniformGenerator.between(min, max));
    }
}
