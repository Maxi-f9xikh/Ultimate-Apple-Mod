package de.maxi.ultimate_apple_mod.fabric.event;

import de.maxi.ultimate_apple_mod.ModRegistries;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class FabricLootTableHandler {

    public static void register() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {

            // ── Entity drops ───────────────────────────────────────────────────
            if (id.equals(rl("entities/blaze")))
                tableBuilder.pool(pool("uam_blaze", 1, 3, item(ModRegistries.BLAZE_APPLE.get(), 1, 1)));
            else if (id.equals(rl("entities/enderman")))
                tableBuilder.pool(pool("uam_enderman", 1, 3, item(ModRegistries.ENDER_PEARL_APPLE.get(), 1, 1)));
            else if (id.equals(rl("entities/skeleton")))
                tableBuilder.pool(pool("uam_skeleton", 2, 23, item(ModRegistries.IRON_APPLE.get(), 1, 1)));
            else if (id.equals(rl("entities/creeper")))
                tableBuilder.pool(pool("uam_creeper", 3, 17, item(ModRegistries.DIRT_APPLE.get(), 1, 1)));
            else if (id.equals(rl("entities/witch")))
                tableBuilder.pool(pool("uam_witch", 3, 22, item(ModRegistries.HONEY_APPLE.get(), 1, 1)));
            else if (id.equals(rl("entities/phantom")))
                tableBuilder.pool(pool("uam_phantom", 1, 9, item(ModRegistries.MOON_APPLE.get(), 1, 1)));
            else if (id.equals(rl("entities/iron_golem")))
                tableBuilder.pool(pool("uam_iron_golem", 1, 4, item(ModRegistries.IRON_APPLE.get(), 1, 1)));
            else if (id.equals(rl("entities/pillager")))
                tableBuilder.pool(pool("uam_pillager", 1, 19, item(ModRegistries.APPLE_BOMB.get(), 1, 1)));
            else if (id.equals(rl("entities/shulker")))
                tableBuilder.pool(pool("uam_shulker", 2, 23, item(ModRegistries.VOID_APPLE.get(), 1, 1)));
            else if (id.equals(rl("entities/wither_skeleton")))
                tableBuilder.pool(pool("uam_wither_skeleton", 1, 14, item(ModRegistries.WITHER_APPLE.get(), 1, 1)));
            else if (id.equals(rl("entities/elder_guardian")))
                tableBuilder.pool(pool("uam_elder_guardian", 1, 1, item(ModRegistries.PRISM_APPLE.get(), 1, 1)));
            else if (id.equals(rl("entities/drowned")))
                tableBuilder.pool(pool("uam_drowned", 1, 19, item(ModRegistries.PRISM_APPLE.get(), 1, 1)));
            else if (id.equals(rl("entities/parrot")))
                tableBuilder.pool(pool("uam_parrot", 3, 7, item(ModRegistries.BANANA.get(), 1, 1)));
            else if (id.equals(rl("entities/zombie"))) {
                tableBuilder.pool(pool("uam_zombie_pear", 1, 39, item(ModRegistries.BIRNE.get(), 1, 1)));
                tableBuilder.pool(LootPool.lootPool()
                    .name("uam_baby_zombie_rotten")
                    .setRolls(ConstantValue.exactly(1))
                    .when(LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.THIS,
                        EntityPredicate.Builder.entity()
                            .flags(EntityFlagsPredicate.Builder.flags().setIsBaby(true).build())))
                    .add(LootItem.lootTableItem(ModRegistries.ROTTEN_APPLE.get()).setWeight(1).apply(count(1, 1)))
                    .add(EmptyLootItem.emptyItem().setWeight(9)));
            }
            else if (id.equals(rl("entities/husk")))
                tableBuilder.pool(pool("uam_husk_pear", 1, 39, item(ModRegistries.BIRNE.get(), 1, 1)));
            else if (id.equals(rl("entities/zombie_villager")))
                tableBuilder.pool(pool("uam_zombie_villager_pear", 1, 39, item(ModRegistries.BIRNE.get(), 1, 1)));
            else if (id.equals(rl("entities/evoker"))) {
                tableBuilder.modifyPools(poolBuilder -> poolBuilder
                    .when(net.minecraft.world.level.storage.loot.predicates.AlternativeLootItemCondition.alternative(
                        net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition.killedByPlayer(),
                        net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition.killedByPlayer()
                    )));
            }

            // ── Chest loot tables ──────────────────────────────────────────────
            else if (id.equals(rl("chests/simple_dungeon")))
                tableBuilder.pool(LootPool.lootPool().name("uam_dungeon").setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModRegistries.IRON_APPLE.get()).setWeight(3).apply(count(1, 2)))
                    .add(LootItem.lootTableItem(ModRegistries.ROTTEN_APPLE.get()).setWeight(2).apply(count(1, 2)))
                    .add(LootItem.lootTableItem(ModRegistries.BAKED_APPLE.get()).setWeight(2).apply(count(1, 2)))
                    .add(EmptyLootItem.emptyItem().setWeight(13)));
            else if (id.equals(rl("chests/mineshaft")))
                tableBuilder.pool(LootPool.lootPool().name("uam_mineshaft").setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModRegistries.COPPER_APPLE.get()).setWeight(3).apply(count(1, 2)))
                    .add(LootItem.lootTableItem(ModRegistries.ROASTED_APPLE.get()).setWeight(2).apply(count(1, 2)))
                    .add(LootItem.lootTableItem(ModRegistries.IRON_APPLE.get()).setWeight(2).apply(count(1, 1)))
                    .add(EmptyLootItem.emptyItem().setWeight(13)));
            else if (id.equals(rl("chests/village/village_weaponsmith")))
                tableBuilder.pool(LootPool.lootPool().name("uam_village_weaponsmith").setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModRegistries.IRON_APPLE.get()).setWeight(3).apply(count(1, 2)))
                    .add(LootItem.lootTableItem(ModRegistries.COPPER_APPLE.get()).setWeight(2).apply(count(1, 2)))
                    .add(EmptyLootItem.emptyItem().setWeight(15)));
            else if (id.equals(rl("chests/village/village_armorer")))
                tableBuilder.pool(LootPool.lootPool().name("uam_village_armorer").setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModRegistries.IRON_APPLE.get()).setWeight(4).apply(count(1, 2)))
                    .add(LootItem.lootTableItem(ModRegistries.DIAMOND_APPLE.get()).setWeight(1).apply(count(1, 1)))
                    .add(EmptyLootItem.emptyItem().setWeight(15)));
            else if (id.equals(rl("chests/desert_pyramid")))
                tableBuilder.pool(LootPool.lootPool().name("uam_desert_pyramid").setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModRegistries.HONEY_APPLE.get()).setWeight(3).apply(count(1, 2)))
                    .add(LootItem.lootTableItem(ModRegistries.REDSTONE_APPLE.get()).setWeight(2).apply(count(1, 1)))
                    .add(LootItem.lootTableItem(ModRegistries.BAKED_APPLE.get()).setWeight(2).apply(count(1, 2)))
                    .add(EmptyLootItem.emptyItem().setWeight(13)));
            else if (id.equals(rl("chests/jungle_temple")))
                tableBuilder.pool(LootPool.lootPool().name("uam_jungle_temple").setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModRegistries.ORCHARD_APPLE.get()).setWeight(3).apply(count(1, 2)))
                    .add(LootItem.lootTableItem(ModRegistries.ENDER_PEARL_APPLE.get()).setWeight(2).apply(count(1, 1)))
                    .add(LootItem.lootTableItem(ModRegistries.ECHO_APPLE.get()).setWeight(2).apply(count(1, 1)))
                    .add(EmptyLootItem.emptyItem().setWeight(13)));
            else if (id.equals(rl("chests/woodland_mansion")))
                tableBuilder.pool(LootPool.lootPool().name("uam_woodland_mansion").setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModRegistries.TOTEM_APPLE.get()).setWeight(2).apply(count(1, 1)))
                    .add(LootItem.lootTableItem(ModRegistries.DIAMOND_APPLE.get()).setWeight(2).apply(count(1, 1)))
                    .add(LootItem.lootTableItem(ModRegistries.MOON_APPLE.get()).setWeight(3).apply(count(1, 1)))
                    .add(EmptyLootItem.emptyItem().setWeight(13)));
            else if (id.equals(rl("chests/pillager_outpost")))
                tableBuilder.pool(LootPool.lootPool().name("uam_pillager_outpost").setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModRegistries.APPLE_BOMB.get()).setWeight(3).apply(count(1, 3)))
                    .add(LootItem.lootTableItem(ModRegistries.IRON_APPLE.get()).setWeight(2).apply(count(1, 2)))
                    .add(EmptyLootItem.emptyItem().setWeight(15)));
            else if (id.equals(rl("chests/buried_treasure")))
                tableBuilder.pool(LootPool.lootPool().name("uam_buried_treasure").setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModRegistries.DIAMOND_APPLE.get()).setWeight(1).apply(count(1, 1)))
                    .add(LootItem.lootTableItem(ModRegistries.PRISM_APPLE.get()).setWeight(1).apply(count(1, 1)))
                    .add(EmptyLootItem.emptyItem().setWeight(2)));
            else if (id.equals(rl("chests/shipwreck/supply")))
                tableBuilder.pool(LootPool.lootPool().name("uam_shipwreck_supply").setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModRegistries.PRISM_APPLE.get()).setWeight(2).apply(count(1, 1)))
                    .add(LootItem.lootTableItem(ModRegistries.BANANA.get()).setWeight(2).apply(count(1, 2)))
                    .add(LootItem.lootTableItem(ModRegistries.BAKED_APPLE.get()).setWeight(2).apply(count(1, 2)))
                    .add(EmptyLootItem.emptyItem().setWeight(4)));
            else if (id.equals(rl("chests/stronghold_corridor")))
                tableBuilder.pool(LootPool.lootPool().name("uam_stronghold_corridor").setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModRegistries.EMERALD_APPLE.get()).setWeight(3).apply(count(1, 1)))
                    .add(LootItem.lootTableItem(ModRegistries.ENDER_PEARL_APPLE.get()).setWeight(2).apply(count(1, 1)))
                    .add(EmptyLootItem.emptyItem().setWeight(10)));
            else if (id.equals(rl("chests/stronghold_library")))
                tableBuilder.pool(LootPool.lootPool().name("uam_stronghold_library").setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModRegistries.DRAGON_APPLE.get()).setWeight(2).apply(count(1, 1)))
                    .add(LootItem.lootTableItem(ModRegistries.LONGEVITY_APPLE.get()).setWeight(3).apply(count(1, 1)))
                    .add(EmptyLootItem.emptyItem().setWeight(12)));
            else if (id.equals(rl("chests/ancient_city")))
                tableBuilder.pool(LootPool.lootPool().name("uam_ancient_city").setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModRegistries.ECHO_APPLE.get()).setWeight(3).apply(count(1, 1)))
                    .add(LootItem.lootTableItem(ModRegistries.VOID_APPLE.get()).setWeight(2).apply(count(1, 1)))
                    .add(LootItem.lootTableItem(ModRegistries.TIME_FREEZE_APPLE.get()).setWeight(2).apply(count(1, 1)))
                    .add(EmptyLootItem.emptyItem().setWeight(13)));
            else if (id.equals(rl("chests/ruined_portal")))
                tableBuilder.pool(LootPool.lootPool().name("uam_ruined_portal").setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModRegistries.BLAZE_APPLE.get()).setWeight(4).apply(count(1, 2)))
                    .add(LootItem.lootTableItem(ModRegistries.NETHERITE_APPLE.get()).setWeight(1).apply(count(1, 1)))
                    .add(EmptyLootItem.emptyItem().setWeight(15)));
            else if (id.equals(rl("chests/nether_bridge")))
                tableBuilder.pool(LootPool.lootPool().name("uam_nether_bridge").setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModRegistries.BLAZE_APPLE.get()).setWeight(1).apply(count(1, 2)))
                    .add(LootItem.lootTableItem(ModRegistries.WITHER_APPLE.get()).setWeight(1).apply(count(1, 1)))
                    .add(EmptyLootItem.emptyItem().setWeight(2)));
            else if (id.equals(rl("chests/bastion_treasure")))
                tableBuilder.pool(LootPool.lootPool().name("uam_bastion_treasure").setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModRegistries.NETHERITE_APPLE.get()).setWeight(3).apply(count(1, 1)))
                    .add(LootItem.lootTableItem(ModRegistries.DRAGON_APPLE.get()).setWeight(2).apply(count(1, 1)))
                    .add(LootItem.lootTableItem(ModRegistries.NETHER_STAR_APPLE.get()).setWeight(1).apply(count(1, 1)))
                    .add(EmptyLootItem.emptyItem().setWeight(4)));
            else if (id.equals(rl("chests/end_city_treasure")))
                tableBuilder.pool(LootPool.lootPool().name("uam_end_city").setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModRegistries.VOID_APPLE.get()).setWeight(3).apply(count(1, 2)))
                    .add(LootItem.lootTableItem(ModRegistries.DRAGON_APPLE.get()).setWeight(2).apply(count(1, 1)))
                    .add(LootItem.lootTableItem(ModRegistries.QUANTUM_APPLE.get()).setWeight(2).apply(count(1, 1)))
                    .add(EmptyLootItem.emptyItem().setWeight(3)));
        });
    }

    private static ResourceLocation rl(String path) { return new ResourceLocation("minecraft", path); }

    private static SetItemCountFunction.Builder count(int min, int max) {
        return SetItemCountFunction.setCount(UniformGenerator.between(min, max));
    }

    private static LootPool pool(String name, int itemWeight, int emptyWeight, LootItem.Builder<?> entry) {
        return LootPool.lootPool().name(name).setRolls(ConstantValue.exactly(1))
            .add(entry.setWeight(itemWeight))
            .add(EmptyLootItem.emptyItem().setWeight(emptyWeight))
            .build();
    }

    private static LootItem.Builder<?> item(net.minecraft.world.item.Item item, int min, int max) {
        return LootItem.lootTableItem(item).apply(count(min, max));
    }
}
