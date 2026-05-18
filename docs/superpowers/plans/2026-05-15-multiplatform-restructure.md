# Multiplatform Restructure (Forge + Fabric) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move all shared code into `common/`, keep Forge-specific code in `forge/`, and add a full Fabric implementation so both platforms expose identical functionality.

**Architecture:** Hybrid B — platform-separate implementations. `ModRegistries.java` in common holds `Supplier<>` fields filled by each platform at init. Common classes reference `ModRegistries.*` instead of `ultimate_apple_modForge.*`. Forge event handlers remain unchanged. Fabric event handlers are new classes using native Fabric API.

**Tech Stack:** Minecraft 1.20.1, Forge 47.x, Fabric Loader 0.16.13, Fabric API, Architectury 9.2.14 (Gradle build only, no Architectury runtime abstractions).

---

## File Structure

### New files in `common/`
| File | Purpose |
|---|---|
| `common/.../ModRegistries.java` | Platform-neutral `Supplier<>` fields for every registered object |
| `common/.../RewindPositionCache.java` | Platform-neutral position history (extracted from Forge `RewindTracker`) |
| `common/.../effect/*.java` (5 files) | Effect classes moved from forge |
| `common/.../item/*.java` (19 files) | Item classes moved from forge |
| `common/.../item/*Entity.java` (4 files) | Entity classes moved from forge, refs → ModRegistries |
| `common/.../block/MixerBlockEntityBase.java` | Full mixer logic extracted from forge MixerBlockEntity |
| `common/.../block/MixerBlock.java` | Moved from forge (no changes) |
| `common/.../block/MixerMenu.java` | Moved from forge (refs → ModRegistries + MixerBlockEntityBase) |
| `common/.../block/MixerScreen.java` | Moved from forge (no changes) |
| `common/.../block/MixerRecipes.java` | Moved from forge (no changes) |
| `common/.../network/FireDragonBreathPayload.java` | Common C2S packet record |
| `common/.../mixin/LivingEntitySizeMixin.java` | Hitbox resize for CurseOfRotten (both platforms) |

### Modified files in `forge/`
| File | Change |
|---|---|
| `forge/.../forge/ultimate_apple_modForge.java` | Fill `ModRegistries.*` fields in constructor |
| `forge/.../forge/block/MixerBlockEntity.java` | Slim to subclass of `MixerBlockEntityBase` |
| `forge/.../forge/block/ModBlocks.java` | Update generic type on `MIXER_BLOCK_ENTITY` |
| `forge/.../event/RewindTracker.java` | Delegate to `RewindPositionCache` |

### New files in `fabric/`
| File | Purpose |
|---|---|
| `fabric/.../fabric/ultimate_apple_modFabric.java` | Full registration of all objects + composter values |
| `fabric/.../fabric/FabricModClient.java` | Client init: keybinding, renderers, screen, network |
| `fabric/.../fabric/block/ModBlocks.java` | Fabric Registry.register for block + BE + MenuType |
| `fabric/.../fabric/block/MixerBlockEntity.java` | Extends base, implements SidedInventory |
| `fabric/.../fabric/event/FabricEventRegistrar.java` | Calls register() on every handler |
| `fabric/.../fabric/event/FabricMobDropHandler.java` | Wither + Evoker drops |
| `fabric/.../fabric/event/FabricPlayerEffectHandler.java` | Totem (ALLOW_DEATH), Lifesteal (AFTER_DEATH), CurseOfRotten tick |
| `fabric/.../fabric/event/FabricLootTableHandler.java` | All loot table injections |
| `fabric/.../fabric/event/FabricDecayHandler.java` | Vanilla apple decay |
| `fabric/.../fabric/event/FabricTntAppleHandler.java` | Ghast killed by TntAppleEntity → advancement |
| `fabric/.../fabric/event/FabricBabyZombieHandler.java` | Baby zombie rotten apple drop |
| `fabric/.../fabric/event/FabricClientHandler.java` | Tooltip, keybind poll, rewind render |
| `fabric/.../fabric/network/FabricNetworkHandler.java` | C2S packet registration + server receiver |
| `fabric/.../fabric/mixin/PlayerRendererMixin.java` | Render scale for CurseOfRotten (Fabric-only) |
| `fabric/src/main/resources/ultimate_apple_mod.fabric.mixins.json` | Fabric-only mixin config |

### Modified files in `fabric/`
| File | Change |
|---|---|
| `fabric/src/main/resources/fabric.mod.json` | Update entrypoints + add fabric mixin config |

### Resources
| Change | Detail |
|---|---|
| Move `forge/src/main/resources/assets/` → `common/src/main/resources/assets/` | All textures, models, blockstates, lang |
| Move `forge/src/main/resources/data/` → `common/src/main/resources/data/` | All recipes, loot, advancements |

---

## Task 1: Create common scaffold — RewindPositionCache + ModRegistries

**Files:**
- Create: `common/src/main/java/de/maxi/ultimate_apple_mod/RewindPositionCache.java`
- Create: `common/src/main/java/de/maxi/ultimate_apple_mod/ModRegistries.java`

- [ ] **Step 1: Create RewindPositionCache**

```java
// common/src/main/java/de/maxi/ultimate_apple_mod/RewindPositionCache.java
package de.maxi.ultimate_apple_mod;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Platform-neutral position history store.
 * Each platform ticks this via its own server-tick event.
 * ShakeBombEntity reads it via getPositionFiveSecondsAgo().
 */
public class RewindPositionCache {

    private static final Map<UUID, ArrayDeque<Vec3>> history = new HashMap<>();

    /** Called once per second by each platform's server-tick handler. */
    public static void recordAll(Iterable<? extends Player> players) {
        for (Player player : players) {
            ArrayDeque<Vec3> q = history.computeIfAbsent(player.getUUID(), k -> new ArrayDeque<>());
            q.addLast(player.position());
            while (q.size() > 5) q.removeFirst();
        }
    }

    /** Returns the position ~5 seconds ago, or null if no history yet. */
    public static Vec3 getPositionFiveSecondsAgo(Player player) {
        ArrayDeque<Vec3> q = history.get(player.getUUID());
        if (q == null || q.isEmpty()) return null;
        return q.peekFirst();
    }
}
```

- [ ] **Step 2: Create ModRegistries**

```java
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
```

- [ ] **Step 3: Verify the project still compiles (no new classes imported yet)**

Run: `./gradlew :forge:compileJava`
Expected: BUILD SUCCESSFUL (ModRegistries has no code logic, just null Supplier fields)

---

## Task 2: Move effect classes to common

**Files:**
- Move: `forge/src/main/java/de/maxi/ultimate_apple_mod/effect/` → `common/src/main/java/de/maxi/ultimate_apple_mod/effect/`
- Files: `CurseOfRotten.java`, `LifestealEffect.java`, `MoonGravityEffect.java`, `TimeFreezeEffect.java`, `TotemProtectionEffect.java`

- [ ] **Step 1: Move all 5 effect files**

```
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/effect" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/effect"
```

- [ ] **Step 2: Verify packages match**

Open each moved file and confirm the package declaration is `de.maxi.ultimate_apple_mod.effect` — no change needed since the package path is the same.

- [ ] **Step 3: Verify Forge still compiles**

Run: `./gradlew :forge:compileJava`

Forge imports the effect classes through the common module (Architectury includes common in the Forge classpath). Expected: BUILD SUCCESSFUL.

---

## Task 3: Move item classes to common + fix CopperAppleItem

**Files:**
- Move: all 19 item `.java` files from `forge/src/main/java/de/maxi/ultimate_apple_mod/item/` to `common/src/main/java/de/maxi/ultimate_apple_mod/item/`
- Modify: `common/.../item/CopperAppleItem.java` (ForgeRegistries → BuiltInRegistries)

The 19 item files are:
`AppleBombItem`, `CoalAppleItem`, `CopperAppleItem`, `CupItem`, `DragonAppleItem`, `EchoAppleItem`, `EnderPearlAppleItem`, `HoneyAppleItem`, `LapislazuliAppleItem`, `NuclearAppleItem`, `OrchardCallerItem`, `PrismAppleItem`, `QuantumAppleItem`, `RewindAppleItem`, `ShakeItem`, `TntAppleItem`, `TotemAppleItem`, `VoidAppleItem`, `WitherAppleItem`

- [ ] **Step 1: Move item files (leave entity files — they are moved in Task 4)**

```
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/item/AppleBombItem.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/item/"
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/item/CoalAppleItem.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/item/"
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/item/CopperAppleItem.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/item/"
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/item/CupItem.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/item/"
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/item/DragonAppleItem.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/item/"
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/item/EchoAppleItem.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/item/"
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/item/EnderPearlAppleItem.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/item/"
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/item/HoneyAppleItem.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/item/"
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/item/LapislazuliAppleItem.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/item/"
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/item/NuclearAppleItem.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/item/"
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/item/OrchardCallerItem.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/item/"
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/item/PrismAppleItem.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/item/"
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/item/QuantumAppleItem.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/item/"
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/item/RewindAppleItem.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/item/"
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/item/ShakeItem.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/item/"
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/item/TntAppleItem.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/item/"
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/item/TotemAppleItem.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/item/"
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/item/VoidAppleItem.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/item/"
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/item/WitherAppleItem.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/item/"
```

- [ ] **Step 2: Fix CopperAppleItem — replace ForgeRegistries with BuiltInRegistries**

In `common/src/main/java/de/maxi/ultimate_apple_mod/item/CopperAppleItem.java`:

Remove import:
```java
import net.minecraftforge.registries.ForgeRegistries;
```

Add import:
```java
import net.minecraft.core.registries.BuiltInRegistries;
```

Change the registry lookup line inside `inventoryTick()`:
```java
// Before:
Item nextItem = ForgeRegistries.ITEMS.getValue(nextId);

// After:
Item nextItem = BuiltInRegistries.ITEM.get(nextId);
```

- [ ] **Step 3: Verify Forge still compiles**

Run: `./gradlew :forge:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```
git add common/src/main/java/de/maxi/ultimate_apple_mod/item/
git add common/src/main/java/de/maxi/ultimate_apple_mod/effect/
git add common/src/main/java/de/maxi/ultimate_apple_mod/ModRegistries.java
git add common/src/main/java/de/maxi/ultimate_apple_mod/RewindPositionCache.java
git commit -m "refactor: move effect + item classes to common, create ModRegistries scaffold"
```

---

## Task 4: Move entity classes to common + update ModRegistries references

**Files:**
- Move: `AppleBombEntity.java`, `ShakeBombEntity.java`, `TntAppleEntity.java`, `NuclearAppleEntity.java`
- Modify each to replace `ultimate_apple_modForge.*` with `ModRegistries.*`
- Modify `ShakeBombEntity` to use `RewindPositionCache` instead of `RewindTracker`

- [ ] **Step 1: Move entity files**

```
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/item/AppleBombEntity.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/item/"
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/item/TntAppleEntity.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/item/"
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/item/NuclearAppleEntity.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/item/"
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/item/ShakeBombEntity.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/item/"
```

- [ ] **Step 2: Fix AppleBombEntity**

In `common/.../item/AppleBombEntity.java`:

Remove: `import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;`
Add:    `import de.maxi.ultimate_apple_mod.ModRegistries;`

```java
// Before:
public AppleBombEntity(LivingEntity thrower, Level level) {
    super(ultimate_apple_modForge.APPLE_BOMB_ENTITY.get(), thrower, level);
}

@Override
protected Item getDefaultItem() {
    return ultimate_apple_modForge.APPLE_BOMB.get();
}

// After:
public AppleBombEntity(LivingEntity thrower, Level level) {
    super(ModRegistries.APPLE_BOMB_ENTITY.get(), thrower, level);
}

@Override
protected Item getDefaultItem() {
    return ModRegistries.APPLE_BOMB.get();
}
```

- [ ] **Step 3: Fix TntAppleEntity**

In `common/.../item/TntAppleEntity.java`:

Remove: `import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;`
Add:    `import de.maxi.ultimate_apple_mod.ModRegistries;`

```java
// Before:
public TntAppleEntity(LivingEntity thrower, Level level) {
    super(ultimate_apple_modForge.TNT_APPLE_ENTITY.get(), thrower, level);
}

@Override
protected Item getDefaultItem() {
    return ultimate_apple_modForge.TNT_APPLE.get();
}

// After:
public TntAppleEntity(LivingEntity thrower, Level level) {
    super(ModRegistries.TNT_APPLE_ENTITY.get(), thrower, level);
}

@Override
protected Item getDefaultItem() {
    return ModRegistries.TNT_APPLE.get();
}
```

- [ ] **Step 4: Fix NuclearAppleEntity**

In `common/.../item/NuclearAppleEntity.java`:

Remove: `import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;`
Add:    `import de.maxi.ultimate_apple_mod.ModRegistries;`

```java
// Before:
public NuclearAppleEntity(LivingEntity thrower, Level level) {
    super(ultimate_apple_modForge.NUCLEAR_APPLE_ENTITY.get(), thrower, level);
}

@Override
protected Item getDefaultItem() {
    return ultimate_apple_modForge.NUCLEAR_APPLE.get();
}

// After:
public NuclearAppleEntity(LivingEntity thrower, Level level) {
    super(ModRegistries.NUCLEAR_APPLE_ENTITY.get(), thrower, level);
}

@Override
protected Item getDefaultItem() {
    return ModRegistries.NUCLEAR_APPLE.get();
}
```

- [ ] **Step 5: Fix ShakeBombEntity**

In `common/.../item/ShakeBombEntity.java`:

Remove:
```java
import de.maxi.ultimate_apple_mod.event.RewindTracker;
import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
```

Add:
```java
import de.maxi.ultimate_apple_mod.ModRegistries;
import de.maxi.ultimate_apple_mod.RewindPositionCache;
```

Three replacements:

```java
// 1. Constructor
// Before:
public ShakeBombEntity(LivingEntity thrower, Level level, ItemStack shakeStack) {
    super(ultimate_apple_modForge.SHAKE_BOMB_ENTITY.get(), thrower, level);

// After:
public ShakeBombEntity(LivingEntity thrower, Level level, ItemStack shakeStack) {
    super(ModRegistries.SHAKE_BOMB_ENTITY.get(), thrower, level);
```

```java
// 2. getDefaultItem()
// Before:
protected Item getDefaultItem() {
    return ultimate_apple_modForge.SHAKE_ITEM.get();
}
// After:
protected Item getDefaultItem() {
    return ModRegistries.SHAKE_ITEM.get();
}
```

```java
// 3. Lifesteal effect in applyToTarget()
// Before:
thrower.addEffect(new MobEffectInstance(
    ultimate_apple_modForge.LIFESTEAL_EFFECT.get(), 20 * 60, 0));
// After:
thrower.addEffect(new MobEffectInstance(
    ModRegistries.LIFESTEAL.get(), 20 * 60, 0));
```

```java
// 4. RewindTracker call in applyToTarget()
// Before:
Vec3 oldPos = RewindTracker.getPositionFiveSecondsAgo(player);
// After:
Vec3 oldPos = RewindPositionCache.getPositionFiveSecondsAgo(player);
```

- [ ] **Step 6: Update forge/event/RewindTracker.java to use RewindPositionCache**

In `forge/src/main/java/de/maxi/ultimate_apple_mod/event/RewindTracker.java`:

Add import: `import de.maxi.ultimate_apple_mod.RewindPositionCache;`

Replace the entire body of `onServerTick()`:
```java
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
```

Remove the now-unused `positionHistory` map field and `getPositionFiveSecondsAgo()` method (they moved to `RewindPositionCache`).

- [ ] **Step 7: Verify Forge compiles**

Run: `./gradlew :forge:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```
git add common/src/main/java/de/maxi/ultimate_apple_mod/item/
git add common/src/main/java/de/maxi/ultimate_apple_mod/RewindPositionCache.java
git add forge/src/main/java/de/maxi/ultimate_apple_mod/event/RewindTracker.java
git commit -m "refactor: move entity classes to common, update Forge RewindTracker → RewindPositionCache"
```

---

## Task 5: Extract MixerBlockEntityBase + move Mixer block classes to common

**Files:**
- Create: `common/.../block/MixerBlockEntityBase.java` (full content of current MixerBlockEntity, minus platform-specific parts)
- Move (no changes): `MixerBlock.java`, `MixerRecipes.java`, `MixerScreen.java`
- Move + modify: `MixerMenu.java` (update imports)

- [ ] **Step 1: Create the `common/block/` directory and create MixerBlockEntityBase**

Create `common/src/main/java/de/maxi/ultimate_apple_mod/block/MixerBlockEntityBase.java`:

```java
package de.maxi.ultimate_apple_mod.block;

import de.maxi.ultimate_apple_mod.ModRegistries;
import de.maxi.ultimate_apple_mod.item.CupItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class MixerBlockEntityBase extends BlockEntity implements Container, MenuProvider {

    public static final int SLOT_CUP    = 0;
    public static final int SLOT_ING1   = 1;
    public static final int SLOT_ING2   = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int MAX_PROGRESS = 100;

    protected final NonNullList<ItemStack> items =
            NonNullList.withSize(4, ItemStack.EMPTY);

    @Nullable
    protected CompoundTag pendingShakeTag = null;

    int progress = 0;
    protected boolean stateDirty = false;

    final ContainerData data = new ContainerData() {
        @Override public int get(int i)         { return i == 0 ? progress : MAX_PROGRESS; }
        @Override public void set(int i, int v) { if (i == 0) progress = v; }
        @Override public int getCount()         { return 2; }
    };

    protected MixerBlockEntityBase(BlockPos pos, BlockState state) {
        super(ModRegistries.MIXER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.ultimate_apple_mod.mixer");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new MixerMenu(id, inv, this, this.data);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MixerBlockEntityBase be) {
        if (be.pendingShakeTag != null) {
            boolean cupPresent  = be.items.get(SLOT_CUP).getItem() instanceof CupItem;
            boolean ing1Present = !be.items.get(SLOT_ING1).isEmpty();
            boolean ing2Present = !be.items.get(SLOT_ING2).isEmpty();
            if (!cupPresent || !ing1Present || !ing2Present) {
                be.pendingShakeTag = null;
                be.progress = 0;
                be.stateDirty = true;
                be.setChanged();
                return;
            }

            be.progress++;
            if (be.progress >= MAX_PROGRESS) {
                be.consumeSlot(SLOT_CUP);
                be.consumeSlot(SLOT_ING1);
                be.consumeSlot(SLOT_ING2);

                ItemStack shake = new ItemStack(ModRegistries.SHAKE_ITEM.get());
                shake.setTag(be.pendingShakeTag.copy());
                be.items.set(SLOT_OUTPUT, shake);
                be.pendingShakeTag = null;
                be.progress = 0;
                be.stateDirty = true;
            }
            be.setChanged();
        } else {
            if (be.canStartMix()) {
                MixerRecipes.ShakeContribution c1 =
                        MixerRecipes.getContribution(be.items.get(SLOT_ING1)).get();
                MixerRecipes.ShakeContribution c2 =
                        MixerRecipes.getContribution(be.items.get(SLOT_ING2)).get();

                ResourceLocation quantumId = new ResourceLocation("ultimate_apple_mod", "quantum_apple");
                var rng = level.getRandom();
                List<MixerRecipes.ShakeContribution> pool = MixerRecipes.getRandomizableContributions();
                if (!pool.isEmpty()) {
                    if (quantumId.equals(BuiltInRegistries.ITEM.getKey(be.items.get(SLOT_ING1).getItem())))
                        c1 = pool.get(rng.nextInt(pool.size()));
                    if (quantumId.equals(BuiltInRegistries.ITEM.getKey(be.items.get(SLOT_ING2).getItem())))
                        c2 = pool.get(rng.nextInt(pool.size()));
                }

                ResourceLocation COAL_ID = new ResourceLocation("ultimate_apple_mod", "coal_apple");
                ResourceLocation TNT_ID  = new ResourceLocation("ultimate_apple_mod", "tnt_apple");
                ResourceLocation BOMB_ID = new ResourceLocation("ultimate_apple_mod", "apple_bomb");
                ResourceLocation id1 = BuiltInRegistries.ITEM.getKey(be.items.get(SLOT_ING1).getItem());
                ResourceLocation id2 = BuiltInRegistries.ITEM.getKey(be.items.get(SLOT_ING2).getItem());

                boolean coalTnt  = (COAL_ID.equals(id1) && TNT_ID.equals(id2)) || (TNT_ID.equals(id1) && COAL_ID.equals(id2));
                boolean coalBomb = (COAL_ID.equals(id1) && BOMB_ID.equals(id2)) || (BOMB_ID.equals(id1) && COAL_ID.equals(id2));
                boolean coalMix  = COAL_ID.equals(id1) || COAL_ID.equals(id2);

                if (coalTnt) {
                    if (COAL_ID.equals(id1)) c1 = emptyContribution();
                    else                     c2 = emptyContribution();
                } else if (coalBomb) {
                    if (COAL_ID.equals(id1)) c1 = withDoubledDuration(c1);
                    else                     c2 = withDoubledDuration(c2);
                }

                CompoundTag shakeTag = buildShakeNbt(c1, c2);
                if (coalMix && !coalTnt) shakeTag.putBoolean("isCoalFuel", true);
                be.pendingShakeTag = shakeTag;
                be.progress = 0;
                be.stateDirty = true;
                be.setChanged();
            }
        }

        if (be.stateDirty) {
            be.syncBlockState(level, pos, state);
            be.stateDirty = false;
            be.setChanged();
        }
    }

    private boolean canStartMix() {
        if (pendingShakeTag != null) return false;
        if (!items.get(SLOT_OUTPUT).isEmpty()) return false;
        ItemStack cup  = items.get(SLOT_CUP);
        ItemStack ing1 = items.get(SLOT_ING1);
        ItemStack ing2 = items.get(SLOT_ING2);
        if (!(cup.getItem() instanceof CupItem)) return false;
        if (ing1.isEmpty() || ing2.isEmpty()) return false;
        if (ing1.getItem() == ing2.getItem()) return false;
        if (MixerRecipes.areIncompatible(ing1, ing2)) return false;
        return MixerRecipes.getContribution(ing1).isPresent()
                && MixerRecipes.getContribution(ing2).isPresent();
    }

    private static CompoundTag buildShakeNbt(MixerRecipes.ShakeContribution c1,
                                             MixerRecipes.ShakeContribution c2) {
        boolean clearsEffects = c1.clearsEffects() || c2.clearsEffects();
        Map<ResourceLocation, MixerRecipes.EffectData> merged = new LinkedHashMap<>();
        if (clearsEffects) {
            if (c1.clearsEffects()) for (MixerRecipes.EffectData e : c1.effects()) mergeEffect(merged, e);
            if (c2.clearsEffects()) for (MixerRecipes.EffectData e : c2.effects()) mergeEffect(merged, e);
        } else {
            for (MixerRecipes.EffectData e : c1.effects()) mergeEffect(merged, e);
            for (MixerRecipes.EffectData e : c2.effects()) mergeEffect(merged, e);
        }
        int dragonCharges  = clearsEffects ? 0 : (c1.dragonCharges() + c2.dragonCharges());
        boolean lifesteal    = !clearsEffects && (c1.lifesteal()    || c2.lifesteal());
        boolean witherCurse  = !clearsEffects && (c1.witherCurse()  || c2.witherCurse());
        boolean voidLaunch   = !clearsEffects && (c1.voidLaunch()   || c2.voidLaunch());
        boolean rewindEffect = !clearsEffects && (c1.rewindEffect() || c2.rewindEffect());
        boolean orchardSpawn = !clearsEffects && (c1.orchardSpawn() || c2.orchardSpawn());
        boolean enderTeleport = !clearsEffects && (c1.enderTeleport() || c2.enderTeleport());
        boolean isBomb = c1.isBomb() || c2.isBomb();
        boolean isTntExplosion = c1.isTntExplosion() || c2.isTntExplosion();
        double rawMultiplier = Math.max(c1.durationMultiplier(), c2.durationMultiplier());
        final double durationFactor;
        if (clearsEffects && rawMultiplier >= 2.0) { merged.clear(); durationFactor = 1.0; }
        else if (clearsEffects) { durationFactor = 0.80; }
        else { durationFactor = 1.20 * rawMultiplier; }

        CompoundTag tag = new CompoundTag();
        ListTag effectsList = new ListTag();
        for (MixerRecipes.EffectData e : merged.values()) {
            CompoundTag et = new CompoundTag();
            et.putString("id", e.id().toString());
            et.putInt("duration", (int)(e.duration() * durationFactor));
            et.putInt("amplifier", e.amplifier());
            effectsList.add(et);
        }
        tag.put("effects", effectsList);
        tag.putInt("dragonCharges", dragonCharges);
        tag.putBoolean("lifesteal", lifesteal);
        tag.putBoolean("witherCurse", witherCurse);
        tag.putBoolean("clearsEffects", clearsEffects);
        tag.putBoolean("voidLaunch", voidLaunch);
        tag.putBoolean("rewindEffect", rewindEffect);
        tag.putBoolean("orchardSpawn", orchardSpawn);
        tag.putBoolean("enderTeleport", enderTeleport);
        tag.putBoolean("isBomb", isBomb);
        tag.putBoolean("isTntExplosion", isTntExplosion);
        return tag;
    }

    private static MixerRecipes.ShakeContribution emptyContribution() {
        return new MixerRecipes.ShakeContribution(
                List.of(), 0, false, false, false, 1.0,
                false, false, false, false, false, false);
    }

    private static MixerRecipes.ShakeContribution withDoubledDuration(MixerRecipes.ShakeContribution c) {
        List<MixerRecipes.EffectData> doubled = c.effects().stream()
                .map(e -> new MixerRecipes.EffectData(e.id(), e.duration() * 2, e.amplifier()))
                .collect(java.util.stream.Collectors.toList());
        return new MixerRecipes.ShakeContribution(doubled,
                c.dragonCharges(), c.lifesteal(), c.witherCurse(),
                c.clearsEffects(), c.durationMultiplier(),
                c.voidLaunch(), c.rewindEffect(), c.orchardSpawn(),
                c.enderTeleport(), c.isBomb(), c.isTntExplosion());
    }

    private static void mergeEffect(Map<ResourceLocation, MixerRecipes.EffectData> map,
                                    MixerRecipes.EffectData e) {
        if (map.containsKey(e.id())) {
            MixerRecipes.EffectData ex = map.get(e.id());
            map.put(e.id(), new MixerRecipes.EffectData(e.id(),
                    Math.max(ex.duration(), e.duration()), Math.max(ex.amplifier(), e.amplifier())));
        } else {
            map.put(e.id(), e);
        }
    }

    protected void consumeSlot(int slot) {
        ItemStack stack = items.get(slot);
        stack.shrink(1);
        if (stack.isEmpty()) items.set(slot, ItemStack.EMPTY);
    }

    private void syncBlockState(Level level, BlockPos pos, BlockState state) {
        boolean hasJar   = (pendingShakeTag != null) || !items.get(SLOT_CUP).isEmpty()
                         || !items.get(SLOT_ING1).isEmpty() || !items.get(SLOT_ING2).isEmpty();
        boolean hasShake = !items.get(SLOT_OUTPUT).isEmpty();
        BlockState newState = state
                .setValue(MixerBlock.HAS_JAR,   hasJar)
                .setValue(MixerBlock.HAS_SHAKE, hasShake);
        if (!newState.equals(state)) level.setBlock(pos, newState, 3);
    }

    @Override public int  getContainerSize()           { return items.size(); }
    @Override public boolean isEmpty()                 { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot)       { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int count) {
        ItemStack r = ContainerHelper.removeItem(items, slot, count);
        if (!r.isEmpty()) { stateDirty = true; setChanged(); }
        return r;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        stateDirty = true; setChanged();
    }
    @Override public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) return false;
        return player.distanceToSqr(worldPosition.getX()+0.5, worldPosition.getY()+0.5, worldPosition.getZ()+0.5) <= 64.0;
    }
    @Override public void clearContent() { items.clear(); stateDirty = true; setChanged(); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_CUP    -> stack.getItem() instanceof CupItem && items.get(SLOT_CUP).isEmpty()
                             && items.get(SLOT_OUTPUT).isEmpty() && pendingShakeTag == null;
            case SLOT_ING1   -> {
                if (MixerRecipes.getContribution(stack).isEmpty()) yield false;
                ItemStack ing2 = items.get(SLOT_ING2);
                yield ing2.isEmpty() || (ing2.getItem() != stack.getItem() && !MixerRecipes.areIncompatible(stack, ing2));
            }
            case SLOT_ING2   -> {
                if (MixerRecipes.getContribution(stack).isEmpty()) yield false;
                ItemStack ing1 = items.get(SLOT_ING1);
                yield ing1.isEmpty() || (ing1.getItem() != stack.getItem() && !MixerRecipes.areIncompatible(stack, ing1));
            }
            default -> false;
        };
    }

    @Override public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("Progress", progress);
        if (pendingShakeTag != null) tag.put("PendingShake", pendingShakeTag.copy());
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
        progress = tag.getInt("Progress");
        pendingShakeTag = tag.contains("PendingShake", Tag.TAG_COMPOUND)
                ? tag.getCompound("PendingShake").copy() : null;
    }
}
```

- [ ] **Step 2: Move MixerBlock, MixerRecipes, MixerScreen (no code changes)**

```
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/forge/block/MixerBlock.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/block/"
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/forge/block/MixerRecipes.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/block/"
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/forge/block/MixerScreen.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/block/"
```

Fix package declarations in each moved file from `de.maxi.ultimate_apple_mod.forge.block` to `de.maxi.ultimate_apple_mod.block`.

- [ ] **Step 3: Move MixerMenu + fix imports**

```
git mv "forge/src/main/java/de/maxi/ultimate_apple_mod/forge/block/MixerMenu.java" \
       "common/src/main/java/de/maxi/ultimate_apple_mod/block/"
```

In `common/.../block/MixerMenu.java`:

Fix package: `package de.maxi.ultimate_apple_mod.block;`

Remove: `import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;`
Add:    `import de.maxi.ultimate_apple_mod.ModRegistries;`

Remove: `import de.maxi.ultimate_apple_mod.forge.block.MixerBlockEntity;` (not needed — slot constants are now in MixerBlockEntityBase)

Update super constructor call:
```java
// Before:
super(ultimate_apple_modForge.MIXER_MENU_TYPE.get(), id);
// After:
super((net.minecraft.world.inventory.MenuType<MixerMenu>) ModRegistries.MIXER_MENU_TYPE.get(), id);
```

Update all `MixerBlockEntity.SLOT_*` references to `MixerBlockEntityBase.SLOT_*`:
```java
// Before:
addSlot(new Slot(container, MixerBlockEntity.SLOT_CUP, 26, 35) {
addSlot(new Slot(container, MixerBlockEntity.SLOT_ING1, 62, 17) {
addSlot(new Slot(container, MixerBlockEntity.SLOT_ING2, 62, 53) {
addSlot(new Slot(container, MixerBlockEntity.SLOT_OUTPUT, MixerScreen.OUT_X, MixerScreen.OUT_Y) {

// After:
addSlot(new Slot(container, MixerBlockEntityBase.SLOT_CUP, 26, 35) {
addSlot(new Slot(container, MixerBlockEntityBase.SLOT_ING1, 62, 17) {
addSlot(new Slot(container, MixerBlockEntityBase.SLOT_ING2, 62, 53) {
addSlot(new Slot(container, MixerBlockEntityBase.SLOT_OUTPUT, MixerScreen.OUT_X, MixerScreen.OUT_Y) {
```

- [ ] **Step 4: Verify Forge compiles**

Run: `./gradlew :forge:compileJava`
Expected: BUILD SUCCESSFUL (Forge's MixerBlockEntity will fail until Task 6 — fix it if needed by keeping the old file temporarily)

- [ ] **Step 5: Commit**

```
git add common/src/main/java/de/maxi/ultimate_apple_mod/block/
git commit -m "refactor: extract MixerBlockEntityBase and move Mixer block classes to common"
```

---

## Task 6: Update Forge to use common classes

**Files:**
- Modify: `forge/.../forge/block/MixerBlockEntity.java` → slim subclass of MixerBlockEntityBase
- Modify: `forge/.../forge/block/ModBlocks.java` → update type reference
- Modify: `forge/.../forge/ultimate_apple_modForge.java` → fill ModRegistries fields

- [ ] **Step 1: Slim down forge MixerBlockEntity to extend MixerBlockEntityBase**

Replace the entire content of `forge/src/main/java/de/maxi/ultimate_apple_mod/forge/block/MixerBlockEntity.java`:

```java
package de.maxi.ultimate_apple_mod.forge.block;

import de.maxi.ultimate_apple_mod.block.MixerBlockEntityBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MixerBlockEntity extends MixerBlockEntityBase {

    public MixerBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }
}
```

Note: No `IItemHandler` capability is needed — the original Forge class already implemented vanilla `Container` directly, not `IItemHandler`. The base class handles all logic.

- [ ] **Step 2: Update forge ModBlocks MIXER_BLOCK_ENTITY type**

In `forge/src/main/java/de/maxi/ultimate_apple_mod/forge/block/ModBlocks.java`, the `MIXER_BLOCK_ENTITY` registration is in `ultimate_apple_modForge.java` — verify it still references `MixerBlockEntity::new` (which still works since Forge's `MixerBlockEntity` extends `MixerBlockEntityBase`).

Check `forge/.../forge/ultimate_apple_modForge.java` line:
```java
public static final RegistryObject<BlockEntityType<MixerBlockEntity>> MIXER_BLOCK_ENTITY =
    BLOCK_ENTITIES.register("mixer", () -> BlockEntityType.Builder
        .of(MixerBlockEntity::new, ModBlocks.MIXER.get())
        .build(null));
```
This is correct as-is — `MixerBlockEntity` is still a concrete class.

Also update the `serverTick` registration in `MixerBlock.java`. Open `common/.../block/MixerBlock.java` and find where it calls `serverTick`. The tick is registered with `BlockEntityType.Builder` — it should use `MixerBlockEntityBase::serverTick`. If the Forge `MixerBlock.java` (now in common) references `MixerBlockEntity.serverTick`, update it:

In `common/.../block/MixerBlock.java`, find the EntityBlock.getTicker() method (or wherever serverTick is wired) and ensure it references `MixerBlockEntityBase::serverTick`.

- [ ] **Step 3: Fill ModRegistries fields in ultimate_apple_modForge constructor**

In `forge/src/main/java/de/maxi/ultimate_apple_mod/forge/ultimate_apple_modForge.java`, add at the END of the constructor body (after all register calls):

```java
// Add this import at the top of the file:
import de.maxi.ultimate_apple_mod.ModRegistries;

// Add this block at the end of the constructor, after the last register call:
// ── Fill platform-neutral ModRegistries ──────────────────────────────────
ModRegistries.CURSE_OF_ROTTEN         = CURSE_OF_ROTTEN;
ModRegistries.MOON_GRAVITY            = MOON_GRAVITY_EFFECT;
ModRegistries.LIFESTEAL               = LIFESTEAL_EFFECT;
ModRegistries.TOTEM_PROTECTION        = TOTEM_PROTECTION_EFFECT;
ModRegistries.TIME_FREEZE             = TIME_FREEZE_EFFECT;

ModRegistries.DIAMOND_APPLE           = DIAMOND_APPLE;
ModRegistries.LAPISLAZULI_APPLE       = LAPISLAZULI_APPLE;
ModRegistries.EMERALD_APPLE           = EMERALD_APPLE;
ModRegistries.REDSTONE_APPLE          = REDSTONE_APPLE;
ModRegistries.NETHERITE_APPLE         = NETHERITE_APPLE;
ModRegistries.IRON_APPLE              = IRON_APPLE;
ModRegistries.ROTTEN_APPLE            = ROTTEN_APPLE;
ModRegistries.ROASTED_APPLE           = ROASTED_APPLE;
ModRegistries.BAKED_APPLE             = BAKED_APPLE;
ModRegistries.BURNT_APPLE             = BURNT_APPLE;
ModRegistries.BLAZE_APPLE             = BLAZE_APPLE;
ModRegistries.BIRNE                   = BIRNE;
ModRegistries.COPPER_APPLE            = COPPER_APPLE;
ModRegistries.EXPOSED_COPPER_APPLE    = EXPOSED_COPPER_APPLE;
ModRegistries.WEATHERED_COPPER_APPLE  = WEATHERED_COPPER_APPLE;
ModRegistries.OXIDIZED_COPPER_APPLE   = OXIDIZED_COPPER_APPLE;
ModRegistries.WAXED_COPPER_APPLE      = WAXED_COPPER_APPLE;
ModRegistries.WAXED_EXPOSED_COPPER_APPLE   = WAXED_EXPOSED_COPPER_APPLE;
ModRegistries.WAXED_WEATHERED_COPPER_APPLE = WAXED_WEATHERED_COPPER_APPLE;
ModRegistries.WAXED_OXIDIZED_COPPER_APPLE  = WAXED_OXIDIZED_COPPER_APPLE;
ModRegistries.ENDER_PEARL_APPLE       = ENDER_PEARL_APPLE;
ModRegistries.MOON_APPLE              = MOON_APPLE;
ModRegistries.ORCHARD_APPLE           = ORCHARD_APPLE;
ModRegistries.ECHO_APPLE              = ECHO_APPLE;
ModRegistries.REWIND_APPLE            = REWIND_APPLE;
ModRegistries.APPLE_BOMB              = APPLE_BOMB;
ModRegistries.COAL_APPLE              = COAL_APPLE;
ModRegistries.TNT_APPLE               = TNT_APPLE;
ModRegistries.NUCLEAR_APPLE           = NUCLEAR_APPLE;
ModRegistries.WITHER_APPLE            = WITHER_APPLE;
ModRegistries.HONEY_APPLE             = HONEY_APPLE;
ModRegistries.DRAGON_APPLE            = DRAGON_APPLE;
ModRegistries.NETHER_STAR_APPLE       = NETHER_STAR_APPLE;
ModRegistries.DIRT_APPLE              = DIRT_APPLE;
ModRegistries.TOTEM_APPLE             = TOTEM_APPLE;
ModRegistries.QUANTUM_APPLE           = QUANTUM_APPLE;
ModRegistries.VOID_APPLE              = VOID_APPLE;
ModRegistries.TIME_FREEZE_APPLE       = TIME_FREEZE_APPLE;
ModRegistries.LONGEVITY_APPLE         = LONGEVITY_APPLE;
ModRegistries.PRISM_APPLE             = PRISM_APPLE;
ModRegistries.BANANA                  = BANANA;
ModRegistries.CUP_ITEM                = CUP_ITEM;
ModRegistries.SHAKE_ITEM              = SHAKE_ITEM;

ModRegistries.APPLE_BOMB_ENTITY       = APPLE_BOMB_ENTITY;
ModRegistries.SHAKE_BOMB_ENTITY       = SHAKE_BOMB_ENTITY;
ModRegistries.TNT_APPLE_ENTITY        = TNT_APPLE_ENTITY;
ModRegistries.NUCLEAR_APPLE_ENTITY    = NUCLEAR_APPLE_ENTITY;

ModRegistries.MIXER                   = ModBlocks.MIXER;
ModRegistries.MIXER_ITEM              = ModBlocks.MIXER_ITEM;
ModRegistries.MIXER_BLOCK_ENTITY      = MIXER_BLOCK_ENTITY::get;
ModRegistries.MIXER_MENU_TYPE         = MIXER_MENU_TYPE;
```

Note: `MIXER_BLOCK_ENTITY` is `RegistryObject<BlockEntityType<MixerBlockEntity>>` but `ModRegistries.MIXER_BLOCK_ENTITY` is `Supplier<BlockEntityType<?>>`. Use `MIXER_BLOCK_ENTITY::get` (method reference) to widen the generic type.

- [ ] **Step 4: Verify Forge full build**

Run: `./gradlew :forge:build`
Expected: BUILD SUCCESSFUL — the Forge jar should produce without errors.

- [ ] **Step 5: Commit**

```
git add forge/src/main/java/de/maxi/ultimate_apple_mod/forge/block/MixerBlockEntity.java
git add forge/src/main/java/de/maxi/ultimate_apple_mod/forge/ultimate_apple_modForge.java
git commit -m "refactor: slim Forge MixerBlockEntity to subclass, fill ModRegistries from Forge registrations"
```

---

## Task 7: Migrate resources from forge → common

**Files:** All `assets/` and `data/` directories under `forge/src/main/resources/`

- [ ] **Step 1: Move assets**

```
git mv "forge/src/main/resources/assets" "common/src/main/resources/assets"
```

- [ ] **Step 2: Move data**

```
git mv "forge/src/main/resources/data" "common/src/main/resources/data"
```

- [ ] **Step 3: Verify forge/src/main/resources only contains META-INF**

```
ls forge/src/main/resources/
```

Expected output: only `META-INF/` folder (containing `mods.toml` and optionally `accesstransformer.cfg`).

- [ ] **Step 4: Verify Forge build still works (resources bundled via Architectury)**

Run: `./gradlew :forge:build`
Expected: BUILD SUCCESSFUL — Architectury automatically includes `common/src/main/resources` in the Forge jar.

- [ ] **Step 5: Commit**

```
git add common/src/main/resources/assets/
git add common/src/main/resources/data/
git commit -m "refactor: migrate all resources from forge to common"
```

---

## Task 8: Create Fabric main registration

**Files:**
- Modify: `fabric/src/main/java/de/maxi/ultimate_apple_mod/fabric/ultimate_apple_modFabric.java`
- Create: `fabric/src/main/java/de/maxi/ultimate_apple_mod/fabric/block/ModBlocks.java`

- [ ] **Step 1: Create fabric/block/ModBlocks.java**

```java
package de.maxi.ultimate_apple_mod.fabric.block;

import de.maxi.ultimate_apple_mod.block.MixerBlock;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModBlocks {

    public static Block  MIXER;
    public static Item   MIXER_ITEM;

    public static void register() {
        MIXER = Registry.register(BuiltInRegistries.BLOCK,
            new ResourceLocation(ultimate_apple_mod.MOD_ID, "mixer"), new MixerBlock());
        MIXER_ITEM = Registry.register(BuiltInRegistries.ITEM,
            new ResourceLocation(ultimate_apple_mod.MOD_ID, "mixer"),
            new BlockItem(MIXER, new Item.Properties()));
    }
}
```

- [ ] **Step 2: Replace fabric/ultimate_apple_modFabric.java with full registration**

```java
package de.maxi.ultimate_apple_mod.fabric;

import de.maxi.ultimate_apple_mod.ModRegistries;
import de.maxi.ultimate_apple_mod.block.MixerBlockEntityBase;
import de.maxi.ultimate_apple_mod.block.MixerMenu;
import de.maxi.ultimate_apple_mod.effect.CurseOfRotten;
import de.maxi.ultimate_apple_mod.effect.LifestealEffect;
import de.maxi.ultimate_apple_mod.effect.MoonGravityEffect;
import de.maxi.ultimate_apple_mod.effect.TimeFreezeEffect;
import de.maxi.ultimate_apple_mod.effect.TotemProtectionEffect;
import de.maxi.ultimate_apple_mod.fabric.block.ModBlocks;
import de.maxi.ultimate_apple_mod.fabric.event.FabricEventRegistrar;
import de.maxi.ultimate_apple_mod.item.*;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.network.chat.Component;

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
                .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 20*60, 2), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20*20, 1), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20*30, 1), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 20*60, 0), 1f).build())
            .stacksTo(64)));
        Item lapislazuliApple = reg("lapislazuli_apple", new LapislazuliAppleItem());
        Item emeraldApple = reg("emerald_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(8).saturationMod(0.9f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.LUCK, 20*60, 1), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, 20*30, 0), 1f).build())
            .stacksTo(64)));
        Item redstoneApple = reg("redstone_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(8).saturationMod(0.9f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20*20, 2), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 20*20, 2), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.GLOWING, 20*20, 0), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20*15, 0), 1f).build())
            .stacksTo(64)));
        Item netheriteApple = reg("netherite_apple", new Item(new Item.Properties().fireResistant()
            .food(new FoodProperties.Builder().nutrition(10).saturationMod(1.0f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20*120, 2), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20*120, 0), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 20*120, 3), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20*30, 2), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20*30, 1), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 20*120, 2), 1f).build())
            .stacksTo(64)));
        Item ironApple = reg("iron_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(6).saturationMod(0.7f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 20*30, 0), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20*10, 0), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20*15, 0), 1f).build())
            .stacksTo(64)));
        Item rottenApple = reg("rotten_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.1f).alwaysEat()
                .effect(() -> new MobEffectInstance(curseOfRotten, 400, 0, false, true), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 400, 0), 1f).build())
            .stacksTo(64)));
        Item roastedApple = reg("roasted_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.1f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 20*20, 0), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.SATURATION, 20*10, 0), 1f).build())
            .stacksTo(64)));
        Item bakedApple = reg("baked_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.1f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20*5, 0), 1f).build())
            .stacksTo(64)));
        Item burntApple = reg("burnt_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.1f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 20*15, 1), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 20*5, 0), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20*5, 0), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20*15, 0), 1f).build())
            .stacksTo(64)));
        Item blazeApple = reg("blaze_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(6).saturationMod(0.5f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20*5, 0), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20*5, 0), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20*15, 0), 1f).build())
            .stacksTo(64)));
        Item birne = reg("pear_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.1f)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20*15, 0), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.SATURATION, 20*5, 0), 1f).build())
            .stacksTo(64)));
        Item copperApple = reg("copper_apple", new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(5).saturationMod(0.6f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 20*25, 1), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20*25, 0), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20*25, 0), 1f).build())
            .stacksTo(64), 0, false));
        Item exposedCopperApple = reg("exposed_copper_apple", new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.5f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 20*20, 0), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20*15, 0), 1f).build())
            .stacksTo(64), 1, false));
        Item weatheredCopperApple = reg("weathered_copper_apple", new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(3).saturationMod(0.3f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 20*10, 0), 1f).build())
            .stacksTo(64), 2, false));
        Item oxidizedCopperApple = reg("oxidized_copper_apple", new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.1f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20*5, 0), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 20*5, 0), 1f).build())
            .stacksTo(64), 3, false));
        Item waxedCopperApple = reg("waxed_copper_apple", new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(5).saturationMod(0.6f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 20*25, 1), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20*25, 0), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20*25, 0), 1f).build())
            .stacksTo(64), 0, true));
        Item waxedExposedCopperApple = reg("waxed_exposed_copper_apple", new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.5f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 20*20, 0), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20*15, 0), 1f).build())
            .stacksTo(64), 1, true));
        Item waxedWeatheredCopperApple = reg("waxed_weathered_copper_apple", new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(3).saturationMod(0.3f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 20*10, 0), 1f).build())
            .stacksTo(64), 2, true));
        Item waxedOxidizedCopperApple = reg("waxed_oxidized_copper_apple", new CopperAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.1f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20*5, 0), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 20*5, 0), 1f).build())
            .stacksTo(64), 3, true));
        Item enderPearlApple = reg("ender_pearl_apple", new EnderPearlAppleItem());
        Item moonApple = reg("moon_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(6).saturationMod(0.6f).alwaysEat()
                .effect(() -> new MobEffectInstance(moonGravity, 20*30, 0), 1f).build())
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
                .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 20*30, 2), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 20*10, 0), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20*15, 1), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.BLINDNESS, 20*5, 0), 1f).build())
            .stacksTo(64)));
        Item tntApple = reg("tnt_apple", new TntAppleItem(new Item.Properties().stacksTo(16)));
        Item nuclearApple = reg("nuclear_apple",
            new NuclearAppleItem(new Item.Properties().stacksTo(1).fireResistant()));
        Item witherApple = reg("wither_apple", new WitherAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(6).saturationMod(0.6f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 20*30, 2), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20*10, 1), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20*5, 1), 1f).build())
            .stacksTo(64)));
        Item honeyApple = reg("honey_apple", new HoneyAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.6f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20*5, 0), 1f).build())
            .stacksTo(64)));
        Item dragonApple = reg("dragon_apple", new DragonAppleItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(8).saturationMod(0.8f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 20*10, 3), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20*10, 1), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20*10, 2), 1f).build())
            .stacksTo(64)));
        Item netherStarApple = reg("nether_star_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(10).saturationMod(1.0f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 4), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 20*30, 3), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20*10, 2), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20*5, 3), 1f).build())
            .stacksTo(1)));
        Item dirtApple = reg("dirt_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.0f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 20*30, 2), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 20*10, 0), 1f).build())
            .stacksTo(64)));
        Item totemApple  = reg("totem_apple",  new TotemAppleItem());
        Item quantumApple= reg("quantum_apple", new QuantumAppleItem());
        Item voidApple   = reg("void_apple",   new VoidAppleItem());
        Item timeFreezeApple = reg("time_freeze_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.4f).alwaysEat()
                .effect(() -> new MobEffectInstance(timeFreeze, 20*30, 0), 1f).build())
            .stacksTo(64)));
        Item longevityApple = reg("longevity_apple", new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(6).saturationMod(0.6f).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 20*120, 3), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 20*60, 0), 1f)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20*15, 0), 1f).build())
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

        de.maxi.ultimate_apple_mod.fabric.block.MixerBlockEntity[] beHolder = new de.maxi.ultimate_apple_mod.fabric.block.MixerBlockEntity[0];
        BlockEntityType<de.maxi.ultimate_apple_mod.fabric.block.MixerBlockEntity> mixerBE =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, rl("mixer"),
                BlockEntityType.Builder.of(de.maxi.ultimate_apple_mod.fabric.block.MixerBlockEntity::new,
                    ModBlocks.MIXER).build(null));
        MenuType<MixerMenu> mixerMenu = Registry.register(BuiltInRegistries.MENU,
            rl("mixer"),
            new MenuType<>((id, inv) -> new MixerMenu(id, inv,
                new net.minecraft.world.SimpleContainer(4),
                new net.minecraft.world.inventory.SimpleContainerData(2))));

        // ── 5. Fill ModRegistries ─────────────────────────────────────────────
        ModRegistries.DIAMOND_APPLE           = () -> diamondApple;
        ModRegistries.LAPISLAZULI_APPLE       = () -> lapislazuliApple;
        ModRegistries.EMERALD_APPLE           = () -> emeraldApple;
        ModRegistries.REDSTONE_APPLE          = () -> redstoneApple;
        ModRegistries.NETHERITE_APPLE         = () -> netheriteApple;
        ModRegistries.IRON_APPLE              = () -> ironApple;
        ModRegistries.ROTTEN_APPLE            = () -> rottenApple;
        ModRegistries.ROASTED_APPLE           = () -> roastedApple;
        ModRegistries.BAKED_APPLE             = () -> bakedApple;
        ModRegistries.BURNT_APPLE             = () -> burntApple;
        ModRegistries.BLAZE_APPLE             = () -> blazeApple;
        ModRegistries.BIRNE                   = () -> birne;
        ModRegistries.COPPER_APPLE            = () -> copperApple;
        ModRegistries.EXPOSED_COPPER_APPLE    = () -> exposedCopperApple;
        ModRegistries.WEATHERED_COPPER_APPLE  = () -> weatheredCopperApple;
        ModRegistries.OXIDIZED_COPPER_APPLE   = () -> oxidizedCopperApple;
        ModRegistries.WAXED_COPPER_APPLE      = () -> waxedCopperApple;
        ModRegistries.WAXED_EXPOSED_COPPER_APPLE   = () -> waxedExposedCopperApple;
        ModRegistries.WAXED_WEATHERED_COPPER_APPLE = () -> waxedWeatheredCopperApple;
        ModRegistries.WAXED_OXIDIZED_COPPER_APPLE  = () -> waxedOxidizedCopperApple;
        ModRegistries.ENDER_PEARL_APPLE       = () -> enderPearlApple;
        ModRegistries.MOON_APPLE              = () -> moonApple;
        ModRegistries.ORCHARD_APPLE           = () -> orchardApple;
        ModRegistries.ECHO_APPLE              = () -> echoApple;
        ModRegistries.REWIND_APPLE            = () -> rewindApple;
        ModRegistries.APPLE_BOMB              = () -> appleBomb;
        ModRegistries.COAL_APPLE              = () -> coalApple;
        ModRegistries.TNT_APPLE               = () -> tntApple;
        ModRegistries.NUCLEAR_APPLE           = () -> nuclearApple;
        ModRegistries.WITHER_APPLE            = () -> witherApple;
        ModRegistries.HONEY_APPLE             = () -> honeyApple;
        ModRegistries.DRAGON_APPLE            = () -> dragonApple;
        ModRegistries.NETHER_STAR_APPLE       = () -> netherStarApple;
        ModRegistries.DIRT_APPLE              = () -> dirtApple;
        ModRegistries.TOTEM_APPLE             = () -> totemApple;
        ModRegistries.QUANTUM_APPLE           = () -> quantumApple;
        ModRegistries.VOID_APPLE              = () -> voidApple;
        ModRegistries.TIME_FREEZE_APPLE       = () -> timeFreezeApple;
        ModRegistries.LONGEVITY_APPLE         = () -> longevityApple;
        ModRegistries.PRISM_APPLE             = () -> prismApple;
        ModRegistries.BANANA                  = () -> banana;
        ModRegistries.CUP_ITEM                = () -> cupItem;
        ModRegistries.SHAKE_ITEM              = () -> shakeItem;
        ModRegistries.APPLE_BOMB_ENTITY       = () -> appleBombEntity;
        ModRegistries.SHAKE_BOMB_ENTITY       = () -> shakeBombEntity;
        ModRegistries.TNT_APPLE_ENTITY        = () -> tntAppleEntity;
        ModRegistries.NUCLEAR_APPLE_ENTITY    = () -> nuclearAppleEntity;
        ModRegistries.MIXER                   = () -> ModBlocks.MIXER;
        ModRegistries.MIXER_ITEM              = () -> ModBlocks.MIXER_ITEM;
        ModRegistries.MIXER_BLOCK_ENTITY      = () -> mixerBE;
        ModRegistries.MIXER_MENU_TYPE         = () -> mixerMenu;

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

        // ── 8. Register events ────────────────────────────────────────────────
        FabricEventRegistrar.register();
    }

    private static Item reg(String id, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, rl(id), item);
    }

    private static ResourceLocation rl(String path) {
        return new ResourceLocation(ultimate_apple_mod.MOD_ID, path);
    }
}
```

- [ ] **Step 3: Verify Fabric compiles (will fail on missing classes — that's expected)**

Run: `./gradlew :fabric:compileJava`

The compilation will fail because `FabricEventRegistrar`, `FabricModClient`, etc. don't exist yet. Check that it fails ONLY on missing class references (not on syntax or import errors in the registration code above).

- [ ] **Step 4: Commit**

```
git add fabric/src/main/java/de/maxi/ultimate_apple_mod/fabric/ultimate_apple_modFabric.java
git add fabric/src/main/java/de/maxi/ultimate_apple_mod/fabric/block/ModBlocks.java
git commit -m "feat(fabric): create full Fabric registration — items, effects, entities, blocks, creative tab"
```

---

## Task 9: Create Fabric MixerBlockEntity

**Files:**
- Create: `fabric/src/main/java/de/maxi/ultimate_apple_mod/fabric/block/MixerBlockEntity.java`

- [ ] **Step 1: Create Fabric MixerBlockEntity implementing SidedInventory**

```java
package de.maxi.ultimate_apple_mod.fabric.block;

import de.maxi.ultimate_apple_mod.block.MixerBlockEntityBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fabric subclass of MixerBlockEntityBase.
 * Implements WorldlyContainer (Fabric's sided inventory equivalent)
 * so hoppers and other automation can interact with the mixer.
 * No slot restrictions — any item can enter or exit any slot from any side.
 */
public class MixerBlockEntity extends MixerBlockEntityBase implements WorldlyContainer {

    private static final int[] ALL_SLOTS = {SLOT_CUP, SLOT_ING1, SLOT_ING2, SLOT_OUTPUT};

    public MixerBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return ALL_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return true;
    }
}
```

Note: Fabric 1.20.1 uses `WorldlyContainer` (vanilla interface) rather than a Fabric-specific `SidedInventory`. It is found in `net.minecraft.world.WorldlyContainer`.

- [ ] **Step 2: Verify the class compiles in isolation**

Run: `./gradlew :fabric:compileJava`
Expected: Still fails on missing FabricEventRegistrar etc., but MixerBlockEntity itself compiles fine.

---

## Task 10: Create Fabric event handlers + hitbox mixin

**Files:**
- Create: `fabric/.../fabric/event/FabricEventRegistrar.java`
- Create: `fabric/.../fabric/event/FabricMobDropHandler.java`
- Create: `fabric/.../fabric/event/FabricPlayerEffectHandler.java`
- Create: `fabric/.../fabric/event/FabricLootTableHandler.java`
- Create: `fabric/.../fabric/event/FabricDecayHandler.java`
- Create: `fabric/.../fabric/event/FabricTntAppleHandler.java`
- Create: `fabric/.../fabric/event/FabricBabyZombieHandler.java`
- Create: `common/.../mixin/LivingEntitySizeMixin.java`
- Modify: `common/src/main/resources/ultimate_apple_mod.mixins.json`

- [ ] **Step 1: Create FabricEventRegistrar**

```java
package de.maxi.ultimate_apple_mod.fabric.event;

public class FabricEventRegistrar {
    public static void register() {
        FabricMobDropHandler.register();
        FabricPlayerEffectHandler.register();
        FabricLootTableHandler.register();
        FabricDecayHandler.register();
        FabricTntAppleHandler.register();
        FabricBabyZombieHandler.register();
    }
}
```

- [ ] **Step 2: Create FabricMobDropHandler**

```java
package de.maxi.ultimate_apple_mod.fabric.event;

import de.maxi.ultimate_apple_mod.ModRegistries;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Random;

public class FabricMobDropHandler {

    private static final Random RNG = new Random();

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof WitherBoss) {
                if (RNG.nextDouble() < 0.5) {
                    spawnAt(entity, new ItemStack(ModRegistries.WITHER_APPLE.get()));
                    spawnAt(entity, new ItemStack(ModRegistries.NETHER_STAR_APPLE.get()));
                }
            } else if (entity instanceof Evoker) {
                ItemStack reward = RNG.nextDouble() < 0.5
                    ? new ItemStack(Items.TOTEM_OF_UNDYING)
                    : new ItemStack(ModRegistries.TOTEM_APPLE.get());
                spawnAt(entity, reward);
            }
        });
    }

    private static void spawnAt(net.minecraft.world.entity.LivingEntity entity, ItemStack stack) {
        entity.level().addFreshEntity(new ItemEntity(
            entity.level(), entity.getX(), entity.getY(), entity.getZ(), stack));
    }
}
```

Note: On Fabric, `AFTER_DEATH` fires after all vanilla loot is already spawned. The Evoker vanilla totem drop comes from loot tables — it is removed by loot-table suppression via `FabricLootTableHandler` (see Step 5). The new drop is spawned here instead.

- [ ] **Step 3: Create FabricPlayerEffectHandler**

```java
package de.maxi.ultimate_apple_mod.fabric.event;

import de.maxi.ultimate_apple_mod.ModRegistries;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.WeakHashMap;

public class FabricPlayerEffectHandler {

    private static final WeakHashMap<Player, Boolean> serverRottenState = new WeakHashMap<>();

    public static void register() {

        // ── Totem Apple: cancel death ─────────────────────────────────────────
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (!(entity instanceof ServerPlayer player)) return true;
            if (!(player.level() instanceof ServerLevel)) return true;
            try {
                if (!player.hasEffect(ModRegistries.TOTEM_PROTECTION.get())) return true;
            } catch (NullPointerException ignored) { return true; }

            player.removeEffect(ModRegistries.TOTEM_PROTECTION.get());
            player.setHealth(1.0f);
            player.removeAllEffects();
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 45, 1));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 40, 0));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20 * 15, 3));
            player.level().broadcastEntityEvent(player, (byte) 35);
            player.displayClientMessage(
                Component.translatable("message.ultimate_apple_mod.totem_apple_triggered"), true);
            return false; // cancel death
        });

        // ── Lifesteal: heal on nearby mob death ───────────────────────────────
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity.level() instanceof ServerLevel serverLevel)) return;
            if (entity instanceof Player) return;

            LivingEntity dying = entity;
            ServerPlayer recipient = null;
            Entity attacker = damageSource.getEntity();
            if (attacker instanceof ServerPlayer sp
                    && sp.hasEffect(ModRegistries.LIFESTEAL.get())) {
                recipient = sp;
            }
            if (recipient == null) {
                double best = Double.MAX_VALUE;
                for (ServerPlayer sp : serverLevel.players()) {
                    double dist = sp.distanceToSqr(dying);
                    if (dist <= 32.0 * 32.0 && dist < best
                            && sp.hasEffect(ModRegistries.LIFESTEAL.get())) {
                        recipient = sp; best = dist;
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
        });

        // ── CurseOfRotten: refreshDimensions + pose fix (server-side) ─────────
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                for (ServerPlayer player : level.players()) {
                    boolean hasEffect;
                    try { hasEffect = player.hasEffect(ModRegistries.CURSE_OF_ROTTEN.get()); }
                    catch (NullPointerException ignored) { continue; }

                    Boolean prev = serverRottenState.get(player);
                    if (prev == null || prev != hasEffect) {
                        serverRottenState.put(player, hasEffect);
                        player.refreshDimensions();
                    }
                    // Fix swimming pose on land
                    if (hasEffect && player.getPose() == Pose.SWIMMING && !player.isInWater()) {
                        player.setPose(Pose.STANDING);
                    }
                }
            }
        });
    }
}
```

- [ ] **Step 4: Create FabricDecayHandler**

```java
package de.maxi.ultimate_apple_mod.fabric.event;

import de.maxi.ultimate_apple_mod.ModRegistries;
import de.maxi.ultimate_apple_mod.event.DecayEventHandler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FabricDecayHandler {

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                long now = level.getGameTime();
                if (now % 20 != 0) continue; // once per second

                for (Player player : level.players()) {
                    Inventory inv = player.getInventory();
                    for (int i = 0; i < inv.getContainerSize(); i++) {
                        ItemStack stack = inv.getItem(i);
                        if (stack.isEmpty()) continue;

                        long threshold = DecayEventHandler.getDecayThreshold(stack.getItem());
                        if (threshold == 0) continue;

                        CompoundTag tag = stack.getOrCreateTag();
                        if (!tag.contains(DecayEventHandler.DECAY_TAG)) {
                            tag.putLong(DecayEventHandler.DECAY_TAG, now);
                            continue;
                        }
                        long elapsed = now - tag.getLong(DecayEventHandler.DECAY_TAG);
                        if (elapsed < threshold) continue;

                        // Decay: apple → rotten apple
                        int count = stack.getCount();
                        inv.setItem(i, new ItemStack(ModRegistries.ROTTEN_APPLE.get(), count));
                    }
                }
            }
        });
    }
}
```

- [ ] **Step 5: Create FabricLootTableHandler**

```java
package de.maxi.ultimate_apple_mod.fabric.event;

import de.maxi.ultimate_apple_mod.ModRegistries;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
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
            // Evoker: vanilla totem-of-undying drop is handled by removing it here and
            // spawning a coin-flip replacement in FabricMobDropHandler.AFTER_DEATH.
            else if (id.equals(rl("entities/evoker"))) {
                tableBuilder.modifyPools(poolBuilder -> poolBuilder
                    .when(net.minecraft.world.level.storage.loot.predicates.AlternativeLootItemCondition.alternative(
                        // Always-false condition: removes all entries from the vanilla pool
                        // that drop Totem of Undying. Fabric API doesn't have a direct
                        // "remove entry" API, so we suppress the entire pool by adding a
                        // never-true condition. The actual drop is added in FabricMobDropHandler.
                        net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition.killedByPlayer(),
                        net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition.killedByPlayer()
                    )));
                // Note: The above suppresses vanilla evoker totem. FabricMobDropHandler handles the actual drop.
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
```

- [ ] **Step 6: Create FabricTntAppleHandler**

```java
package de.maxi.ultimate_apple_mod.fabric.event;

import de.maxi.ultimate_apple_mod.item.TntAppleEntity;
import de.maxi.ultimate_apple_mod.item.TntAppleItem;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ghast;

public class FabricTntAppleHandler {

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof Ghast)) return;
            Entity directEntity = damageSource.getDirectEntity();
            if (!(directEntity instanceof TntAppleEntity tntApple)) return;
            Entity owner = tntApple.getOwner();
            if (!(owner instanceof ServerPlayer player)) return;
            TntAppleItem.grantAdvancement(player, "tnt_apple_ghast");
        });
    }
}
```

- [ ] **Step 7: Create FabricBabyZombieHandler**

```java
package de.maxi.ultimate_apple_mod.fabric.event;

import de.maxi.ultimate_apple_mod.ModRegistries;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;

public class FabricBabyZombieHandler {

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof Zombie zombie) || !zombie.isBaby()) return;
            if (!entity.level().isClientSide && Math.random() < 0.1) {
                entity.level().addFreshEntity(new ItemEntity(
                    entity.level(), entity.getX(), entity.getY(), entity.getZ(),
                    new ItemStack(ModRegistries.ROTTEN_APPLE.get())));
            }
        });
    }
}
```

- [ ] **Step 8: Create LivingEntitySizeMixin in common**

```java
// common/src/main/java/de/maxi/ultimate_apple_mod/mixin/LivingEntitySizeMixin.java
package de.maxi.ultimate_apple_mod.mixin;

import de.maxi.ultimate_apple_mod.ModRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntitySizeMixin {

    @Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
    private void uam_shrinkWhenCursed(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if (!((Object) this instanceof Player player)) return;
        try {
            if (ModRegistries.CURSE_OF_ROTTEN == null) return;
            if (player.hasEffect(ModRegistries.CURSE_OF_ROTTEN.get())) {
                cir.setReturnValue(EntityDimensions.scalable(0.25f, 0.6f));
            }
        } catch (NullPointerException ignored) {}
    }
}
```

- [ ] **Step 9: Register LivingEntitySizeMixin in common mixin config**

Edit `common/src/main/resources/ultimate_apple_mod.mixins.json`:

```json
{
  "required": true,
  "package": "de.maxi.ultimate_apple_mod.mixin",
  "compatibilityLevel": "JAVA_17",
  "minVersion": "0.8",
  "client": [
  ],
  "mixins": [
    "LivingEntitySizeMixin"
  ],
  "injectors": {
    "defaultRequire": 1
  }
}
```

- [ ] **Step 10: Verify Fabric compiles (remaining failure only on FabricModClient)**

Run: `./gradlew :fabric:compileJava`
Expected: Fails ONLY because `FabricModClient` doesn't exist yet. All event handlers and the registrar compile cleanly.

- [ ] **Step 11: Commit**

```
git add fabric/src/main/java/de/maxi/ultimate_apple_mod/fabric/event/
git add common/src/main/java/de/maxi/ultimate_apple_mod/mixin/LivingEntitySizeMixin.java
git add common/src/main/resources/ultimate_apple_mod.mixins.json
git commit -m "feat(fabric): create all Fabric event handlers + common LivingEntitySizeMixin"
```

---

## Task 11: Create networking — common payload + Fabric handler

**Files:**
- Create: `common/src/main/java/de/maxi/ultimate_apple_mod/network/FireDragonBreathPayload.java`
- Create: `fabric/src/main/java/de/maxi/ultimate_apple_mod/fabric/network/FabricNetworkHandler.java`

- [ ] **Step 1: Create FireDragonBreathPayload in common**

```java
package de.maxi.ultimate_apple_mod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * C2S packet: sent by the client when the player presses the Fire Dragon Breath keybind.
 * Contains no data — the server reads charges from player persistent data.
 */
public record FireDragonBreathPayload() implements CustomPacketPayload {

    public static final Type<FireDragonBreathPayload> TYPE =
        new Type<>(new ResourceLocation("ultimate_apple_mod", "fire_dragon_breath"));

    public static final StreamCodec<ByteBuf, FireDragonBreathPayload> CODEC =
        StreamCodec.unit(new FireDragonBreathPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
```

- [ ] **Step 2: Create FabricNetworkHandler**

```java
package de.maxi.ultimate_apple_mod.fabric.network;

import de.maxi.ultimate_apple_mod.network.FireDragonBreathPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.phys.Vec3;

public class FabricNetworkHandler {

    private static final String CHARGES_KEY = "dragonBreathCharges";

    public static void registerServer() {
        PayloadTypeRegistry.playC2S().register(
            FireDragonBreathPayload.TYPE,
            FireDragonBreathPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(
            FireDragonBreathPayload.TYPE,
            (payload, ctx) -> ctx.server().execute(() -> {
                ServerPlayer player = ctx.player();
                int charges = player.getPersistentData().getInt(CHARGES_KEY);
                if (charges <= 0) return;

                Vec3 look = player.getLookAngle();
                DragonFireball fireball = new DragonFireball(
                    player.level(), player, look.x, look.y, look.z);
                fireball.setPos(
                    player.getX() + look.x * 1.5,
                    player.getEyeY() - 0.1,
                    player.getZ() + look.z * 1.5);
                player.level().addFreshEntity(fireball);

                int remaining = charges - 1;
                player.getPersistentData().putInt(CHARGES_KEY, remaining);
                player.displayClientMessage(
                    Component.translatable(
                        "message.ultimate_apple_mod.dragon_breath_remaining", remaining), true);
            }));
    }
}
```

- [ ] **Step 3: Call FabricNetworkHandler.registerServer() from ultimate_apple_modFabric.onInitialize()**

In `ultimate_apple_modFabric.java`, add at the END of `onInitialize()`:

```java
// After FabricEventRegistrar.register():
FabricNetworkHandler.registerServer();
```

Add import: `import de.maxi.ultimate_apple_mod.fabric.network.FabricNetworkHandler;`

- [ ] **Step 4: Commit**

```
git add common/src/main/java/de/maxi/ultimate_apple_mod/network/FireDragonBreathPayload.java
git add fabric/src/main/java/de/maxi/ultimate_apple_mod/fabric/network/FabricNetworkHandler.java
git add fabric/src/main/java/de/maxi/ultimate_apple_mod/fabric/ultimate_apple_modFabric.java
git commit -m "feat: add FireDragonBreathPayload to common + Fabric network handler"
```

---

## Task 12: Create FabricModClient + client handler + render mixin

**Files:**
- Create: `fabric/src/main/java/de/maxi/ultimate_apple_mod/fabric/FabricModClient.java`
- Create: `fabric/src/main/java/de/maxi/ultimate_apple_mod/fabric/event/FabricClientHandler.java`
- Create: `fabric/src/main/java/de/maxi/ultimate_apple_mod/fabric/mixin/PlayerRendererMixin.java`
- Create: `fabric/src/main/resources/ultimate_apple_mod.fabric.mixins.json`

- [ ] **Step 1: Create FabricClientHandler**

```java
package de.maxi.ultimate_apple_mod.fabric.event;

import de.maxi.ultimate_apple_mod.ModRegistries;
import de.maxi.ultimate_apple_mod.RewindPositionCache;
import de.maxi.ultimate_apple_mod.event.DecayEventHandler;
import de.maxi.ultimate_apple_mod.fabric.FabricModClient;
import de.maxi.ultimate_apple_mod.fabric.network.FabricClientNetworking;
import de.maxi.ultimate_apple_mod.network.FireDragonBreathPayload;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class FabricClientHandler {

    private static boolean wasRottenActive = false;

    public static void register() {

        // ── Tooltip event ─────────────────────────────────────────────────────
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            // 1. Decay countdown for vanilla apples
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains(DecayEventHandler.DECAY_TAG)) {
                long threshold = DecayEventHandler.getDecayThreshold(stack.getItem());
                if (threshold > 0) {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.level != null) {
                        long remaining = threshold - (mc.level.getGameTime() - tag.getLong(DecayEventHandler.DECAY_TAG));
                        if (remaining <= 0) {
                            lines.add(Component.literal("§cRots any moment now!"));
                        } else {
                            long s = remaining / 20;
                            String color = remaining > 20L*60*15 ? "§a"
                                : remaining > 20L*60*5 ? "§e"
                                : remaining > 20L*60 ? "§6" : "§c";
                            lines.add(Component.literal(String.format("%sRots in: %d:%02d", color, s/60, s%60)));
                        }
                    }
                }
            }

            // 2. Shift tooltip for mod items
            Item item = stack.getItem();
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id == null || !id.getNamespace().equals(ultimate_apple_mod.MOD_ID)) return;
            String path = id.getPath();
            if (path.equals("shake") || path.equals("cup")) return;

            if (!Screen.hasShiftDown()) {
                while (lines.size() > 1) lines.remove(1);
                lines.add(Component.literal("§7Hold §eShift §7for more info").withStyle(ChatFormatting.DARK_GRAY));
            } else {
                if (path.equals("mixer")) {
                    lines.add(Component.literal("Combine two apple items to brew a Shake.").withStyle(ChatFormatting.GRAY));
                    lines.add(Component.literal("Effects from both ingredients are merged.").withStyle(ChatFormatting.GRAY));
                    lines.add(Component.literal("Requires a Cup in the bottom slot.").withStyle(ChatFormatting.DARK_GRAY));
                    lines.add(Component.literal("⊕ All effect durations receive a +20% bonus.").withStyle(ChatFormatting.DARK_GREEN));
                    lines.add(Component.literal("⊕ Add a Longevity Apple to double all durations.").withStyle(ChatFormatting.DARK_GREEN));
                    return;
                }
                FoodProperties food = item.getFoodProperties(stack, null);
                if (food != null && !food.getEffects().isEmpty()) {
                    lines.add(Component.literal("Effects:").withStyle(ChatFormatting.GOLD));
                    for (var pair : food.getEffects()) lines.add(formatEffect(pair.getFirst()));
                }
            }
        });

        // ── Client tick: keybind polling + CurseOfRotten pose fix + rewind track ─
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            Player player = client.player;

            // Dragon breath keybind
            while (FabricModClient.FIRE_DRAGON_BREATH_KEY.consumeClick()) {
                boolean aimingAtEntity = client.hitResult instanceof net.minecraft.world.phys.EntityHitResult;
                var mainHand = player.getMainHandItem();
                boolean holdingMelee = mainHand.getItem() instanceof net.minecraft.world.item.SwordItem
                    || mainHand.getItem() instanceof net.minecraft.world.item.AxeItem;
                if (aimingAtEntity && holdingMelee) continue;
                net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                    new FireDragonBreathPayload());
            }

            // CurseOfRotten client-side dimension refresh + pose fix
            boolean isRottenActive = false;
            try { isRottenActive = player.hasEffect(ModRegistries.CURSE_OF_ROTTEN.get()); }
            catch (NullPointerException ignored) {}
            if (isRottenActive != wasRottenActive) {
                player.refreshDimensions();
                wasRottenActive = isRottenActive;
            }
            if (isRottenActive && player.getPose() == Pose.SWIMMING && !player.isInWater()) {
                player.setPose(Pose.STANDING);
            }

            // Rewind position tracking (client only records for local player — server handles all players)
            // Server-side is handled in FabricPlayerEffectHandler via ServerTickEvents.END_SERVER_TICK
        });
    }

    private static Component formatEffect(MobEffectInstance eff) {
        int amp = eff.getAmplifier();
        int dur = eff.getDuration();
        MutableComponent line = Component.literal("  ")
            .append(eff.getEffect().getDisplayName().copy().withStyle(ChatFormatting.GRAY));
        if (amp > 0) line.append(Component.literal(" " + toRoman(amp + 1)).withStyle(ChatFormatting.GRAY));
        line.append(Component.literal(" (" + formatDuration(dur) + ")").withStyle(ChatFormatting.DARK_GRAY));
        return line;
    }

    private static String toRoman(int n) {
        return switch (n) { case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; case 5 -> "V";
            case 6 -> "VI"; case 7 -> "VII"; case 8 -> "VIII"; case 9 -> "IX"; case 10 -> "X";
            default -> String.valueOf(n); };
    }

    private static String formatDuration(int ticks) {
        int s = ticks / 20;
        if (s >= 60) { int m = s/60; int r = s%60; return r == 0 ? m+"m" : m+"m "+r+"s"; }
        return s + "s";
    }
}
```

- [ ] **Step 2: Create FabricModClient**

```java
package de.maxi.ultimate_apple_mod.fabric;

import de.maxi.ultimate_apple_mod.ModRegistries;
import de.maxi.ultimate_apple_mod.block.MixerScreen;
import de.maxi.ultimate_apple_mod.fabric.event.FabricClientHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import org.lwjgl.glfw.GLFW;

public class FabricModClient implements ClientModInitializer {

    public static KeyMapping FIRE_DRAGON_BREATH_KEY;

    @Override
    public void onInitializeClient() {

        // ── Keybinding ────────────────────────────────────────────────────────
        FIRE_DRAGON_BREATH_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.ultimate_apple_mod.fire_dragon_breath",
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_LEFT,
            "key.categories.ultimate_apple_mod"));

        // ── Entity renderers ──────────────────────────────────────────────────
        EntityRendererRegistry.register(ModRegistries.APPLE_BOMB_ENTITY.get(),    ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModRegistries.SHAKE_BOMB_ENTITY.get(),    ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModRegistries.TNT_APPLE_ENTITY.get(),     ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModRegistries.NUCLEAR_APPLE_ENTITY.get(), ThrownItemRenderer::new);

        // ── Screen registration ───────────────────────────────────────────────
        MenuScreens.register(
            (net.minecraft.world.inventory.MenuType<de.maxi.ultimate_apple_mod.block.MixerMenu>)
                ModRegistries.MIXER_MENU_TYPE.get(),
            MixerScreen::new);

        // ── Client events ─────────────────────────────────────────────────────
        FabricClientHandler.register();
    }
}
```

- [ ] **Step 3: Create PlayerRendererMixin (Fabric-only, for visual render scale)**

```java
// fabric/src/main/java/de/maxi/ultimate_apple_mod/fabric/mixin/PlayerRendererMixin.java
package de.maxi.ultimate_apple_mod.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxi.ultimate_apple_mod.ModRegistries;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

    @Inject(method = "renderRightHand", at = @At("HEAD"))
    private void uam_scaleWhenCursed(PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource buffer,
                                      int packedLight, AbstractClientPlayer player, CallbackInfo ci) {
        // Intentionally empty — scale is applied in the render method below
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/player/Player;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"))
    private void uam_scalePlayer(net.minecraft.world.entity.player.Player player, float entityYaw,
                                   float partialTick, PoseStack poseStack,
                                   net.minecraft.client.renderer.MultiBufferSource buffer,
                                   int packedLight, CallbackInfo ci) {
        try {
            if (ModRegistries.CURSE_OF_ROTTEN != null
                    && player.hasEffect(ModRegistries.CURSE_OF_ROTTEN.get())) {
                poseStack.scale(0.35f, 0.35f, 0.35f);
            }
        } catch (NullPointerException ignored) {}
    }
}
```

- [ ] **Step 4: Create fabric-only mixin config**

Create `fabric/src/main/resources/ultimate_apple_mod.fabric.mixins.json`:

```json
{
  "required": true,
  "package": "de.maxi.ultimate_apple_mod.fabric.mixin",
  "compatibilityLevel": "JAVA_17",
  "minVersion": "0.8",
  "client": [
    "PlayerRendererMixin"
  ],
  "mixins": [
  ],
  "injectors": {
    "defaultRequire": 1
  }
}
```

- [ ] **Step 5: Commit**

```
git add fabric/src/main/java/de/maxi/ultimate_apple_mod/fabric/FabricModClient.java
git add fabric/src/main/java/de/maxi/ultimate_apple_mod/fabric/event/FabricClientHandler.java
git add fabric/src/main/java/de/maxi/ultimate_apple_mod/fabric/mixin/PlayerRendererMixin.java
git add fabric/src/main/resources/ultimate_apple_mod.fabric.mixins.json
git commit -m "feat(fabric): add FabricModClient, client event handler, player render scale mixin"
```

---

## Task 13: Update fabric.mod.json + verify both builds

**Files:**
- Modify: `fabric/src/main/resources/fabric.mod.json`

- [ ] **Step 1: Update fabric.mod.json entrypoints and add fabric mixin config**

Replace the content of `fabric/src/main/resources/fabric.mod.json`:

```json
{
  "schemaVersion": 1,
  "id": "ultimate_apple_mod",
  "version": "${version}",
  "name": "Ultimate Apple Mod",
  "description": "",
  "authors": ["Maxi"],
  "contact": {
    "homepage": ""
  },
  "license": "ISC",
  "icon": "assets/ultimate_apple_mod/icon.png",
  "environment": "*",
  "entrypoints": {
    "main": [
      "de.maxi.ultimate_apple_mod.fabric.ultimate_apple_modFabric"
    ],
    "client": [
      "de.maxi.ultimate_apple_mod.fabric.FabricModClient"
    ]
  },
  "mixins": [
    "ultimate_apple_mod.mixins.json",
    "ultimate_apple_mod.fabric.mixins.json"
  ],
  "depends": {
    "fabricloader": ">=0.16.13",
    "minecraft": "~1.20.1",
    "java": ">=17",
    "architectury": ">=9.2.14",
    "fabric-api": "*"
  }
}
```

- [ ] **Step 2: Build Forge**

Run: `./gradlew :forge:build`
Expected: BUILD SUCCESSFUL — `forge/build/libs/` contains the Forge jar.

Fix any compilation errors before proceeding.

- [ ] **Step 3: Build Fabric**

Run: `./gradlew :fabric:build`
Expected: BUILD SUCCESSFUL — `fabric/build/libs/` contains the Fabric jar.

Common fixes if build fails:
- `CustomPacketPayload` not found: verify `fabric-networking-api-v1` is in `fabric/build.gradle` dependencies
- `FabricItemGroup` not found: verify `fabric-item-group-api-v1` is present
- `WorldlyContainer` not found: it's `net.minecraft.world.WorldlyContainer` (vanilla interface)
- `LootTableEvents` not found: verify `fabric-loot-api-v2` is present in Fabric API
- Missing `MenuType` registry: use `BuiltInRegistries.MENU` (vanilla, available without Forge)

- [ ] **Step 4: Final commit**

```
git add fabric/src/main/resources/fabric.mod.json
git commit -m "feat(fabric): update fabric.mod.json with correct entrypoints and mixin configs"
```

- [ ] **Step 5: Smoke-test checklist (manual, in game)**

**Forge:**
- [ ] Mod loads without errors in Forge dev run
- [ ] Creative tab shows all items
- [ ] Copper Apple oxidizes in inventory over time
- [ ] Mixer Block can be placed and used
- [ ] Dragon Apple fires fireball on left-click
- [ ] Vanilla apple decays to rotten apple after 30 min

**Fabric:**
- [ ] Mod loads without errors in Fabric dev run
- [ ] Creative tab shows all items (same as Forge)
- [ ] Mixer Block can be placed and used
- [ ] Dragon Apple fires fireball on left-click (keybind works)
- [ ] Entity renderers: apple_bomb, shake_bomb, tnt_apple, nuclear_apple render as thrown items
- [ ] Mob drops: Wither drops Wither Apple 50% of the time
- [ ] Loot table: Blazes can drop Blaze Apple
