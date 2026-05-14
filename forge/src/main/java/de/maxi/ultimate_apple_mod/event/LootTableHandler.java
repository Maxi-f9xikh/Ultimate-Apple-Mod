package de.maxi.ultimate_apple_mod.event;

import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
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
 * Adds custom apple drops to:
 *   (a) vanilla structure chest loot tables, and
 *   (b) vanilla entity loot tables (so JER displays them automatically).
 *
 * Mob drop summary
 * ─────────────────────────────────────────────────────────────────────────
 *  Baby Zombie    → Rotten Apple       10 %  (babyzombiedroppt, event only)
 *  Blaze          → Blaze Apple        25 %  ← loot table (JER visible)
 *  Enderman       → Ender Pearl Apple  25 %  ← loot table (JER visible)
 *  Skeleton       → Iron Apple          8 %  ← loot table (JER visible)
 *  Creeper        → Dirt Apple         15 %  ← loot table (JER visible)
 *  Witch          → Honey Apple        12 %  ← loot table (JER visible)
 *  Phantom        → Moon Apple         10 %  ← loot table (JER visible)
 *  Iron Golem     → Iron Apple         20 %  ← loot table (JER visible)
 *  Pillager       → Apple Bomb          5 %  ← loot table (JER visible)
 *  Evoker         → Totem Apple        50 %  (MobDropEventHandler, event only)
 *  Shulker        → Void Apple          8 %  ← loot table (JER visible)
 *  Wither (boss)  → Wither Apple      100 %  ← loot table (JER visible)
 *  Elder Guardian → Prism Apple        50 %  ← loot table (JER visible)
 *  Drowned        → Prism Apple         5 %  ← loot table (JER visible)
 *  Parrot         → Banana             30 %  ← loot table (JER visible)
 *  Zombie (all)   → Pear Apple        2.5 %  ← loot table (JER visible)
 *  Husk           → Pear Apple        2.5 %  ← loot table (JER visible)
 *  Zombie Villager→ Pear Apple        2.5 %  ← loot table (JER visible)
 *
 * Structure chest distribution overview
 * ─────────────────────────────────────────────────────────────────────────
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
 * ─────────────────────────────────────────────────────────────────────────
 */
@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LootTableHandler {

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation name = event.getName();

        // ════════════════════════════════════════════════════════════════════
        // ENTITY DROPS  (JER reads these automatically)
        // ════════════════════════════════════════════════════════════════════

        // ── Overworld hostile ────────────────────────────────────────────────

        if (name.equals(rl("entities/blaze"))) {
            // 25 %: weight 1 item + weight 3 empty = 1/4
            event.getTable().addPool(pool("uam_blaze", 1, 3,
                item(ultimate_apple_modForge.BLAZE_APPLE.get(), 1, 1, 1)));

        } else if (name.equals(rl("entities/enderman"))) {
            // 25 %
            event.getTable().addPool(pool("uam_enderman", 1, 3,
                item(ultimate_apple_modForge.ENDER_PEARL_APPLE.get(), 1, 1, 1)));

        } else if (name.equals(rl("entities/skeleton"))) {
            // ~8 %: weight 2 / total 25
            event.getTable().addPool(pool("uam_skeleton", 2, 23,
                item(ultimate_apple_modForge.IRON_APPLE.get(), 2, 1, 1)));

        } else if (name.equals(rl("entities/creeper"))) {
            // ~15 %: weight 3 / total 20
            event.getTable().addPool(pool("uam_creeper", 3, 17,
                item(ultimate_apple_modForge.DIRT_APPLE.get(), 3, 1, 1)));

        } else if (name.equals(rl("entities/witch"))) {
            // ~12 %: weight 3 / total 25
            event.getTable().addPool(pool("uam_witch", 3, 22,
                item(ultimate_apple_modForge.HONEY_APPLE.get(), 3, 1, 1)));

        } else if (name.equals(rl("entities/phantom"))) {
            // ~10 %: weight 1 / total 10
            event.getTable().addPool(pool("uam_phantom", 1, 9,
                item(ultimate_apple_modForge.MOON_APPLE.get(), 1, 1, 1)));

        } else if (name.equals(rl("entities/iron_golem"))) {
            // ~20 %: weight 1 / total 5
            event.getTable().addPool(pool("uam_iron_golem", 1, 4,
                item(ultimate_apple_modForge.IRON_APPLE.get(), 1, 1, 1)));

        // ── Raid / Illager ───────────────────────────────────────────────────

        } else if (name.equals(rl("entities/pillager"))) {
            // ~5 %: weight 1 / total 20
            event.getTable().addPool(pool("uam_pillager", 1, 19,
                item(ultimate_apple_modForge.APPLE_BOMB.get(), 1, 1, 1)));

        // ── End mobs ─────────────────────────────────────────────────────────

        } else if (name.equals(rl("entities/shulker"))) {
            // ~8 %: weight 2 / total 25
            event.getTable().addPool(pool("uam_shulker", 2, 23,
                item(ultimate_apple_modForge.VOID_APPLE.get(), 2, 1, 1)));

        // ── Boss mobs ─────────────────────────────────────────────────────────

        } else if (name.equals(rl("entities/wither"))) {
            // 100 % — Wither Apple is the whole point of fighting the Wither
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_wither")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.WITHER_APPLE.get())
                    .setWeight(1).apply(count(1, 1)))
                .build());

        // ── Ocean mobs ────────────────────────────────────────────────────────

        } else if (name.equals(rl("entities/elder_guardian"))) {
            // 50 %: weight 1 / total 2
            event.getTable().addPool(pool("uam_elder_guardian", 1, 1,
                item(ultimate_apple_modForge.PRISM_APPLE.get(), 1, 1, 1)));

        } else if (name.equals(rl("entities/drowned"))) {
            // ~5 %: weight 1 / total 20
            event.getTable().addPool(pool("uam_drowned", 1, 19,
                item(ultimate_apple_modForge.PRISM_APPLE.get(), 1, 1, 1)));

        // ── Jungle / Overworld passive ────────────────────────────────────────

        } else if (name.equals(rl("entities/parrot"))) {
            // ~30 %: weight 3 / total 10
            event.getTable().addPool(pool("uam_parrot", 3, 7,
                item(ultimate_apple_modForge.BANANA.get(), 3, 1, 1)));

        // ── Zombie variants (Pear Apple 2.5 %) ────────────────────────────────
        // Each zombie-type mob gets its own entry so all variants are covered.

        } else if (name.equals(rl("entities/zombie"))) {
            // ~2.5 %: weight 1 / total 40
            event.getTable().addPool(pool("uam_zombie_pear", 1, 39,
                item(ultimate_apple_modForge.BIRNE.get(), 1, 1, 1)));

        } else if (name.equals(rl("entities/husk"))) {
            event.getTable().addPool(pool("uam_husk_pear", 1, 39,
                item(ultimate_apple_modForge.BIRNE.get(), 1, 1, 1)));

        } else if (name.equals(rl("entities/zombie_villager"))) {
            event.getTable().addPool(pool("uam_zombie_villager_pear", 1, 39,
                item(ultimate_apple_modForge.BIRNE.get(), 1, 1, 1)));

        // ════════════════════════════════════════════════════════════════════
        // CHEST LOOT TABLES
        // ════════════════════════════════════════════════════════════════════

        // ── Overworld: dungeons / caves ──────────────────────────────────────

        } else if (name.equals(rl("chests/simple_dungeon"))) {
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

        } else if (name.equals(rl("chests/mineshaft"))) {
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

        // ── Overworld: villages ──────────────────────────────────────────────

        } else if (name.equals(rl("chests/village/village_weaponsmith"))) {
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_village_weaponsmith")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.IRON_APPLE.get())
                    .setWeight(3).apply(count(1, 2)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.COPPER_APPLE.get())
                    .setWeight(2).apply(count(1, 2)))
                .add(EmptyLootItem.emptyItem().setWeight(15))
                .build());

        } else if (name.equals(rl("chests/village/village_armorer"))) {
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_village_armorer")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.IRON_APPLE.get())
                    .setWeight(4).apply(count(1, 2)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.DIAMOND_APPLE.get())
                    .setWeight(1).apply(count(1, 1)))
                .add(EmptyLootItem.emptyItem().setWeight(15))
                .build());

        // ── Overworld: structures ────────────────────────────────────────────

        } else if (name.equals(rl("chests/desert_pyramid"))) {
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

        } else if (name.equals(rl("chests/jungle_temple"))) {
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

        } else if (name.equals(rl("chests/woodland_mansion"))) {
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

        } else if (name.equals(rl("chests/pillager_outpost"))) {
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_pillager_outpost")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.APPLE_BOMB.get())
                    .setWeight(3).apply(count(1, 3)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.IRON_APPLE.get())
                    .setWeight(2).apply(count(1, 2)))
                .add(EmptyLootItem.emptyItem().setWeight(15))
                .build());

        // ── Overworld: ocean / coastal ───────────────────────────────────────

        } else if (name.equals(rl("chests/buried_treasure"))) {
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_buried_treasure")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.DIAMOND_APPLE.get())
                    .setWeight(2).apply(count(1, 1)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.PRISM_APPLE.get())
                    .setWeight(3).apply(count(1, 2)))
                .add(EmptyLootItem.emptyItem().setWeight(5))
                .build());

        } else if (name.equals(rl("chests/shipwreck/supply"))) {
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

        // ── Overworld: stronghold ────────────────────────────────────────────

        } else if (name.equals(rl("chests/stronghold_corridor"))) {
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_stronghold_corridor")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.EMERALD_APPLE.get())
                    .setWeight(2).apply(count(1, 1)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.ENDER_PEARL_APPLE.get())
                    .setWeight(3).apply(count(1, 2)))
                .add(EmptyLootItem.emptyItem().setWeight(5))
                .build());

        } else if (name.equals(rl("chests/stronghold_library"))) {
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_stronghold_library")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.DRAGON_APPLE.get())
                    .setWeight(1).apply(count(1, 1)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.LONGEVITY_APPLE.get())
                    .setWeight(2).apply(count(1, 1)))
                .add(EmptyLootItem.emptyItem().setWeight(7))
                .build());

        // ── Deep Dark: ancient city ──────────────────────────────────────────

        } else if (name.equals(rl("chests/ancient_city"))) {
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

        // ── Ruined portal ────────────────────────────────────────────────────

        } else if (name.equals(rl("chests/ruined_portal"))) {
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_ruined_portal")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.BLAZE_APPLE.get())
                    .setWeight(4).apply(count(1, 2)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.NETHERITE_APPLE.get())
                    .setWeight(1).apply(count(1, 1)))
                .add(EmptyLootItem.emptyItem().setWeight(15))
                .build());

        // ── Nether ───────────────────────────────────────────────────────────

        } else if (name.equals(rl("chests/nether_bridge"))) {
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_nether_bridge")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.BLAZE_APPLE.get())
                    .setWeight(4).apply(count(1, 2)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.WITHER_APPLE.get())
                    .setWeight(1).apply(count(1, 1)))
                .add(EmptyLootItem.emptyItem().setWeight(5))
                .build());

        } else if (name.equals(rl("chests/bastion_treasure"))) {
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_bastion_treasure")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.NETHERITE_APPLE.get())
                    .setWeight(3).apply(count(1, 1)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.DRAGON_APPLE.get())
                    .setWeight(2).apply(count(1, 1)))
                .add(LootItem.lootTableItem(ultimate_apple_modForge.NETHER_STAR_APPLE.get())
                    .setWeight(1).apply(count(1, 1)))
                .add(EmptyLootItem.emptyItem().setWeight(4))
                .build());

        // ── End ──────────────────────────────────────────────────────────────

        } else if (name.equals(rl("chests/end_city_treasure"))) {
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

    /** Minecraft-namespace ResourceLocation for a loot-table path. */
    private static ResourceLocation rl(String path) {
        return new ResourceLocation("minecraft", path);
    }

    /** Shorthand: SetItemCountFunction with a uniform random count. */
    private static SetItemCountFunction.Builder count(int min, int max) {
        return SetItemCountFunction.setCount(UniformGenerator.between(min, max));
    }

    /**
     * Builds a simple 1-roll loot pool with one item entry and one empty entry.
     *
     * @param name        pool identifier
     * @param itemWeight  weight of the item (controls drop chance)
     * @param emptyWeight weight of the empty entry (controls miss chance)
     * @param itemEntry   the LootItem builder (pre-configured with count)
     */
    private static LootPool pool(String name, int itemWeight, int emptyWeight,
                                 LootItem.Builder<?> itemEntry) {
        return LootPool.lootPool()
            .name(name)
            .setRolls(ConstantValue.exactly(1))
            .add(itemEntry.setWeight(itemWeight))
            .add(EmptyLootItem.emptyItem().setWeight(emptyWeight))
            .build();
    }

    /**
     * Shorthand for a LootItem with a fixed count range.
     *
     * @param item   the item to drop
     * @param weight weight (ignored here — set by caller via {@link #pool})
     * @param min    minimum count
     * @param max    maximum count
     */
    private static LootItem.Builder<?> item(net.minecraft.world.item.Item item,
                                             int weight, int min, int max) {
        return LootItem.lootTableItem(item).apply(count(min, max));
    }
}
