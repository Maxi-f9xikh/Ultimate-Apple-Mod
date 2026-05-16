// common/src/main/java/de/maxi/ultimate_apple_mod/ModRegistries.java
package de.maxi.ultimate_apple_mod;

import de.maxi.ultimate_apple_mod.item.AppleBombEntity;
import de.maxi.ultimate_apple_mod.item.NuclearAppleEntity;
import de.maxi.ultimate_apple_mod.item.ShakeBombEntity;
import de.maxi.ultimate_apple_mod.item.TntAppleEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class ModRegistries {

    // ── Effects ───────────────────────────────────────────────────────────────
    public static Supplier<MobEffect> CURSE_OF_ROTTEN;
    public static Supplier<MobEffect> MOON_GRAVITY;
    public static Supplier<MobEffect> LIFESTEAL;
    public static Supplier<MobEffect> TOTEM_PROTECTION;
    public static Supplier<MobEffect> TIME_FREEZE;

    // ── Items ─────────────────────────────────────────────────────────────────
    public static Supplier<Item> DIAMOND_APPLE;
    public static Supplier<Item> LAPISLAZULI_APPLE;
    public static Supplier<Item> EMERALD_APPLE;
    public static Supplier<Item> REDSTONE_APPLE;
    public static Supplier<Item> NETHERITE_APPLE;
    public static Supplier<Item> IRON_APPLE;
    public static Supplier<Item> ROTTEN_APPLE;
    public static Supplier<Item> ROASTED_APPLE;
    public static Supplier<Item> BAKED_APPLE;
    public static Supplier<Item> BURNT_APPLE;
    public static Supplier<Item> BLAZE_APPLE;
    public static Supplier<Item> BIRNE;           // pear_apple
    public static Supplier<Item> COPPER_APPLE;
    public static Supplier<Item> EXPOSED_COPPER_APPLE;
    public static Supplier<Item> WEATHERED_COPPER_APPLE;
    public static Supplier<Item> OXIDIZED_COPPER_APPLE;
    public static Supplier<Item> WAXED_COPPER_APPLE;
    public static Supplier<Item> WAXED_EXPOSED_COPPER_APPLE;
    public static Supplier<Item> WAXED_WEATHERED_COPPER_APPLE;
    public static Supplier<Item> WAXED_OXIDIZED_COPPER_APPLE;
    public static Supplier<Item> ENDER_PEARL_APPLE;
    public static Supplier<Item> MOON_APPLE;
    public static Supplier<Item> ORCHARD_APPLE;
    public static Supplier<Item> ECHO_APPLE;
    public static Supplier<Item> REWIND_APPLE;
    public static Supplier<Item> APPLE_BOMB;
    public static Supplier<Item> COAL_APPLE;
    public static Supplier<Item> TNT_APPLE;
    public static Supplier<Item> NUCLEAR_APPLE;
    public static Supplier<Item> WITHER_APPLE;
    public static Supplier<Item> HONEY_APPLE;
    public static Supplier<Item> DRAGON_APPLE;
    public static Supplier<Item> NETHER_STAR_APPLE;
    public static Supplier<Item> DIRT_APPLE;
    public static Supplier<Item> TOTEM_APPLE;
    public static Supplier<Item> QUANTUM_APPLE;
    public static Supplier<Item> VOID_APPLE;
    public static Supplier<Item> TIME_FREEZE_APPLE;
    public static Supplier<Item> LONGEVITY_APPLE;
    public static Supplier<Item> PRISM_APPLE;
    public static Supplier<Item> BANANA;
    public static Supplier<Item> CUP_ITEM;
    public static Supplier<Item> SHAKE_ITEM;

    // ── Entity types ──────────────────────────────────────────────────────────
    public static Supplier<EntityType<AppleBombEntity>>    APPLE_BOMB_ENTITY;
    public static Supplier<EntityType<ShakeBombEntity>>    SHAKE_BOMB_ENTITY;
    public static Supplier<EntityType<TntAppleEntity>>     TNT_APPLE_ENTITY;
    public static Supplier<EntityType<NuclearAppleEntity>> NUCLEAR_APPLE_ENTITY;

    // ── Block + BlockEntity + Menu ────────────────────────────────────────────
    public static Supplier<Block>              MIXER;
    public static Supplier<Item>               MIXER_ITEM;
    public static Supplier<BlockEntityType<?>> MIXER_BLOCK_ENTITY;
    public static Supplier<MenuType<?>>        MIXER_MENU_TYPE;
}
