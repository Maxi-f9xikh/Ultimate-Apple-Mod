# Phase 1 — NeoForge + Quilt for 1.20.1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add NeoForge and Quilt as build targets to the existing 1.20.1 project, producing two new distributable jars with identical gameplay to the Forge/Fabric builds.

**Architecture:** NeoForge subproject is a near-clone of `forge/` with `net.minecraftforge.*` → `net.neoforged.*` package renames and one API rename (`IForgeMenuType` → `IMenuTypeExtension`). Quilt subproject is 4 source files that delegate entirely to the existing `fabric/` module — Quilt runs Fabric mods via `quilted-fabric-api`.

**Tech Stack:** Architectury Loom 1.10-SNAPSHOT, Architectury Plugin 3.4-SNAPSHOT, NeoForge 1.20.1-47.1.104, Quilt Loader 0.26.4, Quilted Fabric API 7.6.0+0.92.2-1.20.1

---

## File Map

**Modified:**
- `settings.gradle` — add `neoforge`, `quilt` subprojects + maven repos
- `gradle.properties` — add NeoForge + Quilt version properties

**NeoForge (new — all under `neoforge/src/main/java/de/maxi/ultimate_apple_mod/neoforge/`):**
- `ultimate_apple_modNeoForge.java` — main mod class, all registrations
- `ModClient.java` — client-side setup (key bindings, entity renderers, screen)
- `ModRecipes.java` — recipe serializer DeferredRegister
- `block/MixerBlockEntity.java` — thin wrapper delegating to common base
- `block/ModBlocks.java` — Mixer block + item DeferredRegisters
- `event/ClientEventHandler.java` — tooltip events
- `event/ClientPlayerRenderHandler.java` — rotten apple render/hitbox
- `event/DecayEventHandler.java` — vanilla apple decay
- `event/KeyInputHandler.java` — dragon breath key polling
- `event/LootTableHandler.java` — loot table injections
- `event/MobDropEventHandler.java` — Wither/Evoker special drops
- `event/PlayerEffectEventHandler.java` — totem death cancel, lifesteal
- `event/RewindTracker.java` — rewind position recording
- `event/TntAppleEventHandler.java` — ghast kill advancement
- `event/babyzombiedroppt.java` — baby zombie rotten apple drop
- `network/NetworkHandler.java` — SimpleChannel registration
- `network/FireDragonBreathPacket.java` — dragon breath packet

**NeoForge resources (new):**
- `neoforge/src/main/resources/META-INF/mods.toml`
- `neoforge/src/main/resources/META-INF/logo.png` (copy from forge)
- `neoforge/build.gradle`

**Quilt (new):**
- `quilt/build.gradle`
- `quilt/src/main/resources/quilt.mod.json`
- `quilt/src/main/java/.../quilt/ultimate_apple_modQuilt.java`
- `quilt/src/main/java/.../quilt/QuiltModClient.java`

---

## Task 1: Update root build files

**Files:**
- Modify: `settings.gradle`
- Modify: `gradle.properties`

- [ ] **Step 1: Add NeoForge and Quilt to settings.gradle**

Replace the entire `settings.gradle` with:

```gradle
pluginManagement {
    repositories {
        maven { url "https://maven.fabricmc.net/" }
        maven { url "https://maven.architectury.dev/" }
        maven { url "https://files.minecraftforge.net/maven/" }
        maven { url "https://maven.neoforged.net/releases/" }
        maven { url "https://maven.quiltmc.org/repository/release/" }
        gradlePluginPortal()
    }
}

rootProject.name = 'ultimate_apple_mod'

include 'common'
include 'fabric'
include 'forge'
include 'neoforge'
include 'quilt'
```

- [ ] **Step 2: Add version properties to gradle.properties**

Add these three lines to the end of `gradle.properties`:

```properties
neoforge_version = 1.20.1-47.1.104
quilt_loader_version = 0.26.4
quilted_fabric_api_version = 7.6.0+0.92.2-1.20.1
```

> If the build fails later with "could not find NeoForge 1.20.1-47.1.104", check https://projects.neoforged.net/neoforged/neoforge for the actual latest 1.20.1 build and update accordingly. Same for Quilt versions at https://modrinth.com/mod/quilt-standard-libraries.

- [ ] **Step 3: Verify settings syntax**

```
Run: .\gradlew.bat projects
Expected: Output lists common, fabric, forge, neoforge, quilt under root project.
         (neoforge and quilt will warn "project not found" until their build.gradle exists — that's OK)
```

---

## Task 2: NeoForge build.gradle

**Files:**
- Create: `neoforge/build.gradle`

- [ ] **Step 1: Create neoforge/build.gradle**

```gradle
plugins {
    id 'com.github.johnrengelman.shadow'
}

loom {
    neoForge {
        mixinConfig "ultimate_apple_mod.mixins.json"
    }
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

configurations {
    common {
        canBeResolved = true
        canBeConsumed = false
    }
    compileClasspath.extendsFrom common
    runtimeClasspath.extendsFrom common
    developmentNeoForge.extendsFrom common

    shadowBundle {
        canBeResolved = true
        canBeConsumed = false
    }
}

dependencies {
    neoForge "net.neoforged:neoforge:$rootProject.neoforge_version"
    modImplementation "dev.architectury:architectury-neoforge:$rootProject.architectury_api_version"

    common(project(path: ':common', configuration: 'namedElements')) { transitive false }
    shadowBundle project(path: ':common', configuration: 'namedElements')
    implementation project(":common")

    // JEI compile-only (same versions as Forge build)
    compileOnly "mezz.jei:jei-1.20.1-common-api:15.3.0.4"
    compileOnly "mezz.jei:jei-1.20.1-neoforge-api:15.3.0.4"
}

processResources {
    inputs.property 'version', project.version

    filesMatching('META-INF/mods.toml') {
        expand version: project.version
    }
}

shadowJar {
    configurations = [project.configurations.shadowBundle]
    archiveClassifier = 'dev-shadow'
}

remapJar {
    input.set shadowJar.archiveFile
}

jar {
    from("src/main/resources/META-INF/logo.png") {
        into "META-INF"
    }
}
```

> Note: `jei-1.20.1-neoforge-api` may not exist on BlameJared's maven. If the build fails on that line, remove it (JEI NeoForge API was not separate from common API in early 1.20.1 NeoForge builds) and keep only `jei-1.20.1-common-api`.

---

## Task 3: NeoForge mods.toml

**Files:**
- Create: `neoforge/src/main/resources/META-INF/mods.toml`
- Copy: `neoforge/src/main/resources/META-INF/logo.png` from `forge/src/main/resources/META-INF/logo.png`

- [ ] **Step 1: Create mods.toml**

```toml
modLoader="javafml"
loaderVersion = "[47,)"
license = "Internet Systems Consortium (ISC) License"

[[mods]]
modId = "ultimate_apple_mod"
version = "${file.jarVersion}"
displayName = "Ultimate Apple Mod"
authors = "Maxi"
description = """
This mod adds new types of special apples, from ore apples to lucky blocks or apples with effects.
Using a special table, you can craft an enchantment template apple with an item to apply a specific enchantment to the apple.
You can combine the apple with a potion, meaning eating it will grant the effect, and much more.
"""

logoFile="logo.png"

[[dependencies.ultimate_apple_mod]]
modId = "neoforge"
mandatory = true
versionRange = "[47,)"
ordering = "NONE"
side = "BOTH"

[[dependencies.ultimate_apple_mod]]
modId = "minecraft"
mandatory = true
versionRange = "[1.20.1,)"
ordering = "NONE"
side = "BOTH"

[[dependencies.ultimate_apple_mod]]
modId = "architectury"
mandatory = true
versionRange = "[9.2.14,)"
ordering = "AFTER"
side = "BOTH"

[[mixins]]
config="ultimate_apple_mod.mixins.json"
```

- [ ] **Step 2: Copy logo.png**

```
Run (PowerShell): Copy-Item "forge\src\main\resources\META-INF\logo.png" -Destination "neoforge\src\main\resources\META-INF\logo.png" -Force
Expected: No error output.
```

---

## Task 4: NeoForge main initializer

**Files:**
- Create: `neoforge/src/main/java/de/maxi/ultimate_apple_mod/neoforge/ultimate_apple_modNeoForge.java`

This is the largest file — identical to the Forge version with three changes:
1. Package + class name
2. All `net.minecraftforge.*` imports → `net.neoforged.*`
3. `IForgeMenuType.create` → `IMenuTypeExtension.create`

- [ ] **Step 1: Create ultimate_apple_modNeoForge.java**

```java
package de.maxi.ultimate_apple_mod.neoforge;

import de.maxi.ultimate_apple_mod.effect.CurseOfRotten;
import de.maxi.ultimate_apple_mod.effect.LifestealEffect;
import de.maxi.ultimate_apple_mod.effect.MoonGravityEffect;
import de.maxi.ultimate_apple_mod.effect.TimeFreezeEffect;
import de.maxi.ultimate_apple_mod.effect.TotemProtectionEffect;
import de.maxi.ultimate_apple_mod.item.LapislazuliAppleItem;
import de.maxi.ultimate_apple_mod.item.PrismAppleItem;
import de.maxi.ultimate_apple_mod.item.QuantumAppleItem;
import de.maxi.ultimate_apple_mod.item.TotemAppleItem;
import de.maxi.ultimate_apple_mod.item.VoidAppleItem;
import de.maxi.ultimate_apple_mod.ModRegistries;
import de.maxi.ultimate_apple_mod.block.MixerMenu;
import de.maxi.ultimate_apple_mod.neoforge.block.MixerBlockEntity;
import de.maxi.ultimate_apple_mod.neoforge.block.ModBlocks;
import de.maxi.ultimate_apple_mod.neoforge.network.NetworkHandler;
import de.maxi.ultimate_apple_mod.item.AppleBombEntity;
import de.maxi.ultimate_apple_mod.item.AppleBombItem;
import de.maxi.ultimate_apple_mod.item.CoalAppleItem;
import de.maxi.ultimate_apple_mod.item.CopperAppleItem;
import de.maxi.ultimate_apple_mod.item.NuclearAppleEntity;
import de.maxi.ultimate_apple_mod.item.NuclearAppleItem;
import de.maxi.ultimate_apple_mod.item.TntAppleEntity;
import de.maxi.ultimate_apple_mod.item.TntAppleItem;
import de.maxi.ultimate_apple_mod.item.ShakeBombEntity;
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
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

@Mod(ultimate_apple_mod.MOD_ID)
public final class ultimate_apple_modNeoForge {

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

    public static final RegistryObject<MobEffect> MOON_GRAVITY_EFFECT =
        EFFECTS.register("moon_gravity", MoonGravityEffect::new);

    public static final RegistryObject<MobEffect> LIFESTEAL_EFFECT =
        EFFECTS.register("lifesteal", LifestealEffect::new);

    public static final RegistryObject<MobEffect> TOTEM_PROTECTION_EFFECT =
        EFFECTS.register("totem_protection", TotemProtectionEffect::new);

    public static final RegistryObject<MobEffect> TIME_FREEZE_EFFECT =
        EFFECTS.register("time_freeze", TimeFreezeEffect::new);

    // ── Menu Types ────────────────────────────────────────────────────────────

    public static final RegistryObject<MenuType<MixerMenu>> MIXER_MENU_TYPE =
        MENUS.register("mixer", () -> IMenuTypeExtension.create(MixerMenu::new));

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

    public static final RegistryObject<EntityType<ShakeBombEntity>> SHAKE_BOMB_ENTITY =
        ENTITY_TYPES.register("shake_bomb",
            () -> EntityType.Builder.<ShakeBombEntity>of(ShakeBombEntity::new, MobCategory.MISC)
                .sized(0.25f, 0.25f)
                .clientTrackingRange(4)
                .build("shake_bomb"));

    public static final RegistryObject<EntityType<TntAppleEntity>> TNT_APPLE_ENTITY =
        ENTITY_TYPES.register("tnt_apple",
            () -> EntityType.Builder.<TntAppleEntity>of(TntAppleEntity::new, MobCategory.MISC)
                .sized(0.25f, 0.25f)
                .clientTrackingRange(4)
                .build("tnt_apple"));

    public static final RegistryObject<EntityType<NuclearAppleEntity>> NUCLEAR_APPLE_ENTITY =
        ENTITY_TYPES.register("nuclear_apple",
            () -> EntityType.Builder.<NuclearAppleEntity>of(NuclearAppleEntity::new, MobCategory.MISC)
                .sized(0.25f, 0.25f)
                .clientTrackingRange(8)
                .build("nuclear_apple"));

    // ── Items ────────────────────────────────────────────────────────────────

    public static final RegistryObject<Item> DIAMOND_APPLE = ITEMS.register("diamond_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(8).saturationMod(0.9f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST,      20 * 60, 2), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION,      20 * 20, 1), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 30, 1), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION,        20 * 60, 0), 1.0f)
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
                .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 20, 2), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED,      20 * 20, 2), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.GLOWING,        20 * 20, 0), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST,   20 * 15, 0), 1.0f)
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> NETHERITE_APPLE = ITEMS.register("netherite_apple", () ->
        new Item(new Item.Properties()
            .fireResistant()
            .food(new FoodProperties.Builder()
                .nutrition(10).saturationMod(1.0f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 120, 2), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE,   20 * 120, 0), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST,      20 * 120, 3), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION,      20 *  30, 2), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST,      20 *  30, 1), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION,        20 * 120, 2), 1.0f)
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> IRON_APPLE = ITEMS.register("iron_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(6).saturationMod(0.7f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST,      20 * 30, 0), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION,      20 * 10, 0), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 15, 0), 1.0f)
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> ROTTEN_APPLE = ITEMS.register("rotten_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(2).saturationMod(0.1f).alwaysEat()
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

    public static final RegistryObject<Item> BIRNE = ITEMS.register("pear_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(1).saturationMod(0.1f)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20 * 15, 0), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.SATURATION, 20 * 5, 0), 1.0f)
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> COPPER_APPLE = ITEMS.register("copper_apple", () ->
        new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(5).saturationMod(0.6f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED,         20 * 25, 1), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST,       20 * 25, 0), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,  20 * 25, 0), 1.0f)
                .build())
            .stacksTo(64), 0, false));

    public static final RegistryObject<Item> EXPOSED_COPPER_APPLE = ITEMS.register("exposed_copper_apple", () ->
        new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(4).saturationMod(0.5f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED,    20 * 20, 0), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 15, 0), 1.0f)
                .build())
            .stacksTo(64), 1, false));

    public static final RegistryObject<Item> WEATHERED_COPPER_APPLE = ITEMS.register("weathered_copper_apple", () ->
        new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(3).saturationMod(0.3f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 20 * 10, 0), 1.0f)
                .build())
            .stacksTo(64), 2, false));

    public static final RegistryObject<Item> OXIDIZED_COPPER_APPLE = ITEMS.register("oxidized_copper_apple", () ->
        new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(2).saturationMod(0.1f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 5, 0), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS,          20 * 5, 0), 1.0f)
                .build())
            .stacksTo(64), 3, false));

    public static final RegistryObject<Item> WAXED_COPPER_APPLE = ITEMS.register("waxed_copper_apple", () ->
        new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(5).saturationMod(0.6f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED,         20 * 25, 1), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST,       20 * 25, 0), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,  20 * 25, 0), 1.0f)
                .build())
            .stacksTo(64), 0, true));

    public static final RegistryObject<Item> WAXED_EXPOSED_COPPER_APPLE = ITEMS.register("waxed_exposed_copper_apple", () ->
        new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(4).saturationMod(0.5f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED,    20 * 20, 0), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 15, 0), 1.0f)
                .build())
            .stacksTo(64), 1, true));

    public static final RegistryObject<Item> WAXED_WEATHERED_COPPER_APPLE = ITEMS.register("waxed_weathered_copper_apple", () ->
        new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(3).saturationMod(0.3f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 20 * 10, 0), 1.0f)
                .build())
            .stacksTo(64), 2, true));

    public static final RegistryObject<Item> WAXED_OXIDIZED_COPPER_APPLE = ITEMS.register("waxed_oxidized_copper_apple", () ->
        new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(2).saturationMod(0.1f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 5, 0), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS,          20 * 5, 0), 1.0f)
                .build())
            .stacksTo(64), 3, true));

    public static final RegistryObject<Item> ENDER_PEARL_APPLE =
        ITEMS.register("ender_pearl_apple", EnderPearlAppleItem::new);

    public static final RegistryObject<Item> MOON_APPLE = ITEMS.register("moon_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(6).saturationMod(0.6f).alwaysEat()
                .effect(() -> new MobEffectInstance(MOON_GRAVITY_EFFECT.get(), 20 * 30, 0), 1.0f)
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

    public static final RegistryObject<Item> COAL_APPLE =
        ITEMS.register("coal_apple", () ->
            new CoalAppleItem(new Item.Properties()
                .food(new FoodProperties.Builder()
                    .nutrition(2).saturationMod(0.0f).alwaysEat()
                    .effect(() -> new MobEffectInstance(MobEffects.HUNGER,    20 * 30, 2), 1.0f)
                    .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 20 * 10, 0), 1.0f)
                    .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 15, 1), 1.0f)
                    .effect(() -> new MobEffectInstance(MobEffects.BLINDNESS, 20 * 5, 0), 1.0f)
                    .build())
                .stacksTo(64)));

    public static final RegistryObject<Item> TNT_APPLE =
        ITEMS.register("tnt_apple", () ->
            new TntAppleItem(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> NUCLEAR_APPLE =
        ITEMS.register("nuclear_apple", () ->
            new NuclearAppleItem(new Item.Properties()
                .stacksTo(1)
                .fireResistant()));

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

    public static final RegistryObject<Item> LONGEVITY_APPLE = ITEMS.register("longevity_apple", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(6).saturationMod(0.6f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION,    20 * 120, 3), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST,  20 *  60, 0), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION,  20 *  15, 0), 1.0f)
                .build())
            .stacksTo(64)));

    public static final RegistryObject<Item> PRISM_APPLE =
        ITEMS.register("prism_apple", PrismAppleItem::new);

    public static final RegistryObject<Item> BANANA = ITEMS.register("banana", () ->
        new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(4)
                .saturationMod(0.5f)
                .build())
            .stacksTo(64)));

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
                output.accept(COAL_APPLE.get());
                output.accept(COPPER_APPLE.get());
                output.accept(EXPOSED_COPPER_APPLE.get());
                output.accept(WEATHERED_COPPER_APPLE.get());
                output.accept(OXIDIZED_COPPER_APPLE.get());
                output.accept(WAXED_COPPER_APPLE.get());
                output.accept(WAXED_EXPOSED_COPPER_APPLE.get());
                output.accept(WAXED_WEATHERED_COPPER_APPLE.get());
                output.accept(WAXED_OXIDIZED_COPPER_APPLE.get());
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
                output.accept(ENDER_PEARL_APPLE.get());
                output.accept(MOON_APPLE.get());
                output.accept(ORCHARD_APPLE.get());
                output.accept(ECHO_APPLE.get());
                output.accept(REWIND_APPLE.get());
                output.accept(APPLE_BOMB.get());
                output.accept(TNT_APPLE.get());
                output.accept(NUCLEAR_APPLE.get());
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

    public ultimate_apple_modNeoForge() {
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

        @SuppressWarnings("unchecked")
        java.util.function.Supplier<net.minecraft.world.level.block.entity.BlockEntityType<?>> beSupplier =
            (java.util.function.Supplier<net.minecraft.world.level.block.entity.BlockEntityType<?>>) (java.util.function.Supplier<?>) MIXER_BLOCK_ENTITY;
        @SuppressWarnings("unchecked")
        java.util.function.Supplier<net.minecraft.world.inventory.MenuType<?>> menuSupplier =
            (java.util.function.Supplier<net.minecraft.world.inventory.MenuType<?>>) (java.util.function.Supplier<?>) MIXER_MENU_TYPE;

        ModRegistries.CURSE_OF_ROTTEN    = CURSE_OF_ROTTEN;
        ModRegistries.MOON_GRAVITY       = MOON_GRAVITY_EFFECT;
        ModRegistries.LIFESTEAL          = LIFESTEAL_EFFECT;
        ModRegistries.TOTEM_PROTECTION   = TOTEM_PROTECTION_EFFECT;
        ModRegistries.TIME_FREEZE        = TIME_FREEZE_EFFECT;

        ModRegistries.DIAMOND_APPLE                = DIAMOND_APPLE;
        ModRegistries.LAPISLAZULI_APPLE            = LAPISLAZULI_APPLE;
        ModRegistries.EMERALD_APPLE                = EMERALD_APPLE;
        ModRegistries.REDSTONE_APPLE               = REDSTONE_APPLE;
        ModRegistries.NETHERITE_APPLE              = NETHERITE_APPLE;
        ModRegistries.IRON_APPLE                   = IRON_APPLE;
        ModRegistries.ROTTEN_APPLE                 = ROTTEN_APPLE;
        ModRegistries.ROASTED_APPLE                = ROASTED_APPLE;
        ModRegistries.BAKED_APPLE                  = BAKED_APPLE;
        ModRegistries.BURNT_APPLE                  = BURNT_APPLE;
        ModRegistries.BLAZE_APPLE                  = BLAZE_APPLE;
        ModRegistries.BIRNE                        = BIRNE;
        ModRegistries.COPPER_APPLE                 = COPPER_APPLE;
        ModRegistries.EXPOSED_COPPER_APPLE         = EXPOSED_COPPER_APPLE;
        ModRegistries.WEATHERED_COPPER_APPLE       = WEATHERED_COPPER_APPLE;
        ModRegistries.OXIDIZED_COPPER_APPLE        = OXIDIZED_COPPER_APPLE;
        ModRegistries.WAXED_COPPER_APPLE           = WAXED_COPPER_APPLE;
        ModRegistries.WAXED_EXPOSED_COPPER_APPLE   = WAXED_EXPOSED_COPPER_APPLE;
        ModRegistries.WAXED_WEATHERED_COPPER_APPLE = WAXED_WEATHERED_COPPER_APPLE;
        ModRegistries.WAXED_OXIDIZED_COPPER_APPLE  = WAXED_OXIDIZED_COPPER_APPLE;
        ModRegistries.ENDER_PEARL_APPLE            = ENDER_PEARL_APPLE;
        ModRegistries.MOON_APPLE                   = MOON_APPLE;
        ModRegistries.ORCHARD_APPLE                = ORCHARD_APPLE;
        ModRegistries.ECHO_APPLE                   = ECHO_APPLE;
        ModRegistries.REWIND_APPLE                 = REWIND_APPLE;
        ModRegistries.APPLE_BOMB                   = APPLE_BOMB;
        ModRegistries.COAL_APPLE                   = COAL_APPLE;
        ModRegistries.TNT_APPLE                    = TNT_APPLE;
        ModRegistries.NUCLEAR_APPLE                = NUCLEAR_APPLE;
        ModRegistries.WITHER_APPLE                 = WITHER_APPLE;
        ModRegistries.HONEY_APPLE                  = HONEY_APPLE;
        ModRegistries.DRAGON_APPLE                 = DRAGON_APPLE;
        ModRegistries.NETHER_STAR_APPLE            = NETHER_STAR_APPLE;
        ModRegistries.DIRT_APPLE                   = DIRT_APPLE;
        ModRegistries.TOTEM_APPLE                  = TOTEM_APPLE;
        ModRegistries.QUANTUM_APPLE                = QUANTUM_APPLE;
        ModRegistries.VOID_APPLE                   = VOID_APPLE;
        ModRegistries.TIME_FREEZE_APPLE            = TIME_FREEZE_APPLE;
        ModRegistries.LONGEVITY_APPLE              = LONGEVITY_APPLE;
        ModRegistries.PRISM_APPLE                  = PRISM_APPLE;
        ModRegistries.BANANA                       = BANANA;
        ModRegistries.CUP_ITEM                     = CUP_ITEM;
        ModRegistries.SHAKE_ITEM                   = SHAKE_ITEM;

        ModRegistries.APPLE_BOMB_ENTITY    = APPLE_BOMB_ENTITY;
        ModRegistries.SHAKE_BOMB_ENTITY    = SHAKE_BOMB_ENTITY;
        ModRegistries.TNT_APPLE_ENTITY     = TNT_APPLE_ENTITY;
        ModRegistries.NUCLEAR_APPLE_ENTITY = NUCLEAR_APPLE_ENTITY;

        ModRegistries.MIXER_BLOCK_ENTITY = beSupplier;
        ModRegistries.MIXER_MENU_TYPE    = menuSupplier;
        ModRegistries.MIXER              = ModBlocks.MIXER;
        ModRegistries.MIXER_ITEM         = ModBlocks.MIXER_ITEM;

        ultimate_apple_mod.init();
        modEventBus.addListener(ultimate_apple_modNeoForge::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ComposterBlock.COMPOSTABLES.put(ROTTEN_APPLE.get(),       0.30f);
            ComposterBlock.COMPOSTABLES.put(BURNT_APPLE.get(),        0.30f);
            ComposterBlock.COMPOSTABLES.put(DIRT_APPLE.get(),         0.30f);
            ComposterBlock.COMPOSTABLES.put(TNT_APPLE.get(),          0.30f);
            ComposterBlock.COMPOSTABLES.put(COAL_APPLE.get(),         0.30f);
            ComposterBlock.COMPOSTABLES.put(BAKED_APPLE.get(),        0.65f);
            ComposterBlock.COMPOSTABLES.put(ROASTED_APPLE.get(),      0.65f);
            ComposterBlock.COMPOSTABLES.put(BIRNE.get(),              0.65f);
            ComposterBlock.COMPOSTABLES.put(BANANA.get(),             0.65f);
            ComposterBlock.COMPOSTABLES.put(BLAZE_APPLE.get(),        0.65f);
            ComposterBlock.COMPOSTABLES.put(HONEY_APPLE.get(),        0.65f);
            ComposterBlock.COMPOSTABLES.put(MOON_APPLE.get(),         0.65f);
            ComposterBlock.COMPOSTABLES.put(IRON_APPLE.get(),         0.65f);
            ComposterBlock.COMPOSTABLES.put(COPPER_APPLE.get(),              0.65f);
            ComposterBlock.COMPOSTABLES.put(EXPOSED_COPPER_APPLE.get(),      0.65f);
            ComposterBlock.COMPOSTABLES.put(WEATHERED_COPPER_APPLE.get(),    0.65f);
            ComposterBlock.COMPOSTABLES.put(OXIDIZED_COPPER_APPLE.get(),     0.65f);
            ComposterBlock.COMPOSTABLES.put(WAXED_COPPER_APPLE.get(),        0.65f);
            ComposterBlock.COMPOSTABLES.put(WAXED_EXPOSED_COPPER_APPLE.get(),0.65f);
            ComposterBlock.COMPOSTABLES.put(WAXED_WEATHERED_COPPER_APPLE.get(),0.65f);
            ComposterBlock.COMPOSTABLES.put(WAXED_OXIDIZED_COPPER_APPLE.get(),0.65f);
            ComposterBlock.COMPOSTABLES.put(LAPISLAZULI_APPLE.get(),  0.65f);
            ComposterBlock.COMPOSTABLES.put(REDSTONE_APPLE.get(),     0.65f);
            ComposterBlock.COMPOSTABLES.put(EMERALD_APPLE.get(),      0.65f);
            ComposterBlock.COMPOSTABLES.put(DIAMOND_APPLE.get(),      0.65f);
            ComposterBlock.COMPOSTABLES.put(NETHERITE_APPLE.get(),    0.65f);
            ComposterBlock.COMPOSTABLES.put(PRISM_APPLE.get(),        0.65f);
            ComposterBlock.COMPOSTABLES.put(TIME_FREEZE_APPLE.get(),  0.65f);
            ComposterBlock.COMPOSTABLES.put(VOID_APPLE.get(),         0.65f);
            ComposterBlock.COMPOSTABLES.put(ENDER_PEARL_APPLE.get(),  0.65f);
            ComposterBlock.COMPOSTABLES.put(ECHO_APPLE.get(),         0.65f);
            ComposterBlock.COMPOSTABLES.put(REWIND_APPLE.get(),       0.65f);
            ComposterBlock.COMPOSTABLES.put(LONGEVITY_APPLE.get(),    0.65f);
            ComposterBlock.COMPOSTABLES.put(QUANTUM_APPLE.get(),      0.65f);
            ComposterBlock.COMPOSTABLES.put(WITHER_APPLE.get(),       0.65f);
            ComposterBlock.COMPOSTABLES.put(DRAGON_APPLE.get(),       0.65f);
            ComposterBlock.COMPOSTABLES.put(TOTEM_APPLE.get(),        0.65f);
            ComposterBlock.COMPOSTABLES.put(ORCHARD_APPLE.get(),      0.85f);
        });
    }
}
```

---

## Task 5: NeoForge ModClient + ModRecipes

**Files:**
- Create: `neoforge/src/main/java/de/maxi/ultimate_apple_mod/neoforge/ModClient.java`
- Create: `neoforge/src/main/java/de/maxi/ultimate_apple_mod/neoforge/ModRecipes.java`

- [ ] **Step 1: Create ModClient.java**

```java
package de.maxi.ultimate_apple_mod.neoforge;

import de.maxi.ultimate_apple_mod.block.MixerScreen;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import de.maxi.ultimate_apple_mod.neoforge.block.ModBlocks;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import static de.maxi.ultimate_apple_mod.ultimate_apple_mod.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClient {

    public static final KeyMapping FIRE_DRAGON_BREATH_KEY = new KeyMapping(
        "key.ultimate_apple_mod.fire_dragon_breath",
        InputConstants.Type.MOUSE,
        GLFW.GLFW_MOUSE_BUTTON_LEFT,
        "key.categories.ultimate_apple_mod"
    );

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(FIRE_DRAGON_BREATH_KEY);
    }

    @SubscribeEvent
    public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ultimate_apple_modNeoForge.APPLE_BOMB_ENTITY.get(),
            ThrownItemRenderer::new);
        event.registerEntityRenderer(ultimate_apple_modNeoForge.SHAKE_BOMB_ENTITY.get(),
            ThrownItemRenderer::new);
        event.registerEntityRenderer(ultimate_apple_modNeoForge.TNT_APPLE_ENTITY.get(),
            ThrownItemRenderer::new);
        event.registerEntityRenderer(ultimate_apple_modNeoForge.NUCLEAR_APPLE_ENTITY.get(),
            ThrownItemRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ultimate_apple_modNeoForge.MIXER_MENU_TYPE.get(), MixerScreen::new);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.MIXER.get(), RenderType.cutoutMipped());
        });
    }
}
```

- [ ] **Step 2: Create ModRecipes.java**

```java
package de.maxi.ultimate_apple_mod.neoforge;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ultimate_apple_mod.MOD_ID);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
```

---

## Task 6: NeoForge block package

**Files:**
- Create: `neoforge/src/main/java/de/maxi/ultimate_apple_mod/neoforge/block/MixerBlockEntity.java`
- Create: `neoforge/src/main/java/de/maxi/ultimate_apple_mod/neoforge/block/ModBlocks.java`

- [ ] **Step 1: Create MixerBlockEntity.java**

```java
package de.maxi.ultimate_apple_mod.neoforge.block;

import de.maxi.ultimate_apple_mod.block.MixerBlockEntityBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MixerBlockEntity extends MixerBlockEntityBase {

    public MixerBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }
}
```

- [ ] **Step 2: Create ModBlocks.java**

```java
package de.maxi.ultimate_apple_mod.neoforge.block;

import static de.maxi.ultimate_apple_mod.ultimate_apple_mod.MOD_ID;

import de.maxi.ultimate_apple_mod.block.MixerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final RegistryObject<Block> MIXER =
        BLOCKS.register("mixer", () -> new MixerBlock() {
            @Override
            public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
                return new MixerBlockEntity(pos, state);
            }
        });

    public static final RegistryObject<Item> MIXER_ITEM =
        ITEMS.register("mixer", () -> new BlockItem(MIXER.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
}
```

---

## Task 7: NeoForge event handlers

**Files (all in `neoforge/src/main/java/de/maxi/ultimate_apple_mod/neoforge/event/`):**
- `ClientEventHandler.java`
- `ClientPlayerRenderHandler.java`
- `DecayEventHandler.java`
- `KeyInputHandler.java`
- `LootTableHandler.java`
- `MobDropEventHandler.java`
- `PlayerEffectEventHandler.java`
- `RewindTracker.java`
- `TntAppleEventHandler.java`
- `babyzombiedroppt.java`

- [ ] **Step 1: Create ClientEventHandler.java**

```java
package de.maxi.ultimate_apple_mod.neoforge.event;

import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.registries.ForgeRegistries;

import java.util.List;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID,
                        bus   = Mod.EventBusSubscriber.Bus.NEOFORGE,
                        value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tips = event.getToolTip();

        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(DecayEventHandler.DECAY_TAG)) {
            long threshold = DecayEventHandler.getDecayThreshold(stack.getItem());
            if (threshold > 0) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level != null) {
                    long decayStart = tag.getLong(DecayEventHandler.DECAY_TAG);
                    long remaining  = threshold - (mc.level.getGameTime() - decayStart);
                    if (remaining <= 0) {
                        tips.add(Component.literal("§cRots any moment now!"));
                    } else {
                        long totalSecs = remaining / 20;
                        String color;
                        if      (remaining > 20L * 60 * 15) color = "§a";
                        else if (remaining > 20L * 60 *  5) color = "§e";
                        else if (remaining > 20L * 60 *  1) color = "§6";
                        else                                 color = "§c";
                        tips.add(Component.literal(
                            String.format("%sRots in: %d:%02d", color, totalSecs / 60, totalSecs % 60)));
                    }
                }
            }
        }

        Item item = stack.getItem();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null || !id.getNamespace().equals(ultimate_apple_mod.MOD_ID)) return;

        String path = id.getPath();
        if (path.equals("shake") || path.equals("cup")) return;

        if (!Screen.hasShiftDown()) {
            while (tips.size() > 1) tips.remove(1);
            tips.add(Component.literal("§7Hold §eShift §7for more info")
                .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            if (path.equals("mixer")) {
                tips.add(Component.literal("Combine two apple items to brew a Shake.")
                    .withStyle(ChatFormatting.GRAY));
                tips.add(Component.literal("Effects from both ingredients are merged.")
                    .withStyle(ChatFormatting.GRAY));
                tips.add(Component.literal("Requires a Cup in the bottom slot.")
                    .withStyle(ChatFormatting.DARK_GRAY));
                tips.add(Component.literal("⊕ All effect durations receive a +20% bonus.")
                    .withStyle(ChatFormatting.DARK_GREEN));
                tips.add(Component.literal("⊕ Add a Longevity Apple to double all durations.")
                    .withStyle(ChatFormatting.DARK_GREEN));
                return;
            }

            FoodProperties food = item.getFoodProperties(stack, null);
            if (food != null && !food.getEffects().isEmpty()) {
                tips.add(Component.literal("Effects:").withStyle(ChatFormatting.GOLD));
                for (var pair : food.getEffects()) {
                    tips.add(formatEffect(pair.getFirst()));
                }
            }
        }
    }

    private static Component formatEffect(MobEffectInstance eff) {
        int amp = eff.getAmplifier();
        int dur = eff.getDuration();
        MutableComponent line = Component.literal("  ")
            .append(eff.getEffect().getDisplayName().copy().withStyle(ChatFormatting.GRAY));
        if (amp > 0) {
            line.append(Component.literal(" " + toRoman(amp + 1))
                .withStyle(ChatFormatting.GRAY));
        }
        line.append(Component.literal(" (" + formatDuration(dur) + ")")
            .withStyle(ChatFormatting.DARK_GRAY));
        return line;
    }

    private static String toRoman(int n) {
        return switch (n) {
            case 2  -> "II";   case 3 -> "III"; case 4 -> "IV";
            case 5  -> "V";    case 6 -> "VI";  case 7 -> "VII";
            case 8  -> "VIII"; case 9 -> "IX";  case 10 -> "X";
            default -> String.valueOf(n);
        };
    }

    private static String formatDuration(int ticks) {
        int s = ticks / 20;
        if (s >= 60) {
            int m = s / 60;
            int r = s % 60;
            return r == 0 ? m + "m" : m + "m " + r + "s";
        }
        return s + "s";
    }
}
```

- [ ] **Step 2: Create ClientPlayerRenderHandler.java**

```java
package de.maxi.ultimate_apple_mod.neoforge.event;

import de.maxi.ultimate_apple_mod.neoforge.ultimate_apple_modNeoForge;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Pose;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.event.TickEvent;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.NEOFORGE, value = Dist.CLIENT)
public class ClientPlayerRenderHandler {

    private static boolean wasRottenActive = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            wasRottenActive = false;
            return;
        }
        boolean isRottenActive = player.hasEffect(ultimate_apple_modNeoForge.CURSE_OF_ROTTEN.get());
        if (isRottenActive != wasRottenActive) {
            player.refreshDimensions();
            wasRottenActive = isRottenActive;
        }
    }

    @SubscribeEvent
    public static void onClientTickEnd(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        try {
            if (player.hasEffect(ultimate_apple_modNeoForge.CURSE_OF_ROTTEN.get())
                    && player.getPose() == Pose.SWIMMING
                    && !player.isInWater()) {
                player.setPose(Pose.STANDING);
            }
        } catch (NullPointerException ignored) {}
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (event.getEntity().hasEffect(ultimate_apple_modNeoForge.CURSE_OF_ROTTEN.get())) {
            event.getPoseStack().scale(0.35f, 0.35f, 0.35f);
        }
    }
}
```

- [ ] **Step 3: Create DecayEventHandler.java**

```java
package de.maxi.ultimate_apple_mod.neoforge.event;

import de.maxi.ultimate_apple_mod.ModRegistries;
import de.maxi.ultimate_apple_mod.event.DecayHelper;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.TickEvent;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.NEOFORGE)
public class DecayEventHandler {

    public static final String DECAY_TAG = DecayHelper.DECAY_TAG;
    public static final long APPLE_DECAY_TICKS           = DecayHelper.APPLE_DECAY_TICKS;
    public static final long GOLDEN_APPLE_DECAY_TICKS    = 20L * 60 * 45;
    public static final long ENCHANTED_APPLE_DECAY_TICKS = 20L * 60 * 60;

    public static long getDecayThreshold(net.minecraft.world.item.Item item) {
        return DecayHelper.getDecayThreshold(item);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player.level() instanceof ServerLevel serverLevel)) return;
        if (serverLevel.getGameTime() % 20 != 0) return;

        Player player = event.player;
        long now = serverLevel.getGameTime();
        Inventory inv = player.getInventory();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            long threshold = getDecayThreshold(stack.getItem());
            if (threshold == 0) continue;

            CompoundTag tag = stack.getOrCreateTag();
            if (!tag.contains(DECAY_TAG)) {
                tag.putLong(DECAY_TAG, now);
                continue;
            }

            long elapsed = now - tag.getLong(DECAY_TAG);
            if (elapsed < threshold) continue;

            ItemStack replacement = getDecayReplacement(stack);
            inv.setItem(i, replacement);
        }
    }

    private static ItemStack getDecayReplacement(ItemStack original) {
        int count = original.getCount();
        if (original.getItem() == Items.APPLE) {
            return new ItemStack(ModRegistries.ROTTEN_APPLE.get(), count);
        }
        return ItemStack.EMPTY;
    }
}
```

- [ ] **Step 4: Create KeyInputHandler.java**

```java
package de.maxi.ultimate_apple_mod.neoforge.event;

import de.maxi.ultimate_apple_mod.neoforge.ModClient;
import de.maxi.ultimate_apple_mod.neoforge.network.FireDragonBreathPacket;
import de.maxi.ultimate_apple_mod.neoforge.network.NetworkHandler;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.NEOFORGE, value = Dist.CLIENT)
public class KeyInputHandler {

    private static boolean prevFireBreathDown = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        long window = Minecraft.getInstance().getWindow().getWindow();
        boolean isFireDown = false;
        boolean isBoundToMouse = false;
        for (int btn = 0; btn <= 7; btn++) {
            if (ModClient.FIRE_DRAGON_BREATH_KEY.matchesMouse(btn)) {
                isFireDown = GLFW.glfwGetMouseButton(window, btn) == GLFW.GLFW_PRESS;
                isBoundToMouse = true;
                break;
            }
        }
        if (!isBoundToMouse) {
            isFireDown = ModClient.FIRE_DRAGON_BREATH_KEY.isDown();
        }
        if (isFireDown && !prevFireBreathDown) {
            boolean aimingAtEntity = mc.hitResult instanceof EntityHitResult;
            var mainHand = mc.player.getMainHandItem();
            boolean holdingMeleeWeapon = mainHand.getItem() instanceof SwordItem
                || mainHand.getItem() instanceof AxeItem;

            if (!(aimingAtEntity && holdingMeleeWeapon)) {
                NetworkHandler.CHANNEL.sendToServer(new FireDragonBreathPacket());
            }
        }
        prevFireBreathDown = isFireDown;
    }
}
```

- [ ] **Step 5: Create LootTableHandler.java**

```java
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
```

- [ ] **Step 6: Create MobDropEventHandler.java**

```java
package de.maxi.ultimate_apple_mod.neoforge.event;

import de.maxi.ultimate_apple_mod.neoforge.ultimate_apple_modNeoForge;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.Random;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.NEOFORGE)
public class MobDropEventHandler {

    private static final Random RNG = new Random();

    private static void addDrop(LivingDropsEvent event, ItemStack stack) {
        var e = event.getEntity();
        event.getDrops().add(new ItemEntity(e.level(), e.getX(), e.getY(), e.getZ(), stack));
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        var entity = event.getEntity();

        if (entity instanceof WitherBoss) {
            if (RNG.nextDouble() < 0.5) {
                addDrop(event, new ItemStack(ultimate_apple_modNeoForge.WITHER_APPLE.get()));
                addDrop(event, new ItemStack(ultimate_apple_modNeoForge.NETHER_STAR_APPLE.get()));
            }
        } else if (entity instanceof Evoker) {
            event.getDrops().removeIf(drop -> drop.getItem().getItem() == Items.TOTEM_OF_UNDYING);
            ItemStack reward = RNG.nextDouble() < 0.3
                ? new ItemStack(ultimate_apple_modNeoForge.TOTEM_APPLE.get())
                : new ItemStack(Items.TOTEM_OF_UNDYING);
            event.getDrops().add(new ItemEntity(
                entity.level(),
                entity.getX(), entity.getY(), entity.getZ(),
                reward));
        }
    }
}
```

- [ ] **Step 7: Create PlayerEffectEventHandler.java**

```java
package de.maxi.ultimate_apple_mod.neoforge.event;

import de.maxi.ultimate_apple_mod.neoforge.ultimate_apple_modNeoForge;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.NEOFORGE)
public class PlayerEffectEventHandler {

    private static final WeakHashMap<Player, Boolean> serverRottenState = new WeakHashMap<>();
    private static final float ROTTEN_SCALE = 0.35f;

    @SubscribeEvent
    public static void onEntitySize(EntityEvent.Size event) {
        if (!(event.getEntity() instanceof Player player)) return;
        try {
            if (player.hasEffect(ultimate_apple_modNeoForge.CURSE_OF_ROTTEN.get())) {
                event.setNewSize(EntityDimensions.scalable(0.25f, 0.6f));
                event.setNewEyeHeight(1.62f * ROTTEN_SCALE);
            }
        } catch (NullPointerException ignored) {}
    }

    @SubscribeEvent
    public static void onServerPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Player player = event.player;
        if (!(player.level() instanceof ServerLevel)) return;

        boolean hasEffect;
        try {
            hasEffect = player.hasEffect(ultimate_apple_modNeoForge.CURSE_OF_ROTTEN.get());
        } catch (NullPointerException ignored) { return; }

        Boolean prev = serverRottenState.get(player);
        if (prev == null || prev != hasEffect) {
            serverRottenState.put(player, hasEffect);
            player.refreshDimensions();
        }
    }

    @SubscribeEvent
    public static void onServerPlayerTickEnd(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (!(player.level() instanceof ServerLevel)) return;

        try {
            if (player.hasEffect(ultimate_apple_modNeoForge.CURSE_OF_ROTTEN.get())
                    && player.getPose() == Pose.SWIMMING
                    && !player.isInWater()) {
                player.setPose(Pose.STANDING);
            }
        } catch (NullPointerException ignored) {}
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerTotemDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel)) return;

        try {
            if (!player.hasEffect(ultimate_apple_modNeoForge.TOTEM_PROTECTION_EFFECT.get())) return;
        } catch (NullPointerException ignored) { return; }

        event.setCanceled(true);
        player.removeEffect(ultimate_apple_modNeoForge.TOTEM_PROTECTION_EFFECT.get());
        player.setHealth(1.0f);
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,     20 * 45, 1));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,  20 * 40, 0));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,       20 * 15, 3));
        player.level().broadcastEntityEvent(player, (byte) 35);
        player.displayClientMessage(
            Component.translatable("message.ultimate_apple_mod.totem_apple_triggered"), true);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;
        if (event.getEntity() instanceof Player) return;

        LivingEntity dying = event.getEntity();
        ServerPlayer recipient = null;
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof ServerPlayer sp
                && sp.hasEffect(ultimate_apple_modNeoForge.LIFESTEAL_EFFECT.get())) {
            recipient = sp;
        }

        if (recipient == null) {
            double best = Double.MAX_VALUE;
            for (ServerPlayer sp : serverLevel.players()) {
                double dist = sp.distanceToSqr(dying);
                if (dist <= 32.0 * 32.0 && dist < best
                        && sp.hasEffect(ultimate_apple_modNeoForge.LIFESTEAL_EFFECT.get())) {
                    recipient = sp;
                    best = dist;
                }
            }
        }

        if (recipient == null) return;

        recipient.heal(2.0f);
        recipient.displayClientMessage(
            Component.translatable("message.ultimate_apple_mod.lifesteal_heal"), true);
        serverLevel.playSound(null,
            recipient.getX(), recipient.getY(), recipient.getZ(),
            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8f, 0.4f);
        serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE,
            recipient.getX(), recipient.getY() + 1.0, recipient.getZ(),
            14, 0.5, 0.9, 0.5, 0.04);
        serverLevel.sendParticles(ParticleTypes.HEART,
            recipient.getX(), recipient.getY() + 2.1, recipient.getZ(),
            4, 0.4, 0.15, 0.4, 0.0);
    }
}
```

- [ ] **Step 8: Create RewindTracker.java**

```java
package de.maxi.ultimate_apple_mod.neoforge.event;

import de.maxi.ultimate_apple_mod.RewindPositionCache;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.NEOFORGE)
public class RewindTracker {

    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (++tickCounter % 20 != 0) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        for (ServerLevel level : server.getAllLevels()) {
            RewindPositionCache.recordAll(level.players());
        }
    }
}
```

- [ ] **Step 9: Create TntAppleEventHandler.java**

```java
package de.maxi.ultimate_apple_mod.neoforge.event;

import de.maxi.ultimate_apple_mod.item.TntAppleEntity;
import de.maxi.ultimate_apple_mod.item.TntAppleItem;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ghast;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.NEOFORGE)
public class TntAppleEventHandler {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Ghast)) return;

        Entity directEntity = event.getSource().getDirectEntity();
        if (!(directEntity instanceof TntAppleEntity tntApple)) return;

        Entity owner = tntApple.getOwner();
        if (!(owner instanceof ServerPlayer player)) return;

        TntAppleItem.grantAdvancement(player, "tnt_apple_ghast");
    }
}
```

- [ ] **Step 10: Create babyzombiedroppt.java**

```java
package de.maxi.ultimate_apple_mod.neoforge.event;

import de.maxi.ultimate_apple_mod.neoforge.ultimate_apple_modNeoForge;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.NEOFORGE)
public class babyzombiedroppt {

    @SubscribeEvent
    public static void onZombieDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        Level level = entity.level();

        if (level != null && !level.isClientSide && entity instanceof Zombie zombie && zombie.isBaby()) {
            if (Math.random() < 0.1) {
                zombie.spawnAtLocation(new ItemStack(ultimate_apple_modNeoForge.ROTTEN_APPLE.get()));
            }
        }
    }
}
```

---

## Task 8: NeoForge network

**Files:**
- Create: `neoforge/src/main/java/de/maxi/ultimate_apple_mod/neoforge/network/NetworkHandler.java`
- Create: `neoforge/src/main/java/de/maxi/ultimate_apple_mod/neoforge/network/FireDragonBreathPacket.java`

- [ ] **Step 1: Create NetworkHandler.java**

```java
package de.maxi.ultimate_apple_mod.neoforge.network;

import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.NetworkDirection;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.simple.SimpleChannel;

import java.util.Optional;

public class NetworkHandler {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(ultimate_apple_mod.MOD_ID, "main"),
        () -> PROTOCOL,
        PROTOCOL::equals,
        PROTOCOL::equals
    );

    public static void register() {
        CHANNEL.registerMessage(
            0,
            FireDragonBreathPacket.class,
            FireDragonBreathPacket::encode,
            FireDragonBreathPacket::decode,
            FireDragonBreathPacket::handle,
            Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }
}
```

- [ ] **Step 2: Create FireDragonBreathPacket.java**

```java
package de.maxi.ultimate_apple_mod.neoforge.network;

import de.maxi.ultimate_apple_mod.DragonChargesCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FireDragonBreathPacket {

    public FireDragonBreathPacket() {}

    public static void encode(FireDragonBreathPacket msg, FriendlyByteBuf buf) {}

    public static FireDragonBreathPacket decode(FriendlyByteBuf buf) {
        return new FireDragonBreathPacket();
    }

    public static void handle(FireDragonBreathPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            int charges = DragonChargesCache.getCharges(player.getUUID());
            if (charges <= 0) return;

            Vec3 look = player.getLookAngle();
            DragonFireball fireball = new DragonFireball(player.level(), player,
                look.x, look.y, look.z);
            fireball.setPos(
                player.getX() + look.x * 1.5,
                player.getEyeY() - 0.1,
                player.getZ() + look.z * 1.5
            );
            player.level().addFreshEntity(fireball);

            int remaining = charges - 1;
            DragonChargesCache.setCharges(player.getUUID(), remaining);
            player.displayClientMessage(
                Component.translatable("message.ultimate_apple_mod.dragon_breath_remaining", remaining),
                true);
        });
        ctx.get().setPacketHandled(true);
    }
}
```

---

## Task 9: Build and verify NeoForge

- [ ] **Step 1: Attempt the NeoForge build**

```
Run: .\gradlew.bat :neoforge:build
Expected: BUILD SUCCESSFUL with a jar in neoforge/build/libs/
```

If the build fails, common fixes:

**`IMenuTypeExtension` not found:**
Replace the import and call. Try `net.neoforged.neoforge.common.extensions.IMenuTypeExtension` first. If that doesn't exist, use this alternative (works for all NeoForge 1.20.1 builds):
```java
// In ultimate_apple_modNeoForge.java constructor area:
// Remove IMenuTypeExtension and replace with direct MenuType construction:
import net.minecraft.world.flag.FeatureFlags;
// ...
MENUS.register("mixer", () -> new MenuType<>(MixerMenu::new, FeatureFlags.VANILLA_SET));
```

**`LootTableLoadEvent` not found in neoforge.event:**
Try `net.neoforged.neoforge.event.LootTableLoadEvent` — if not there, check `net.neoforged.neoforge.event.level.LootTableLoadEvent`.

**`NetworkRegistry` / `SimpleChannel` not found:**
NeoForge 1.20.1 47.1.x retained these. If missing, check `net.neoforged.neoforge.network.*`.

**`jei-1.20.1-neoforge-api` artifact not found:**
Remove that line from `neoforge/build.gradle` dependencies — NeoForge JEI API didn't separate the artifact in early 1.20.1 builds. The `jei-1.20.1-common-api` covers everything needed for compile.

- [ ] **Step 2: Confirm the jar exists**

```
Run: Get-Item "neoforge\build\libs\ultimate_apple_mod-neoforge-*.jar"
Expected: File listed (not dev-shadow, the remapped jar)
```

---

## Task 10: Quilt build.gradle + quilt.mod.json

**Files:**
- Create: `quilt/build.gradle`
- Create: `quilt/src/main/resources/quilt.mod.json`

- [ ] **Step 1: Create quilt/build.gradle**

```gradle
plugins {
    id 'com.github.johnrengelman.shadow'
}

architectury {
    platformSetupLoomIde()
    fabric()  // Quilt runs Fabric API through quilted-fabric-api; use fabric() target
}

configurations {
    common {
        canBeResolved = true
        canBeConsumed = false
    }
    compileClasspath.extendsFrom common
    runtimeClasspath.extendsFrom common
    developmentFabric.extendsFrom common

    shadowBundle {
        canBeResolved = true
        canBeConsumed = false
    }
}

dependencies {
    modImplementation "org.quiltmc:quilt-loader:$rootProject.quilt_loader_version"
    modImplementation "org.quiltmc.quilted-fabric-api:quilted-fabric-api:$rootProject.quilted_fabric_api_version"
    modImplementation "dev.architectury:architectury-fabric:$rootProject.architectury_api_version"

    // Common module (source of truth for all game logic)
    common(project(path: ':common', configuration: 'namedElements')) { transitive false }
    shadowBundle project(path: ':common', configuration: 'transformProductionFabric')

    // Fabric module: needed at compile time so QuiltModInit can call FabricModInit,
    // and included in the shadow jar so the delegation actually runs at runtime.
    modCompileOnly(project(path: ':fabric', configuration: 'namedElements')) { transitive false }
    shadowBundle project(path: ':fabric', configuration: 'namedElements')
}

processResources {
    inputs.property 'version', project.version

    filesMatching('quilt.mod.json') {
        expand version: project.version
    }
}

shadowJar {
    configurations = [project.configurations.shadowBundle]
    archiveClassifier = 'dev-shadow'
}

remapJar {
    input.set shadowJar.archiveFile
}
```

> Note: We use `fabric()` for Quilt because Architectury API 9.x for 1.20.1 does not have a separate Quilt target — Quilt runs via QFAPI Fabric compatibility. The `developmentFabric` configuration name is correct for this setup.

- [ ] **Step 2: Create quilt.mod.json**

```json
{
  "schema_version": 1,
  "quilt_loader": {
    "group": "de.maxi",
    "id": "ultimate_apple_mod",
    "version": "${version}",
    "metadata": {
      "name": "Ultimate Apple Mod",
      "description": "This mod adds new types of special apples.",
      "contributors": {
        "Maxi": "Owner"
      },
      "license": "ISC",
      "icon": "assets/ultimate_apple_mod/icon.png"
    },
    "intermediate_mappings": "net.fabricmc:intermediary",
    "entrypoints": {
      "init": [
        "de.maxi.ultimate_apple_mod.quilt.ultimate_apple_modQuilt"
      ],
      "client_init": [
        "de.maxi.ultimate_apple_mod.quilt.QuiltModClient"
      ]
    },
    "depends": [
      {
        "id": "quilt_loader",
        "versions": ">=0.22"
      },
      {
        "id": "quilted_fabric_api",
        "versions": "*"
      },
      {
        "id": "minecraft",
        "versions": "~1.20.1"
      }
    ]
  },
  "mixin": [
    "ultimate_apple_mod.mixins.json",
    "ultimate_apple_mod.fabric.mixins.json"
  ]
}
```

---

## Task 11: Quilt initializer files

**Files:**
- Create: `quilt/src/main/java/de/maxi/ultimate_apple_mod/quilt/ultimate_apple_modQuilt.java`
- Create: `quilt/src/main/java/de/maxi/ultimate_apple_mod/quilt/QuiltModClient.java`

- [ ] **Step 1: Create ultimate_apple_modQuilt.java**

```java
package de.maxi.ultimate_apple_mod.quilt;

import de.maxi.ultimate_apple_mod.fabric.ultimate_apple_modFabric;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class ultimate_apple_modQuilt implements ModInitializer {

    @Override
    public void onInitialize(ModContainer container) {
        new ultimate_apple_modFabric().onInitialize();
    }
}
```

- [ ] **Step 2: Create QuiltModClient.java**

```java
package de.maxi.ultimate_apple_mod.quilt;

import de.maxi.ultimate_apple_mod.fabric.FabricModClient;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class QuiltModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient(ModContainer container) {
        new FabricModClient().onInitializeClient();
    }
}
```

---

## Task 12: Build and verify Quilt

- [ ] **Step 1: Attempt the Quilt build**

```
Run: .\gradlew.bat :quilt:build
Expected: BUILD SUCCESSFUL with a jar in quilt/build/libs/
```

Common fixes:

**`org.quiltmc.qsl.base.api.entrypoint.ModInitializer` not found:**
The QSL class path shifted between QFAPI versions. Try the alternative import:
```java
// Replace the import in both files:
import net.fabricmc.api.ModInitializer;
// Change signature:
public class ultimate_apple_modQuilt implements ModInitializer {
    @Override
    public void onInitialize() {
        new ultimate_apple_modFabric().onInitialize();
    }
}
```
And in quilt.mod.json, the `"init"` entrypoint stays the same — Quilt Loader accepts `net.fabricmc.api.ModInitializer` via the QFAPI adapter.

**`implementation project(":fabric")` causes circular dependency:**
Remove that line from `quilt/build.gradle`. Instead keep only the `common` dependency. The quilt source files directly call `ultimate_apple_modFabric` which is available via the `:common` `namedElements` configuration (since fabric's compiled classes are included when Quilt compiles against fabric's shadowBundle).

Actually, the correct approach without circular deps:
```gradle
// In quilt/build.gradle dependencies block, replace:
//   implementation project(":fabric")
// with:
shadowBundle project(path: ':fabric', configuration: 'namedElements')
```
This includes fabric's compiled classes in the Quilt shadow jar.

- [ ] **Step 2: Confirm the jar exists**

```
Run: Get-Item "quilt\build\libs\ultimate_apple_mod-quilt-*.jar"
Expected: File listed.
```

---

## Task 13: Final commit

- [ ] **Step 1: Stage all new files and commit**

```
Run: git add neoforge/ quilt/ settings.gradle gradle.properties
Run: git status
Expected: ~24 new files staged plus the 2 modified root files.
```

```
Run: git commit -m "Add NeoForge and Quilt subprojects for 1.20.1"
Expected: commit created on main branch.
```

---

## Troubleshooting Reference

| Symptom | Fix |
|---|---|
| `Could not find net.neoforged:neoforge:1.20.1-47.1.104` | Check https://projects.neoforged.net for latest 1.20.1 build; update `gradle.properties` |
| `IMenuTypeExtension` doesn't exist | Use `new MenuType<>(MixerMenu::new, FeatureFlags.VANILLA_SET)` — add `import net.minecraft.world.flag.FeatureFlags` |
| `Bus.NEOFORGE` doesn't exist in `Mod.EventBusSubscriber` | Use `Bus.FORGE` — some early NeoForge 1.20.1 builds kept the old name |
| `LootTableLoadEvent` missing | Try `net.neoforged.neoforge.event.level.LootTableLoadEvent` |
| Quilt build: `ModInitializer` interface not found in QSL | Use `net.fabricmc.api.ModInitializer` instead (QFAPI provides it) |
| Quilt build: circular project dependency | Replace `implementation project(":fabric")` with `shadowBundle project(path: ':fabric', configuration: 'namedElements')` |
