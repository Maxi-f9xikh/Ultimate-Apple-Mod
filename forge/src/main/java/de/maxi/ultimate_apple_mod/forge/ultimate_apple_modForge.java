package de.maxi.ultimate_apple_mod.forge;

import de.maxi.ultimate_apple_mod.effect.CurseOfRotten;
import de.maxi.ultimate_apple_mod.effect.GlitchEffect;
import de.maxi.ultimate_apple_mod.effect.GravityEffect;
import de.maxi.ultimate_apple_mod.effect.LifestealEffect;
import de.maxi.ultimate_apple_mod.effect.MoonGravityEffect;
import de.maxi.ultimate_apple_mod.effect.TimeFreezeEffect;
import de.maxi.ultimate_apple_mod.effect.TotemProtectionEffect;
import de.maxi.ultimate_apple_mod.item.LapislazuliAppleItem;
import de.maxi.ultimate_apple_mod.item.PrismAppleItem;
import de.maxi.ultimate_apple_mod.item.QuantumAppleItem;
import de.maxi.ultimate_apple_mod.item.TotemAppleItem;
import de.maxi.ultimate_apple_mod.item.VoidAppleItem;
import de.maxi.ultimate_apple_mod.forge.block.MixerBlockEntity;
import de.maxi.ultimate_apple_mod.forge.block.MixerMenu;
import de.maxi.ultimate_apple_mod.forge.block.ModBlocks;
import de.maxi.ultimate_apple_mod.forge.network.NetworkHandler;
import de.maxi.ultimate_apple_mod.item.AppleBombEntity;
import de.maxi.ultimate_apple_mod.item.AppleBombItem;
import de.maxi.ultimate_apple_mod.item.BlazingAppleStewItem;
import de.maxi.ultimate_apple_mod.item.CupItem;
import de.maxi.ultimate_apple_mod.item.EchoAppleItem;
import de.maxi.ultimate_apple_mod.item.EnderPearlAppleItem;
import de.maxi.ultimate_apple_mod.item.DragonAppleItem;
import de.maxi.ultimate_apple_mod.item.HoneyAppleItem;
import de.maxi.ultimate_apple_mod.item.OrchardCallerItem;
import de.maxi.ultimate_apple_mod.item.RewindAppleItem;
import de.maxi.ultimate_apple_mod.item.ShakeItem;
import de.maxi.ultimate_apple_mod.item.WitherAppleItem;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(ultimate_apple_mod.MOD_ID)
public final class ultimate_apple_modForge {

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, ultimate_apple_mod.MOD_ID);

    public static final DeferredRegister<MobEffect> EFFECTS =
        DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ultimate_apple_mod.MOD_ID);

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ultimate_apple_mod.MOD_ID);

    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ultimate_apple_mod.MOD_ID);

    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, ultimate_apple_mod.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ultimate_apple_mod.MOD_ID);

    // ── Effects ──────────────────────────────────────────────────────────────

    public static final RegistryObject<MobEffect> CURSE_OF_ROTTEN =
        EFFECTS.register("curse_of_rotten", CurseOfRotten::new);

    public static final RegistryObject<MobEffect> Moon_EFFECT =
        EFFECTS.register("gravity_inversion", GravityEffect::new);

    public static final RegistryObject<MobEffect> MOON_GRAVITY_EFFECT =
        EFFECTS.register("moon_gravity", MoonGravityEffect::new);

    public static final RegistryObject<MobEffect> GLITCH_EFFECT =
        EFFECTS.register("glitch", GlitchEffect::new);

    public static final RegistryObject<MobEffect> LIFESTEAL_EFFECT =
        EFFECTS.register("lifesteal", LifestealEffect::new);

    public static final RegistryObject<MobEffect> TOTEM_PROTECTION_EFFECT =
        EFFECTS.register("totem_protection", TotemProtectionEffect::new);

    public static final RegistryObject<MobEffect> TIME_FREEZE_EFFECT =
        EFFECTS.register("time_freeze", TimeFreezeEffect::new);

    // ── Menu Types ────────────────────────────────────────────────────────────

    public static final RegistryObject<MenuType<MixerMenu>> MIXER_MENU_TYPE =
        MENUS.register("mixer", () -> IForgeMenuType.create(MixerMenu::new));

    // ── Block Entity Types ────────────────────────────────────────────────────

    public static final RegistryObject<BlockEntityType<MixerBlockEntity>> MIXER_BLOCK_ENTITY =
        BLOCK_ENTITIES.register("mixer", () -> BlockEntityType.Builder
            .of(MixerBlockEntity::new, ModBlocks.MIXER.get())
            .build(null));

    // ── Entity Types ─────────────────────────────────────────────────────────

    public static final RegistryObject<EntityType<AppleBombEntity>> APPLE_BOMB_ENTITY =
        ENTITY_TYPES.register("apple_bomb",
            () -> EntityType.Builder.<AppleBombEntity>of(AppleBombEntity::new, MobCategory.MISC)
                .sized(0.25f, 0.25f)
                .clientTrackingRange(4)
                .build("apple_bomb"));

    // ── Existing Items (with effects added) ──────────────────────────────────

    public static final RegistryObject<Item> DIAMOND_APPLE = ITEMS.register("diamond_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(8).saturationMod(0.9f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST,      20 * 60, 2), 1.0f) // Health Boost III, 60s
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION,      20 * 20, 1), 1.0f) // Regen II, 20s
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 30, 1), 1.0f) // Resistance II, 30s
                .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION,        20 * 60, 0), 1.0f) // Absorption I, 60s
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> LAPISLAZULI_APPLE =
        ITEMS.register("lapislazuli_apple", LapislazuliAppleItem::new);

    public static final RegistryObject<Item> EMERALD_APPLE = ITEMS.register("emerald_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(8).saturationMod(0.9f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.LUCK, 20 * 60, 1), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 30, 0), 1.0f)
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> REDSTONE_APPLE = ITEMS.register("redstone_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(8).saturationMod(0.9f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 20, 2), 1.0f) // Speed III, 20s
                .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED,      20 * 20, 2), 1.0f) // Haste III, 20s
                .effect(() -> new MobEffectInstance(MobEffects.GLOWING,        20 * 20, 0), 1.0f) // Glowing, 20s
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST,   20 * 15, 0), 1.0f) // Strength I, 15s
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> NETHERITE_APPLE = ITEMS.register("netherite_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(10).saturationMod(1.0f).alwaysEat()
                // Near-godlike defensive stats befitting the cost of 1 Netherite Ingot
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 120, 2), 1.0f) // Resistance III, 2 min
                .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE,   20 * 120, 0), 1.0f) // Fire Resistance, 2 min
                .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST,      20 * 120, 3), 1.0f) // Health Boost IV, 2 min
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION,      20 *  30, 2), 1.0f) // Regen III, 30s
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST,      20 *  30, 1), 1.0f) // Strength II, 30s
                .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION,        20 * 120, 2), 1.0f) // Absorption III, 2 min
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> IRON_APPLE = ITEMS.register("iron_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(6).saturationMod(0.7f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST,      20 * 30, 0), 1.0f) // Health Boost I, 30s
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION,      20 * 10, 0), 1.0f) // Regen I, 10s
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 15, 0), 1.0f) // Resistance I, 15s
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> ROTTEN_APPLE = ITEMS.register("rotten_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(2).saturationMod(0.1f).alwaysEat()
                // CurseOfRotten handles speed boost + hitbox shrink.
                // Nausea is applied separately so it shows up as its own effect icon.
                .effect(() -> new MobEffectInstance(CURSE_OF_ROTTEN.get(), 400, 0, false, true), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 400, 0), 1.0f)
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> ROASTED_APPLE = ITEMS.register("roasted_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(2).saturationMod(0.1f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 20, 0), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.SATURATION, 20 * 10, 0), 1.0f)
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> BAKED_APPLE = ITEMS.register("baked_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(2).saturationMod(0.1f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20 * 5, 0), 1.0f)
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> BURNT_APPLE = ITEMS.register("burnt_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(1).saturationMod(0.1f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 20 * 15, 1), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 20 * 5, 0), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 5, 0), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 15, 0), 1.0f)
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> BLAZE_APPLE = ITEMS.register("blaze_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(6).saturationMod(0.5f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20 * 5, 0), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 5, 0), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 15, 0), 1.0f)
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> BLAZING_APPLE_STEW =
        ITEMS.register("blazing_apple_stew", () -> new BlazingAppleStewItem());

    public static final RegistryObject<Item> BIRNE = ITEMS.register("pear_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(1).saturationMod(0.1f)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20 * 15, 0), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.SATURATION, 20 * 5, 0), 1.0f)
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> COPPER_APPLE = ITEMS.register("copper_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(5).saturationMod(0.6f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED,         20 * 25, 1), 1.0f) // Haste II, 25s
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST,       20 * 25, 0), 1.0f) // Strength I, 25s
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,  20 * 25, 0), 1.0f) // Resistance I, 25s
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> ENDER_PEARL_APPLE =
        ITEMS.register("ender_pearl_apple", EnderPearlAppleItem::new);

    // ── New Items ────────────────────────────────────────────────────────────

    public static final RegistryObject<Item> MOON_APPLE = ITEMS.register("moon_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(6).saturationMod(0.6f).alwaysEat()
                .effect(() -> new MobEffectInstance(MOON_GRAVITY_EFFECT.get(), 20 * 30, 0), 1.0f) // Moon Gravity, 30s
                .effect(() -> new MobEffectInstance(MobEffects.JUMP, 20 * 30, 1), 1.0f) // Jump Boost II, 30s
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> ORCHARD_APPLE =
        ITEMS.register("orchard_apple", () ->
            new OrchardCallerItem(new Item.Properties()
                .food(new FoodProperties.Builder()
                    .nutrition(4).saturationMod(0.4f).alwaysEat()
                    .build())
                .stacksTo(64)));

    public static final RegistryObject<Item> ECHO_APPLE =
        ITEMS.register("echo_apple", () ->
            new EchoAppleItem(new Item.Properties()
                .food(new FoodProperties.Builder()
                    .nutrition(5).saturationMod(0.5f).alwaysEat()
                    .build())
                .stacksTo(64)));

    public static final RegistryObject<Item> REWIND_APPLE =
        ITEMS.register("rewind_apple", () ->
            new RewindAppleItem(new Item.Properties()
                .food(new FoodProperties.Builder()
                    .nutrition(4).saturationMod(0.3f).alwaysEat()
                    .build())
                .stacksTo(64)));

    public static final RegistryObject<Item> APPLE_BOMB =
        ITEMS.register("apple_bomb", () ->
            new AppleBombItem(new Item.Properties().stacksTo(16)));

    // ── Second-Wave Items ────────────────────────────────────────────────────

    public static final RegistryObject<Item> WITHER_APPLE =
        ITEMS.register("wither_apple", () ->
            new WitherAppleItem(new Item.Properties()
                .food(new FoodProperties.Builder()
                    .nutrition(6).saturationMod(0.6f).alwaysEat()
                    .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 20 * 30, 2), 1.0f)
                    .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 10, 1), 1.0f)
                    .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20 * 5, 1), 1.0f)
                    .build())
                .stacksTo(64)));

    public static final RegistryObject<Item> HONEY_APPLE =
        ITEMS.register("honey_apple", () ->
            new HoneyAppleItem(new Item.Properties()
                .food(new FoodProperties.Builder()
                    .nutrition(4).saturationMod(0.6f).alwaysEat()
                    .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 5, 0), 1.0f)
                    .build())
                .stacksTo(64)));

    public static final RegistryObject<Item> DRAGON_APPLE =
        ITEMS.register("dragon_apple", () ->
            new DragonAppleItem(new Item.Properties()
                .food(new FoodProperties.Builder()
                    .nutrition(8).saturationMod(0.8f).alwaysEat()
                    .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 20 * 10, 3), 1.0f)
                    .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 10, 1), 1.0f)
                    .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20 * 10, 2), 1.0f)
                    .build())
                .stacksTo(64)));

    public static final RegistryObject<Item> NETHER_STAR_APPLE = ITEMS.register("nether_star_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(10).saturationMod(1.0f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 4), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 20 * 30, 3), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 10, 2), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20 * 5, 3), 1.0f)
                .build())
            .stacksTo(1)) {
            @Override
            public void appendHoverText(ItemStack stack,
                    @javax.annotation.Nullable net.minecraft.world.level.Level level,
                    java.util.List<net.minecraft.network.chat.Component> components,
                    net.minecraft.world.item.TooltipFlag flag) {
                components.add(net.minecraft.network.chat.Component.translatable(
                    "tooltip.ultimate_apple_mod.nether_star_apple.line1"));
                components.add(net.minecraft.network.chat.Component.translatable(
                    "tooltip.ultimate_apple_mod.nether_star_apple.line2"));
            }
        });

    public static final RegistryObject<Item> DIRT_APPLE = ITEMS.register("dirt_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(1).saturationMod(0.0f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 20 * 30, 2), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 20 * 10, 0), 1.0f)
                .build())
            .stacksTo(64)));

    // ── Special Apples ────────────────────────────────────────────────────────

    public static final RegistryObject<Item> TOTEM_APPLE =
        ITEMS.register("totem_apple", TotemAppleItem::new);

    public static final RegistryObject<Item> QUANTUM_APPLE =
        ITEMS.register("quantum_apple", QuantumAppleItem::new);

    public static final RegistryObject<Item> VOID_APPLE =
        ITEMS.register("void_apple", VoidAppleItem::new);

    public static final RegistryObject<Item> TIME_FREEZE_APPLE = ITEMS.register("time_freeze_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(4).saturationMod(0.4f).alwaysEat()
                .effect(() -> new MobEffectInstance(TIME_FREEZE_EFFECT.get(), 20 * 30, 0), 1.0f)
                .build())
            .stacksTo(64)));

    // ── Longevity Apple ───────────────────────────────────────────────────────

    public static final RegistryObject<Item> LONGEVITY_APPLE = ITEMS.register("longevity_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(6).saturationMod(0.6f).alwaysEat()
                // When eaten on its own: generous survival buffs
                .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION,    20 * 120, 3), 1.0f) // Absorption IV, 2 min
                .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST,  20 *  60, 0), 1.0f) // Health Boost I, 1 min
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION,  20 *  15, 0), 1.0f) // Regen I, 15 s
                .build())
            .stacksTo(64)));

    // ── Prism Apple ──────────────────────────────────────────────────────────

    public static final RegistryObject<Item> PRISM_APPLE =
        ITEMS.register("prism_apple", PrismAppleItem::new);

    // ── Banana ───────────────────────────────────────────────────────────────

    public static final RegistryObject<Item> BANANA = ITEMS.register("banana", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(4)
                .saturationMod(0.5f)
                .build())
            .stacksTo(64)));

    // ── Mixer Items ───────────────────────────────────────────────────────────

    public static final RegistryObject<Item> CUP_ITEM =
        ITEMS.register("cup", CupItem::new);

    public static final RegistryObject<Item> SHAKE_ITEM =
        ITEMS.register("shake", ShakeItem::new);

    // ── Creative Tab ─────────────────────────────────────────────────────────

    public static final RegistryObject<CreativeModeTab> ULTIMATE_TAB = TABS.register("ultimate_tab", () ->
        CreativeModeTab.builder()
            .title(Component.literal("Ultimate Apple Mod"))
            .icon(() -> new ItemStack(DIAMOND_APPLE.get()))
            .displayItems((parameters, output) -> {
                output.accept(LAPISLAZULI_APPLE.get());
                output.accept(COPPER_APPLE.get());
                output.accept(REDSTONE_APPLE.get());
                output.accept(IRON_APPLE.get());
                output.accept(DIAMOND_APPLE.get());
                output.accept(NETHERITE_APPLE.get());
                output.accept(EMERALD_APPLE.get());
                output.accept(ROTTEN_APPLE.get());
                output.accept(ROASTED_APPLE.get());
                output.accept(BAKED_APPLE.get());
                output.accept(BURNT_APPLE.get());
                output.accept(BIRNE.get());
                output.accept(BLAZE_APPLE.get());
                output.accept(BLAZING_APPLE_STEW.get());
                output.accept(ENDER_PEARL_APPLE.get());
                output.accept(MOON_APPLE.get());
                output.accept(ORCHARD_APPLE.get());
                output.accept(ECHO_APPLE.get());
                output.accept(REWIND_APPLE.get());
                output.accept(APPLE_BOMB.get());
                output.accept(WITHER_APPLE.get());
                output.accept(HONEY_APPLE.get());
                output.accept(DRAGON_APPLE.get());
                output.accept(NETHER_STAR_APPLE.get());
                output.accept(DIRT_APPLE.get());
                output.accept(TOTEM_APPLE.get());
                output.accept(QUANTUM_APPLE.get());
                output.accept(VOID_APPLE.get());
                output.accept(TIME_FREEZE_APPLE.get());
                output.accept(LONGEVITY_APPLE.get());
                output.accept(PRISM_APPLE.get());
                output.accept(BANANA.get());
                output.accept(CUP_ITEM.get());
                output.accept(SHAKE_ITEM.get());
                output.accept(ModBlocks.MIXER_ITEM.get());
            })
            .build());

    // ── Constructor ──────────────────────────────────────────────────────────

    public ultimate_apple_modForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        TABS.register(modEventBus);
        EFFECTS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        MENUS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModRecipes.register(modEventBus);
        NetworkHandler.register();
        ultimate_apple_mod.init();
    }
}
