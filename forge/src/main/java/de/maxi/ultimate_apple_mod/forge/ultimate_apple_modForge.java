package de.maxi.ultimate_apple_mod.forge;

import de.maxi.ultimate_apple_mod.item.BlazingAppleStewItem;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.world.item.BottleItem;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.network.chat.Component;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import de.maxi.ultimate_apple_mod.effect.CurseOfRotten;
import de.maxi.ultimate_apple_mod.forge.block.ModBlocks;
import de.maxi.ultimate_apple_mod.item.BlazingAppleStewItem;
import de.maxi.ultimate_apple_mod.item.EnderPearlAppleItem;


@Mod(ultimate_apple_mod.MOD_ID)
public final class ultimate_apple_modForge {

    // Item-Register vorbereiten
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ultimate_apple_mod.MOD_ID);

    //Effect-Register vorbereiten
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ultimate_apple_mod.MOD_ID);

    public static final RegistryObject<MobEffect> CURSE_OF_ROTTEN =
            EFFECTS.register("curse_of_rotten", CurseOfRotten::new);



    // Diamant-Apfel definieren
    public static final RegistryObject<Item> DIAMOND_APPLE = ITEMS.register("diamond_apple", () ->
            new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(8)
                            .saturationMod(0.9f)
                            .alwaysEat()
                            .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 30, 2), 1.0f)   //30 Sekunden
                            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20 * 10, 1), 1.0f)    // 10 Sekunden
                            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 10, 1), 1.0f)    // 10 Sekunden
                            .build())
                    .stacksTo(64)
            )
    );

    // Lapis-Apfel definieren
    public static final RegistryObject<Item> LAPISLAZULI_APPLE = ITEMS.register("lapislazuli_apple", () ->
            new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(8)
                            .saturationMod(0.9f)
                            .alwaysEat()
                            .build())
                    .stacksTo(64)
            )
    );

    // Emerald-Apfel definieren
    public static final RegistryObject<Item> EMERALD_APPLE = ITEMS.register("emerald_apple", () ->
            new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(8)
                            .saturationMod(0.9f)
                            .alwaysEat()
                            .build())
                    .stacksTo(64)
            )
    );

    // Redstone-Apfel definieren
    public static final RegistryObject<Item> REDSTONE_APPLE = ITEMS.register("redstone_apple", () ->
            new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(8)
                            .saturationMod(0.9f)
                            .alwaysEat()
                            .build())
                    .stacksTo(64)
            )
    );

    // Netherite-Apfel definieren
    public static final RegistryObject<Item> NETHERITE_APPLE = ITEMS.register("netherite_apple", () ->
            new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(8)
                            .saturationMod(0.9f)
                            .alwaysEat()
                            .build())
                    .stacksTo(64)
            )
    );

    //Eisen-Apfel definieren
    public static final RegistryObject<Item> IRON_APPLE = ITEMS.register("iron_apple", () ->
            new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(6)
                            .saturationMod(0.7f)
                            .alwaysEat()
                            .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 10, 0), 1.0f)       // 10 Sekunden
                            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20 * 3, 0), 1.0f)       // 3 Sekunden
                            .build())
                    .stacksTo(64)
            )
    );

    //Verrotteter Apfel definieren
    public static final RegistryObject<Item> ROTTEN_APPLE = ITEMS.register("rotten_apple", () ->
            new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(2)
                            .saturationMod(0.1f)
                            .alwaysEat()
                            .effect(() -> new MobEffectInstance(CURSE_OF_ROTTEN.get(), 400, 0, false, true), 1.0f)
                            .build())
                    .stacksTo(64)
            )
    );

    //geroesteter Apfel definieren
    public static final RegistryObject<Item> ROASTED_APPLE = ITEMS.register("roasted_apple", () ->
            new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(2)
                            .saturationMod(0.1f)
                            .alwaysEat()
                            .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 20, 0), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.SATURATION, 20 * 10, 0), 1.0f)
                            .build())
                    .stacksTo(64)
            )
    );

    //gebackener Apfel definiern
    public static final RegistryObject<Item> BAKED_APPLE = ITEMS.register("baked_apple", () ->
            new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(2)
                            .saturationMod(0.1f)
                            .alwaysEat()
                            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20 * 5, 0), 1.0f)
                            .build())
                    .stacksTo(64)
            )
    );

    //verbrannter Apfel definieren
    public static final RegistryObject<Item> BURNT_APPLE = ITEMS.register("burnt_apple", () ->
            new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(1)
                            .saturationMod(0.1f)
                            .alwaysEat()
                            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 20 * 15, 1), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 20 * 5, 0), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 5, 0), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 15, 0), 1.0f)
                            .build())
                    .stacksTo(64)
            )
    );

    //Blaze Apfel definieren
    public static final RegistryObject<Item> BLAZE_APPLE = ITEMS.register("blaze_apple", () ->
            new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(6)
                            .saturationMod(0.5f)
                            .alwaysEat()
                            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20 * 5, 0), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 5, 0), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 15, 0), 1.0f)
                            .build())
                    .stacksTo(64)
            )
    );

    //Blazing apple stew definieren
    public static final RegistryObject<Item> BLAZING_APPLE_STEW = ITEMS.register("blazing_apple_stew", () ->
            new BlazingAppleStewItem());

    // Birnen Apfel definieren
    public static final RegistryObject<Item> BIRNE = ITEMS.register("pear_apple", () ->
            new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(1)
                            .saturationMod(0.1f)
                            .build())
                    .stacksTo(64)
            )
    );

    //Copper apfel definieren
    public static final RegistryObject<Item> COPPER_APPLE = ITEMS.register("copper_apple", () ->
            new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(5)
                            .saturationMod(0.6f)
                            .build())
                    .stacksTo(64)
            )
    );

    // Richtig:
    public static final RegistryObject<Item> ENDER_PEARL_APPLE = ITEMS.register("ender_pearl_apple", EnderPearlAppleItem::new);



    // Eigenen Creative Tab registrieren mit items
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ultimate_apple_mod.MOD_ID);

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

                    })
                    .build()
    );


    public ultimate_apple_modForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Registrierungsreihenfolge:
        ITEMS.register(modEventBus);      // Items (die Effekte nutzen)
        TABS.register(modEventBus);       // Creative Tabs (die Items referenzieren)
        EFFECTS.register(modEventBus);     // Effekte
        ModBlocks.register(modEventBus); // Blocks
        ModRecipes.register(modEventBus); // statt nochmal get() aufzurufen

        ultimate_apple_mod.init(); // Mod registieren
    }
}