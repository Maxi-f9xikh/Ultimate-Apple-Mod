package de.maxi.ultimate_apple_mod.fabric;

import de.maxi.ultimate_apple_mod.ModRegistries;
import de.maxi.ultimate_apple_mod.block.MixerMenu;
import de.maxi.ultimate_apple_mod.effect.CurseOfRotten;
import de.maxi.ultimate_apple_mod.effect.LifestealEffect;
import de.maxi.ultimate_apple_mod.effect.MoonGravityEffect;
import de.maxi.ultimate_apple_mod.effect.TimeFreezeEffect;
import de.maxi.ultimate_apple_mod.effect.TotemProtectionEffect;
import de.maxi.ultimate_apple_mod.fabric.block.ModBlocks;
import de.maxi.ultimate_apple_mod.fabric.event.FabricEventRegistrar;
import de.maxi.ultimate_apple_mod.fabric.network.FabricNetworkHandler;
import de.maxi.ultimate_apple_mod.item.*;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ultimate_apple_modFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ultimate_apple_mod.init();

        // ── 1. Effects (must be before items that reference them) ─────────────
        MobEffect curseOfRotten = Registry.register(BuiltInRegistries.MOB_EFFECT,
            rl("curse_of_rotten"), new CurseOfRotten());
        MobEffect moonGravity = Registry.register(BuiltInRegistries.MOB_EFFECT,
            rl("moon_gravity"), new MoonGravityEffect());
        MobEffect lifesteal = Registry.register(BuiltInRegistries.MOB_EFFECT,
            rl("lifesteal"), new LifestealEffect());
        MobEffect totemProtection = Registry.register(BuiltInRegistries.MOB_EFFECT,
            rl("totem_protection"), new TotemProtectionEffect());
        MobEffect timeFreeze = Registry.register(BuiltInRegistries.MOB_EFFECT,
            rl("time_freeze"), new TimeFreezeEffect());

        ModRegistries.CURSE_OF_ROTTEN  = () -> curseOfRotten;
        ModRegistries.MOON_GRAVITY     = () -> moonGravity;
        ModRegistries.LIFESTEAL        = () -> lifesteal;
        ModRegistries.TOTEM_PROTECTION = () -> totemProtection;
        ModRegistries.TIME_FREEZE      = () -> timeFreeze;

        // ── 2. Items ──────────────────────────────────────────────────────────
        Item diamondApple = reg("diamond_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(8).saturationMod(0.9f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 20*60, 2), 1f)
                .effect(new MobEffectInstance(MobEffects.REGENERATION, 20*20, 1), 1f)
                .effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20*30, 1), 1f)
                .effect(new MobEffectInstance(MobEffects.ABSORPTION, 20*60, 0), 1f).build())
            .stacksTo(64)));
        Item lapislazuliApple = reg("lapislazuli_apple", new LapislazuliAppleItem());
        Item emeraldApple = reg("emerald_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(8).saturationMod(0.9f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.LUCK, 20*60, 1), 1f)
                .effect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20*30, 0), 1f).build())
            .stacksTo(64)));
        Item redstoneApple = reg("redstone_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(8).saturationMod(0.9f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20*20, 2), 1f)
                .effect(new MobEffectInstance(MobEffects.DIG_SPEED, 20*20, 2), 1f)
                .effect(new MobEffectInstance(MobEffects.GLOWING, 20*20, 0), 1f)
                .effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20*15, 0), 1f).build())
            .stacksTo(64)));
        Item netheriteApple = reg("netherite_apple", new Item(new Item.Properties().fireResistant()
            .food(new FoodProperties.Builder().nutrition(10).saturationMod(1.0f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20*120, 2), 1f)
                .effect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20*120, 0), 1f)
                .effect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 20*120, 3), 1f)
                .effect(new MobEffectInstance(MobEffects.REGENERATION, 20*30, 2), 1f)
                .effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20*30, 1), 1f)
                .effect(new MobEffectInstance(MobEffects.ABSORPTION, 20*120, 2), 1f).build())
            .stacksTo(64)));
        Item ironApple = reg("iron_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(6).saturationMod(0.7f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 20*30, 0), 1f)
                .effect(new MobEffectInstance(MobEffects.REGENERATION, 20*10, 0), 1f)
                .effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20*15, 0), 1f).build())
            .stacksTo(64)));
        Item rottenApple = reg("rotten_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.1f).alwaysEat()
                .effect(new MobEffectInstance(curseOfRotten, 400, 0, false, true), 1f)
                .effect(new MobEffectInstance(MobEffects.CONFUSION, 400, 0), 1f).build())
            .stacksTo(64)));
        Item roastedApple = reg("roasted_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.1f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 20*20, 0), 1f)
                .effect(new MobEffectInstance(MobEffects.SATURATION, 20*10, 0), 1f).build())
            .stacksTo(64)));
        Item bakedApple = reg("baked_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.1f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.REGENERATION, 20*5, 0), 1f).build())
            .stacksTo(64)));
        Item burntApple = reg("burnt_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.1f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.HUNGER, 20*15, 1), 1f)
                .effect(new MobEffectInstance(MobEffects.CONFUSION, 20*5, 0), 1f)
                .effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20*5, 0), 1f)
                .effect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20*15, 0), 1f).build())
            .stacksTo(64)));
        Item blazeApple = reg("blaze_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(6).saturationMod(0.5f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.REGENERATION, 20*5, 0), 1f)
                .effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20*5, 0), 1f)
                .effect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20*15, 0), 1f).build())
            .stacksTo(64)));
        Item birne = reg("pear_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.1f)
                .effect(new MobEffectInstance(MobEffects.REGENERATION, 20*15, 0), 1f)
                .effect(new MobEffectInstance(MobEffects.SATURATION, 20*5, 0), 1f).build())
            .stacksTo(64)));
        Item copperApple = reg("copper_apple", new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(5).saturationMod(0.6f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.DIG_SPEED, 20*25, 1), 1f)
                .effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20*25, 0), 1f)
                .effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20*25, 0), 1f).build())
            .stacksTo(64), 0, false));
        Item exposedCopperApple = reg("exposed_copper_apple", new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.5f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.DIG_SPEED, 20*20, 0), 1f)
                .effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20*15, 0), 1f).build())
            .stacksTo(64), 1, false));
        Item weatheredCopperApple = reg("weathered_copper_apple", new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(3).saturationMod(0.3f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.DIG_SPEED, 20*10, 0), 1f).build())
            .stacksTo(64), 2, false));
        Item oxidizedCopperApple = reg("oxidized_copper_apple", new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.1f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20*5, 0), 1f)
                .effect(new MobEffectInstance(MobEffects.WEAKNESS, 20*5, 0), 1f).build())
            .stacksTo(64), 3, false));
        Item waxedCopperApple = reg("waxed_copper_apple", new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(5).saturationMod(0.6f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.DIG_SPEED, 20*25, 1), 1f)
                .effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20*25, 0), 1f)
                .effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20*25, 0), 1f).build())
            .stacksTo(64), 0, true));
        Item waxedExposedCopperApple = reg("waxed_exposed_copper_apple", new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.5f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.DIG_SPEED, 20*20, 0), 1f)
                .effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20*15, 0), 1f).build())
            .stacksTo(64), 1, true));
        Item waxedWeatheredCopperApple = reg("waxed_weathered_copper_apple", new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(3).saturationMod(0.3f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.DIG_SPEED, 20*10, 0), 1f).build())
            .stacksTo(64), 2, true));
        Item waxedOxidizedCopperApple = reg("waxed_oxidized_copper_apple", new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.1f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20*5, 0), 1f)
                .effect(new MobEffectInstance(MobEffects.WEAKNESS, 20*5, 0), 1f).build())
            .stacksTo(64), 3, true));
        Item enderPearlApple = reg("ender_pearl_apple", new EnderPearlAppleItem());
        Item moonApple = reg("moon_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(6).saturationMod(0.6f).alwaysEat()
                .effect(new MobEffectInstance(moonGravity, 20*30, 0), 1f).build())
            .stacksTo(64)));
        Item orchardApple = reg("orchard_apple", new OrchardCallerItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.4f).alwaysEat().build())
            .stacksTo(64)));
        Item echoApple = reg("echo_apple", new EchoAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(5).saturationMod(0.5f).alwaysEat().build())
            .stacksTo(64)));
        Item rewindApple = reg("rewind_apple", new RewindAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.3f).alwaysEat().build())
            .stacksTo(64)));
        Item appleBomb = reg("apple_bomb", new AppleBombItem(new Item.Properties().stacksTo(16)));
        Item coalApple = reg("coal_apple", new CoalAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.0f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.HUNGER, 20*30, 2), 1f)
                .effect(new MobEffectInstance(MobEffects.CONFUSION, 20*10, 0), 1f)
                .effect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20*15, 1), 1f)
                .effect(new MobEffectInstance(MobEffects.BLINDNESS, 20*5, 0), 1f).build())
            .stacksTo(64)));
        Item tntApple = reg("tnt_apple", new TntAppleItem(new Item.Properties().stacksTo(16)));
        Item nuclearApple = reg("nuclear_apple",
            new NuclearAppleItem(new Item.Properties().stacksTo(1).fireResistant()));
        Item witherApple = reg("wither_apple", new WitherAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(6).saturationMod(0.6f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.ABSORPTION, 20*30, 2), 1f)
                .effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20*10, 1), 1f)
                .effect(new MobEffectInstance(MobEffects.REGENERATION, 20*5, 1), 1f).build())
            .stacksTo(64)));
        Item honeyApple = reg("honey_apple", new HoneyAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.6f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20*5, 0), 1f).build())
            .stacksTo(64)));
        Item dragonApple = reg("dragon_apple", new DragonAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(8).saturationMod(0.8f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.ABSORPTION, 20*10, 3), 1f)
                .effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20*10, 1), 1f)
                .effect(new MobEffectInstance(MobEffects.REGENERATION, 20*10, 2), 1f).build())
            .stacksTo(64)));
        Item netherStarApple = reg("nether_star_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(10).saturationMod(1.0f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 4), 1f)
                .effect(new MobEffectInstance(MobEffects.ABSORPTION, 20*30, 3), 1f)
                .effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20*10, 2), 1f)
                .effect(new MobEffectInstance(MobEffects.REGENERATION, 20*5, 3), 1f).build())
            .stacksTo(1)));
        Item dirtApple = reg("dirt_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.0f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.HUNGER, 20*30, 2), 1f)
                .effect(new MobEffectInstance(MobEffects.CONFUSION, 20*10, 0), 1f).build())
            .stacksTo(64)));
        Item totemApple  = reg("totem_apple",  new TotemAppleItem());
        Item quantumApple = reg("quantum_apple", new QuantumAppleItem());
        Item voidApple   = reg("void_apple",   new VoidAppleItem());
        Item timeFreezeApple = reg("time_freeze_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.4f).alwaysEat()
                .effect(new MobEffectInstance(timeFreeze, 20*30, 0), 1f).build())
            .stacksTo(64)));
        Item longevityApple = reg("longevity_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(6).saturationMod(0.6f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.ABSORPTION, 20*120, 3), 1f)
                .effect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 20*60, 0), 1f)
                .effect(new MobEffectInstance(MobEffects.REGENERATION, 20*15, 0), 1f).build())
            .stacksTo(64)));
        Item prismApple  = reg("prism_apple",  new PrismAppleItem());
        Item banana = reg("banana", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.5f).build())
            .stacksTo(64)));
        Item cupItem   = reg("cup",   new CupItem());
        Item shakeItem = reg("shake", new ShakeItem());

        // ── 3. Entity types ───────────────────────────────────────────────────
        EntityType<AppleBombEntity> appleBombEntity = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            rl("apple_bomb"),
            EntityType.Builder.<AppleBombEntity>of(AppleBombEntity::new, MobCategory.MISC)
                .sized(0.25f, 0.25f).clientTrackingRange(4).build("apple_bomb"));
        EntityType<ShakeBombEntity> shakeBombEntity = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            rl("shake_bomb"),
            EntityType.Builder.<ShakeBombEntity>of(ShakeBombEntity::new, MobCategory.MISC)
                .sized(0.25f, 0.25f).clientTrackingRange(4).build("shake_bomb"));
        EntityType<TntAppleEntity> tntAppleEntity = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            rl("tnt_apple"),
            EntityType.Builder.<TntAppleEntity>of(TntAppleEntity::new, MobCategory.MISC)
                .sized(0.25f, 0.25f).clientTrackingRange(4).build("tnt_apple"));
        EntityType<NuclearAppleEntity> nuclearAppleEntity = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            rl("nuclear_apple"),
            EntityType.Builder.<NuclearAppleEntity>of(NuclearAppleEntity::new, MobCategory.MISC)
                .sized(0.25f, 0.25f).clientTrackingRange(8).build("nuclear_apple"));

        // ── 4. Block + BE + Menu ──────────────────────────────────────────────
        ModBlocks.register();

        BlockEntityType<de.maxi.ultimate_apple_mod.fabric.block.MixerBlockEntity> mixerBE =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, rl("mixer"),
                BlockEntityType.Builder.of(
                    de.maxi.ultimate_apple_mod.fabric.block.MixerBlockEntity::new,
                    ModBlocks.MIXER).build(null));
        MenuType<MixerMenu> mixerMenu = Registry.register(BuiltInRegistries.MENU,
            rl("mixer"),
            new MenuType<>((id, inv) -> new MixerMenu(id, inv,
                new net.minecraft.world.SimpleContainer(4),
                new net.minecraft.world.inventory.SimpleContainerData(2))));

        // ── 5. Fill ModRegistries ─────────────────────────────────────────────
        ModRegistries.DIAMOND_APPLE                = () -> diamondApple;
        ModRegistries.LAPISLAZULI_APPLE            = () -> lapislazuliApple;
        ModRegistries.EMERALD_APPLE                = () -> emeraldApple;
        ModRegistries.REDSTONE_APPLE               = () -> redstoneApple;
        ModRegistries.NETHERITE_APPLE              = () -> netheriteApple;
        ModRegistries.IRON_APPLE                   = () -> ironApple;
        ModRegistries.ROTTEN_APPLE                 = () -> rottenApple;
        ModRegistries.ROASTED_APPLE                = () -> roastedApple;
        ModRegistries.BAKED_APPLE                  = () -> bakedApple;
        ModRegistries.BURNT_APPLE                  = () -> burntApple;
        ModRegistries.BLAZE_APPLE                  = () -> blazeApple;
        ModRegistries.BIRNE                        = () -> birne;
        ModRegistries.COPPER_APPLE                 = () -> copperApple;
        ModRegistries.EXPOSED_COPPER_APPLE         = () -> exposedCopperApple;
        ModRegistries.WEATHERED_COPPER_APPLE       = () -> weatheredCopperApple;
        ModRegistries.OXIDIZED_COPPER_APPLE        = () -> oxidizedCopperApple;
        ModRegistries.WAXED_COPPER_APPLE           = () -> waxedCopperApple;
        ModRegistries.WAXED_EXPOSED_COPPER_APPLE   = () -> waxedExposedCopperApple;
        ModRegistries.WAXED_WEATHERED_COPPER_APPLE = () -> waxedWeatheredCopperApple;
        ModRegistries.WAXED_OXIDIZED_COPPER_APPLE  = () -> waxedOxidizedCopperApple;
        ModRegistries.ENDER_PEARL_APPLE            = () -> enderPearlApple;
        ModRegistries.MOON_APPLE                   = () -> moonApple;
        ModRegistries.ORCHARD_APPLE                = () -> orchardApple;
        ModRegistries.ECHO_APPLE                   = () -> echoApple;
        ModRegistries.REWIND_APPLE                 = () -> rewindApple;
        ModRegistries.APPLE_BOMB                   = () -> appleBomb;
        ModRegistries.COAL_APPLE                   = () -> coalApple;
        ModRegistries.TNT_APPLE                    = () -> tntApple;
        ModRegistries.NUCLEAR_APPLE                = () -> nuclearApple;
        ModRegistries.WITHER_APPLE                 = () -> witherApple;
        ModRegistries.HONEY_APPLE                  = () -> honeyApple;
        ModRegistries.DRAGON_APPLE                 = () -> dragonApple;
        ModRegistries.NETHER_STAR_APPLE            = () -> netherStarApple;
        ModRegistries.DIRT_APPLE                   = () -> dirtApple;
        ModRegistries.TOTEM_APPLE                  = () -> totemApple;
        ModRegistries.QUANTUM_APPLE                = () -> quantumApple;
        ModRegistries.VOID_APPLE                   = () -> voidApple;
        ModRegistries.TIME_FREEZE_APPLE            = () -> timeFreezeApple;
        ModRegistries.LONGEVITY_APPLE              = () -> longevityApple;
        ModRegistries.PRISM_APPLE                  = () -> prismApple;
        ModRegistries.BANANA                       = () -> banana;
        ModRegistries.CUP_ITEM                     = () -> cupItem;
        ModRegistries.SHAKE_ITEM                   = () -> shakeItem;
        ModRegistries.APPLE_BOMB_ENTITY            = () -> appleBombEntity;
        ModRegistries.SHAKE_BOMB_ENTITY            = () -> shakeBombEntity;
        ModRegistries.TNT_APPLE_ENTITY             = () -> tntAppleEntity;
        ModRegistries.NUCLEAR_APPLE_ENTITY         = () -> nuclearAppleEntity;
        ModRegistries.MIXER                        = () -> ModBlocks.MIXER;
        ModRegistries.MIXER_ITEM                   = () -> ModBlocks.MIXER_ITEM;
        ModRegistries.MIXER_BLOCK_ENTITY           = () -> mixerBE;
        ModRegistries.MIXER_MENU_TYPE              = () -> mixerMenu;

        // ── 6. Composter values ───────────────────────────────────────────────
        ComposterBlock.COMPOSTABLES.put(rottenApple,        0.30f);
        ComposterBlock.COMPOSTABLES.put(burntApple,         0.30f);
        ComposterBlock.COMPOSTABLES.put(dirtApple,          0.30f);
        ComposterBlock.COMPOSTABLES.put(tntApple,           0.30f);
        ComposterBlock.COMPOSTABLES.put(coalApple,          0.30f);
        ComposterBlock.COMPOSTABLES.put(bakedApple,         0.65f);
        ComposterBlock.COMPOSTABLES.put(roastedApple,       0.65f);
        ComposterBlock.COMPOSTABLES.put(birne,              0.65f);
        ComposterBlock.COMPOSTABLES.put(banana,             0.65f);
        ComposterBlock.COMPOSTABLES.put(blazeApple,         0.65f);
        ComposterBlock.COMPOSTABLES.put(honeyApple,         0.65f);
        ComposterBlock.COMPOSTABLES.put(moonApple,          0.65f);
        ComposterBlock.COMPOSTABLES.put(ironApple,          0.65f);
        ComposterBlock.COMPOSTABLES.put(copperApple,        0.65f);
        ComposterBlock.COMPOSTABLES.put(exposedCopperApple, 0.65f);
        ComposterBlock.COMPOSTABLES.put(weatheredCopperApple, 0.65f);
        ComposterBlock.COMPOSTABLES.put(oxidizedCopperApple,  0.65f);
        ComposterBlock.COMPOSTABLES.put(waxedCopperApple,         0.65f);
        ComposterBlock.COMPOSTABLES.put(waxedExposedCopperApple,  0.65f);
        ComposterBlock.COMPOSTABLES.put(waxedWeatheredCopperApple,0.65f);
        ComposterBlock.COMPOSTABLES.put(waxedOxidizedCopperApple, 0.65f);
        ComposterBlock.COMPOSTABLES.put(lapislazuliApple,   0.65f);
        ComposterBlock.COMPOSTABLES.put(redstoneApple,      0.65f);
        ComposterBlock.COMPOSTABLES.put(emeraldApple,       0.65f);
        ComposterBlock.COMPOSTABLES.put(diamondApple,       0.65f);
        ComposterBlock.COMPOSTABLES.put(netheriteApple,     0.65f);
        ComposterBlock.COMPOSTABLES.put(prismApple,         0.65f);
        ComposterBlock.COMPOSTABLES.put(timeFreezeApple,    0.65f);
        ComposterBlock.COMPOSTABLES.put(voidApple,          0.65f);
        ComposterBlock.COMPOSTABLES.put(enderPearlApple,    0.65f);
        ComposterBlock.COMPOSTABLES.put(echoApple,          0.65f);
        ComposterBlock.COMPOSTABLES.put(rewindApple,        0.65f);
        ComposterBlock.COMPOSTABLES.put(longevityApple,     0.65f);
        ComposterBlock.COMPOSTABLES.put(quantumApple,       0.65f);
        ComposterBlock.COMPOSTABLES.put(witherApple,        0.65f);
        ComposterBlock.COMPOSTABLES.put(dragonApple,        0.65f);
        ComposterBlock.COMPOSTABLES.put(totemApple,         0.65f);
        ComposterBlock.COMPOSTABLES.put(orchardApple,       0.85f);

        // ── 7. Creative tab ───────────────────────────────────────────────────
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, rl("ultimate_tab"),
            FabricItemGroup.builder()
                .title(Component.literal("Ultimate Apple Mod"))
                .icon(() -> new ItemStack(diamondApple))
                .displayItems((params, output) -> {
                    output.accept(lapislazuliApple); output.accept(coalApple);
                    output.accept(copperApple); output.accept(exposedCopperApple);
                    output.accept(weatheredCopperApple); output.accept(oxidizedCopperApple);
                    output.accept(waxedCopperApple); output.accept(waxedExposedCopperApple);
                    output.accept(waxedWeatheredCopperApple); output.accept(waxedOxidizedCopperApple);
                    output.accept(redstoneApple); output.accept(ironApple); output.accept(diamondApple);
                    output.accept(netheriteApple); output.accept(emeraldApple); output.accept(rottenApple);
                    output.accept(roastedApple); output.accept(bakedApple); output.accept(burntApple);
                    output.accept(birne); output.accept(blazeApple); output.accept(enderPearlApple);
                    output.accept(moonApple); output.accept(orchardApple); output.accept(echoApple);
                    output.accept(rewindApple); output.accept(appleBomb); output.accept(tntApple);
                    output.accept(nuclearApple); output.accept(witherApple); output.accept(honeyApple);
                    output.accept(dragonApple); output.accept(netherStarApple); output.accept(dirtApple);
                    output.accept(totemApple); output.accept(quantumApple); output.accept(voidApple);
                    output.accept(timeFreezeApple); output.accept(longevityApple); output.accept(prismApple);
                    output.accept(banana); output.accept(cupItem); output.accept(shakeItem);
                    output.accept(ModBlocks.MIXER_ITEM);
                })
                .build());

        // ── 8. Register events + network ─────────────────────────────────────
        FabricEventRegistrar.register();
        FabricNetworkHandler.registerServer();
    }

    private static Item reg(String id, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, rl(id), item);
    }

    private static ResourceLocation rl(String path) {
        return new ResourceLocation(ultimate_apple_mod.MOD_ID, path);
    }
}
