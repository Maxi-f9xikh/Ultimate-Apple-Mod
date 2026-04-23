package de.maxi.ultimate_apple_mod.forge;

import de.maxi.ultimate_apple_mod.effect.CurseOfRotten;
import de.maxi.ultimate_apple_mod.effect.GlitchEffect;
import de.maxi.ultimate_apple_mod.effect.GravityEffect;
import de.maxi.ultimate_apple_mod.item.AppleBombEntity;
import de.maxi.ultimate_apple_mod.item.AppleBombItem;
import de.maxi.ultimate_apple_mod.item.BlazingAppleStewItem;
import de.maxi.ultimate_apple_mod.item.EchoAppleItem;
import de.maxi.ultimate_apple_mod.item.EnderPearlAppleItem;
import de.maxi.ultimate_apple_mod.item.OrchardCallerItem;
import de.maxi.ultimate_apple_mod.forge.block.ModBlocks;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

    // ── Effects ──────────────────────────────────────────────────────────────

    public static final RegistryObject<MobEffect> CURSE_OF_ROTTEN =
        EFFECTS.register("curse_of_rotten", CurseOfRotten::new);

    public static final RegistryObject<MobEffect> GRAVITY_EFFECT =
        EFFECTS.register("gravity_inversion", GravityEffect::new);

    public static final RegistryObject<MobEffect> GLITCH_EFFECT =
        EFFECTS.register("glitch", GlitchEffect::new);

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
                .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 30, 2), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20 * 10, 1), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 10, 1), 1.0f)
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> LAPISLAZULI_APPLE = ITEMS.register("lapislazuli_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(8).saturationMod(0.9f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.LUCK, 20 * 30, 1), 1.0f)
                .build())
            .stacksTo(64)));

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
                .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 20, 1), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 20 * 20, 0), 1.0f)
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> NETHERITE_APPLE = ITEMS.register("netherite_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(8).saturationMod(0.9f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 60, 0), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 30, 1), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 15, 0), 1.0f)
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> IRON_APPLE = ITEMS.register("iron_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(6).saturationMod(0.7f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 10, 0), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20 * 3, 0), 1.0f)
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> ROTTEN_APPLE = ITEMS.register("rotten_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(2).saturationMod(0.1f).alwaysEat()
                .effect(() -> new MobEffectInstance(CURSE_OF_ROTTEN.get(), 400, 0, false, true), 1.0f)
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
                .nutrition(5).saturationMod(0.6f)
                .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 20 * 20, 1), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.WATER_BREATHING, 20 * 30, 0), 1.0f)
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> ENDER_PEARL_APPLE =
        ITEMS.register("ender_pearl_apple", EnderPearlAppleItem::new);

    // ── New Items ────────────────────────────────────────────────────────────

    public static final RegistryObject<Item> GRAVITY_APPLE = ITEMS.register("gravity_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(6).saturationMod(0.6f).alwaysEat()
                .effect(() -> new MobEffectInstance(GRAVITY_EFFECT.get(), 200, 0), 1.0f)
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> ORCHARD_CALLER =
        ITEMS.register("orchard_caller", () ->
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

    public static final RegistryObject<Item> GLITCH_APPLE = ITEMS.register("glitch_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(4).saturationMod(0.3f).alwaysEat()
                .effect(() -> new MobEffectInstance(GLITCH_EFFECT.get(), 100, 0), 1.0f)
                .build())
            .stacksTo(16)) {
            @Override
            public void appendHoverText(ItemStack stack,
                    @javax.annotation.Nullable net.minecraft.world.level.Level level,
                    java.util.List<net.minecraft.network.chat.Component> components,
                    net.minecraft.world.item.TooltipFlag flag) {
                components.add(net.minecraft.network.chat.Component.translatable(
                    "tooltip.ultimate_apple_mod.glitch_apple.line1"));
                components.add(net.minecraft.network.chat.Component.translatable(
                    "tooltip.ultimate_apple_mod.glitch_apple.line2"));
            }
        });

    public static final RegistryObject<Item> APPLE_BOMB =
        ITEMS.register("apple_bomb", () ->
            new AppleBombItem(new Item.Properties().stacksTo(16)));

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
                output.accept(GRAVITY_APPLE.get());
                output.accept(ORCHARD_CALLER.get());
                output.accept(ECHO_APPLE.get());
                output.accept(GLITCH_APPLE.get());
                output.accept(APPLE_BOMB.get());
            })
            .build());

    // ── Constructor ──────────────────────────────────────────────────────────

    public ultimate_apple_modForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        TABS.register(modEventBus);
        EFFECTS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModRecipes.register(modEventBus);
        ultimate_apple_mod.init();
    }
}
