package de.maxi.ultimate_apple_mod.neoforge.event;

import de.maxi.ultimate_apple_mod.neoforge.ultimate_apple_modNeoForge;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.LootTableLoadEvent;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.NEOFORGE)
public class LootTableHandler {

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation name = event.getName();

        if (name.equals(rl("entities/blaze"))) {
            event.getTable().addPool(pool("uam_blaze", 1, 3,
                item(ultimate_apple_modNeoForge.BLAZE_APPLE.get(), 1, 1, 1)));
        } else if (name.equals(rl("entities/enderman"))) {
            event.getTable().addPool(pool("uam_enderman", 1, 3,
                item(ultimate_apple_modNeoForge.ENDER_PEARL_APPLE.get(), 1, 1, 1)));
        } else if (name.equals(rl("entities/skeleton"))) {
            event.getTable().addPool(pool("uam_skeleton", 2, 23,
                item(ultimate_apple_modNeoForge.IRON_APPLE.get(), 2, 1, 1)));
        } else if (name.equals(rl("entities/creeper"))) {
            event.getTable().addPool(pool("uam_creeper", 3, 17,
                item(ultimate_apple_modNeoForge.DIRT_APPLE.get(), 3, 1, 1)));
        } else if (name.equals(rl("entities/witch"))) {
            event.getTable().addPool(pool("uam_witch", 3, 22,
                item(ultimate_apple_modNeoForge.HONEY_APPLE.get(), 3, 1, 1)));
        } else if (name.equals(rl("entities/phantom"))) {
            event.getTable().addPool(pool("uam_phantom", 1, 9,
                item(ultimate_apple_modNeoForge.MOON_APPLE.get(), 1, 1, 1)));
        } else if (name.equals(rl("entities/iron_golem"))) {
            event.getTable().addPool(pool("uam_iron_golem", 1, 4,
                item(ultimate_apple_modNeoForge.IRON_APPLE.get(), 1, 1, 1)));
        } else if (name.equals(rl("entities/pillager"))) {
            event.getTable().addPool(pool("uam_pillager", 1, 19,
                item(ultimate_apple_modNeoForge.APPLE_BOMB.get(), 1, 1, 1)));
        } else if (name.equals(rl("entities/shulker"))) {
            event.getTable().addPool(pool("uam_shulker", 2, 23,
                item(ultimate_apple_modNeoForge.VOID_APPLE.get(), 2, 1, 1)));
        } else if (name.equals(rl("entities/wither_skeleton"))) {
            event.getTable().addPool(pool("uam_wither_skeleton", 1, 14,
                item(ultimate_apple_modNeoForge.WITHER_APPLE.get(), 1, 1, 1)));
        } else if (name.equals(rl("entities/elder_guardian"))) {
            event.getTable().addPool(pool("uam_elder_guardian", 1, 1,
                item(ultimate_apple_modNeoForge.PRISM_APPLE.get(), 1, 1, 1)));
        } else if (name.equals(rl("entities/drowned"))) {
            event.getTable().addPool(pool("uam_drowned", 1, 19,
                item(ultimate_apple_modNeoForge.PRISM_APPLE.get(), 1, 1, 1)));
        } else if (name.equals(rl("entities/parrot"))) {
            event.getTable().addPool(pool("uam_parrot", 3, 7,
                item(ultimate_apple_modNeoForge.BANANA.get(), 3, 1, 1)));
        } else if (name.equals(rl("entities/zombie"))) {
            event.getTable().addPool(pool("uam_zombie_pear", 1, 39,
                item(ultimate_apple_modNeoForge.BIRNE.get(), 1, 1, 1)));
            event.getTable().addPool(LootPool.lootPool()
                .name("uam_baby_zombie_rotten")
                .setRolls(ConstantValue.exactly(1))
                .when(LootItemEntityPropertyCondition.hasProperties(
                    LootContext.EntityTarget.THIS,
                    EntityPredicate.Builder.entity()
                        .flags(EntityFlagsPredicate.Builder.flags().setIsBaby(true).build())
                ))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.ROTTEN_APPLE.get())
                    .setWeight(1).apply(count(1, 1)))
                .add(EmptyLootItem.emptyItem().setWeight(9))
                .build());
        } else if (name.equals(rl("entities/husk"))) {
            event.getTable().addPool(pool("uam_husk_pear", 1, 39,
                item(ultimate_apple_modNeoForge.BIRNE.get(), 1, 1, 1)));
        } else if (name.equals(rl("entities/zombie_villager"))) {
            event.getTable().addPool(pool("uam_zombie_villager_pear", 1, 39,
                item(ultimate_apple_modNeoForge.BIRNE.get(), 1, 1, 1)));
        } else if (name.equals(rl("chests/simple_dungeon"))) {
            event.getTable().addPool(LootPool.lootPool().name("uam_dungeon")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.IRON_APPLE.get()).setWeight(3).apply(count(1,2)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.ROTTEN_APPLE.get()).setWeight(2).apply(count(1,2)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.BAKED_APPLE.get()).setWeight(2).apply(count(1,2)))
                .add(EmptyLootItem.emptyItem().setWeight(13)).build());
        } else if (name.equals(rl("chests/mineshaft"))) {
            event.getTable().addPool(LootPool.lootPool().name("uam_mineshaft")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.COPPER_APPLE.get()).setWeight(3).apply(count(1,2)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.ROASTED_APPLE.get()).setWeight(2).apply(count(1,2)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.IRON_APPLE.get()).setWeight(2).apply(count(1,1)))
                .add(EmptyLootItem.emptyItem().setWeight(13)).build());
        } else if (name.equals(rl("chests/village/village_weaponsmith"))) {
            event.getTable().addPool(LootPool.lootPool().name("uam_village_weaponsmith")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.IRON_APPLE.get()).setWeight(3).apply(count(1,2)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.COPPER_APPLE.get()).setWeight(2).apply(count(1,2)))
                .add(EmptyLootItem.emptyItem().setWeight(15)).build());
        } else if (name.equals(rl("chests/village/village_armorer"))) {
            event.getTable().addPool(LootPool.lootPool().name("uam_village_armorer")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.IRON_APPLE.get()).setWeight(4).apply(count(1,2)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.DIAMOND_APPLE.get()).setWeight(1).apply(count(1,1)))
                .add(EmptyLootItem.emptyItem().setWeight(15)).build());
        } else if (name.equals(rl("chests/desert_pyramid"))) {
            event.getTable().addPool(LootPool.lootPool().name("uam_desert_pyramid")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.HONEY_APPLE.get()).setWeight(3).apply(count(1,2)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.REDSTONE_APPLE.get()).setWeight(2).apply(count(1,1)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.BAKED_APPLE.get()).setWeight(2).apply(count(1,2)))
                .add(EmptyLootItem.emptyItem().setWeight(13)).build());
        } else if (name.equals(rl("chests/jungle_temple"))) {
            event.getTable().addPool(LootPool.lootPool().name("uam_jungle_temple")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.ORCHARD_APPLE.get()).setWeight(3).apply(count(1,2)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.ENDER_PEARL_APPLE.get()).setWeight(2).apply(count(1,1)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.ECHO_APPLE.get()).setWeight(2).apply(count(1,1)))
                .add(EmptyLootItem.emptyItem().setWeight(13)).build());
        } else if (name.equals(rl("chests/woodland_mansion"))) {
            event.getTable().addPool(LootPool.lootPool().name("uam_woodland_mansion")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.TOTEM_APPLE.get()).setWeight(2).apply(count(1,1)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.DIAMOND_APPLE.get()).setWeight(2).apply(count(1,1)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.MOON_APPLE.get()).setWeight(3).apply(count(1,1)))
                .add(EmptyLootItem.emptyItem().setWeight(13)).build());
        } else if (name.equals(rl("chests/pillager_outpost"))) {
            event.getTable().addPool(LootPool.lootPool().name("uam_pillager_outpost")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.APPLE_BOMB.get()).setWeight(3).apply(count(1,3)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.IRON_APPLE.get()).setWeight(2).apply(count(1,2)))
                .add(EmptyLootItem.emptyItem().setWeight(15)).build());
        } else if (name.equals(rl("chests/buried_treasure"))) {
            event.getTable().addPool(LootPool.lootPool().name("uam_buried_treasure")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.DIAMOND_APPLE.get()).setWeight(2).apply(count(1,1)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.PRISM_APPLE.get()).setWeight(3).apply(count(1,2)))
                .add(EmptyLootItem.emptyItem().setWeight(5)).build());
        } else if (name.equals(rl("chests/shipwreck/supply"))) {
            event.getTable().addPool(LootPool.lootPool().name("uam_shipwreck_supply")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.PRISM_APPLE.get()).setWeight(4).apply(count(1,2)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.BANANA.get()).setWeight(3).apply(count(1,3)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.BAKED_APPLE.get()).setWeight(3).apply(count(1,2)))
                .add(EmptyLootItem.emptyItem().setWeight(10)).build());
        } else if (name.equals(rl("chests/stronghold_corridor"))) {
            event.getTable().addPool(LootPool.lootPool().name("uam_stronghold_corridor")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.EMERALD_APPLE.get()).setWeight(2).apply(count(1,1)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.ENDER_PEARL_APPLE.get()).setWeight(3).apply(count(1,2)))
                .add(EmptyLootItem.emptyItem().setWeight(5)).build());
        } else if (name.equals(rl("chests/stronghold_library"))) {
            event.getTable().addPool(LootPool.lootPool().name("uam_stronghold_library")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.DRAGON_APPLE.get()).setWeight(1).apply(count(1,1)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.LONGEVITY_APPLE.get()).setWeight(2).apply(count(1,1)))
                .add(EmptyLootItem.emptyItem().setWeight(7)).build());
        } else if (name.equals(rl("chests/ancient_city"))) {
            event.getTable().addPool(LootPool.lootPool().name("uam_ancient_city")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.ECHO_APPLE.get()).setWeight(3).apply(count(1,2)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.VOID_APPLE.get()).setWeight(2).apply(count(1,1)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.TIME_FREEZE_APPLE.get()).setWeight(2).apply(count(1,1)))
                .add(EmptyLootItem.emptyItem().setWeight(13)).build());
        } else if (name.equals(rl("chests/ruined_portal"))) {
            event.getTable().addPool(LootPool.lootPool().name("uam_ruined_portal")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.BLAZE_APPLE.get()).setWeight(4).apply(count(1,2)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.NETHERITE_APPLE.get()).setWeight(1).apply(count(1,1)))
                .add(EmptyLootItem.emptyItem().setWeight(15)).build());
        } else if (name.equals(rl("chests/nether_bridge"))) {
            event.getTable().addPool(LootPool.lootPool().name("uam_nether_bridge")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.BLAZE_APPLE.get()).setWeight(4).apply(count(1,2)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.WITHER_APPLE.get()).setWeight(1).apply(count(1,1)))
                .add(EmptyLootItem.emptyItem().setWeight(5)).build());
        } else if (name.equals(rl("chests/bastion_treasure"))) {
            event.getTable().addPool(LootPool.lootPool().name("uam_bastion_treasure")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.NETHERITE_APPLE.get()).setWeight(3).apply(count(1,1)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.DRAGON_APPLE.get()).setWeight(2).apply(count(1,1)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.NETHER_STAR_APPLE.get()).setWeight(1).apply(count(1,1)))
                .add(EmptyLootItem.emptyItem().setWeight(4)).build());
        } else if (name.equals(rl("chests/end_city_treasure"))) {
            event.getTable().addPool(LootPool.lootPool().name("uam_end_city")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.VOID_APPLE.get()).setWeight(3).apply(count(1,2)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.DRAGON_APPLE.get()).setWeight(2).apply(count(1,1)))
                .add(LootItem.lootTableItem(ultimate_apple_modNeoForge.QUANTUM_APPLE.get()).setWeight(2).apply(count(1,1)))
                .add(EmptyLootItem.emptyItem().setWeight(3)).build());
        }
    }

    private static ResourceLocation rl(String path) {
        return new ResourceLocation("minecraft", path);
    }

    private static SetItemCountFunction.Builder count(int min, int max) {
        return SetItemCountFunction.setCount(UniformGenerator.between(min, max));
    }

    private static LootPool pool(String name, int itemWeight, int emptyWeight,
                                 LootItem.Builder<?> itemEntry) {
        return LootPool.lootPool().name(name)
            .setRolls(ConstantValue.exactly(1))
            .add(itemEntry.setWeight(itemWeight))
            .add(EmptyLootItem.emptyItem().setWeight(emptyWeight))
            .build();
    }

    private static LootItem.Builder<?> item(net.minecraft.world.item.Item item,
                                             int weight, int min, int max) {
        return LootItem.lootTableItem(item).apply(count(min, max));
    }
}
