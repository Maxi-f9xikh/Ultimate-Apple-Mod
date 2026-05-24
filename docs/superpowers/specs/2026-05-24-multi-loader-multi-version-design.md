# Multi-Loader / Multi-Version Expansion — Design

## Goal

Expand the Ultimate Apple Mod from its current single-project (Forge + Fabric, 1.20.1) to support NeoForge and Quilt on 1.20.1, then port to 1.21.1 (Fabric + NeoForge + Quilt) and 1.20.4 (all four loaders).

## Repository Layout (approved)

```
Ultimate Apple Mod/          ← 1.20.1 root (unchanged structure)
├── settings.gradle          ← add neoforge + quilt
├── gradle.properties        ← add neoforge/quilt version props
├── common/
├── fabric/
├── forge/
├── neoforge/                ← Phase 1 (new)
├── quilt/                   ← Phase 1 (new)
├── 1.21.1/                  ← Phase 2 (new independent Gradle project)
│   ├── settings.gradle
│   ├── build.gradle
│   ├── gradle.properties
│   ├── common/
│   ├── fabric/
│   ├── neoforge/
│   └── quilt/
└── 1.20.4/                  ← Phase 3 (new independent Gradle project)
    ├── settings.gradle
    ├── build.gradle
    ├── gradle.properties
    ├── common/
    ├── fabric/
    ├── forge/
    ├── neoforge/
    └── quilt/
```

---

## Phase 1 — NeoForge + Quilt for 1.20.1

### Approach

**NeoForge**: Clone of `forge/` subproject with two differences:
1. Gradle dependency: `net.neoforged:neoforge:1.20.1-47.1.x`
2. Java imports: all `net.minecraftforge.*` → `net.neoforged.*` equivalents

**Quilt**: Thin wrapper (~5 files) over the existing `fabric/` subproject. The Quilt initializer literally delegates to `ultimate_apple_modFabric.onInitialize()`.

### Files changed

**Root project** (2 files modified):
- `settings.gradle` — add `include 'neoforge'` and `include 'quilt'`; add NeoForge + Quilt maven repos in `pluginManagement`
- `gradle.properties` — add `neoforge_version = 1.20.1-47.1.104`, `quilt_loader_version = 0.26.4`, `quilted_fabric_api_version = 7.6.0+0.92.2-1.20.1`
  > Version numbers above are latest-known as of spec writing. Verify at https://projects.neoforged.net/neoforged/neoforge and https://modrinth.com/mod/quilt-standard-libraries before building.

**NeoForge subproject** (20 new files):
- `neoforge/build.gradle`
- `neoforge/src/main/java/.../neoforge/ultimate_apple_modNeoForge.java`
- `neoforge/src/main/java/.../neoforge/ModClient.java`
- `neoforge/src/main/java/.../neoforge/ModRecipes.java`
- `neoforge/src/main/java/.../neoforge/block/MixerBlockEntity.java`
- `neoforge/src/main/java/.../neoforge/block/ModBlocks.java`
- `neoforge/src/main/java/.../neoforge/event/ClientEventHandler.java`
- `neoforge/src/main/java/.../neoforge/event/ClientPlayerRenderHandler.java`
- `neoforge/src/main/java/.../neoforge/event/DecayEventHandler.java`
- `neoforge/src/main/java/.../neoforge/event/KeyInputHandler.java`
- `neoforge/src/main/java/.../neoforge/event/LootTableHandler.java`
- `neoforge/src/main/java/.../neoforge/event/MobDropEventHandler.java`
- `neoforge/src/main/java/.../neoforge/event/PlayerEffectEventHandler.java`
- `neoforge/src/main/java/.../neoforge/event/RewindTracker.java`
- `neoforge/src/main/java/.../neoforge/event/TntAppleEventHandler.java`
- `neoforge/src/main/java/.../neoforge/event/babyzombiedroppt.java`
- `neoforge/src/main/java/.../neoforge/network/NetworkHandler.java`
- `neoforge/src/main/java/.../neoforge/network/FireDragonBreathPacket.java`
- `neoforge/src/main/resources/META-INF/mods.toml`
- `neoforge/src/main/resources/META-INF/logo.png` (copy from `forge/src/main/resources/META-INF/logo.png`)

### NeoForge 1.20.1 package mapping

| Forge (old) | NeoForge (new) |
|---|---|
| `net.minecraftforge.fml.common.Mod` | `net.neoforged.fml.common.Mod` |
| `net.minecraftforge.eventbus.api.*` | `net.neoforged.bus.api.*` |
| `net.minecraftforge.fml.event.lifecycle.*` | `net.neoforged.fml.event.lifecycle.*` |
| `net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext` | `net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext` |
| `net.minecraftforge.registries.DeferredRegister` | `net.neoforged.neoforge.registries.DeferredRegister` |
| `net.minecraftforge.registries.ForgeRegistries` | `net.neoforged.neoforge.registries.ForgeRegistries` |
| `net.minecraftforge.registries.RegistryObject` | `net.neoforged.neoforge.registries.RegistryObject` |
| `net.minecraftforge.common.extensions.IForgeMenuType` | `net.neoforged.neoforge.common.extensions.IMenuTypeExtension` |
| `net.minecraftforge.network.NetworkRegistry` | `net.neoforged.neoforge.network.NetworkRegistry` |
| `net.minecraftforge.network.simple.SimpleChannel` | `net.neoforged.neoforge.network.simple.SimpleChannel` |
| `net.minecraftforge.event.*` | `net.neoforged.neoforge.event.*` |
| `net.minecraftforge.client.event.*` | `net.neoforged.neoforge.client.event.*` |
| `net.minecraftforge.api.distmarker.Dist` | `net.neoforged.api.distmarker.Dist` |
| `Mod.EventBusSubscriber.Bus.FORGE` | `Mod.EventBusSubscriber.Bus.NEOFORGE` |

> Note: NeoForge 1.20.1 (47.1.x) retained `ForgeRegistries` under the `net.neoforged.neoforge.*` namespace. If any class name differs at compile time, use `./gradlew :neoforge:dependencies` to inspect what's available and check the [NeoForge 1.20.1 sources](https://github.com/neoforged/NeoForge/tree/1.20.x).

> Additional note: All event handlers in `neoforge/event/` mirror those in `forge/event/` with the same package renames. Files `ClientPlayerRenderHandler.java`, `RewindTracker.java`, `LootTableHandler.java`, `MobDropEventHandler.java`, `PlayerEffectEventHandler.java`, `KeyInputHandler.java`, and `babyzombiedroppt.java` each reference Forge APIs that need the same `net.minecraftforge.*` → `net.neoforged.*` substitution as the others.

**Quilt subproject** (4 new files):
- `quilt/build.gradle`
- `quilt/src/main/java/.../quilt/ultimate_apple_modQuilt.java`
- `quilt/src/main/java/.../quilt/QuiltModClient.java`
- `quilt/src/main/resources/quilt.mod.json`

### Quilt initializer pattern

```java
// ultimate_apple_modQuilt.java
public class ultimate_apple_modQuilt implements ModInitializer {
    @Override
    public void onInitialize(ModContainer container) {
        new ultimate_apple_modFabric().onInitialize();
    }
}
```

```json
// quilt.mod.json
{
  "schema_version": 1,
  "quilt_loader": {
    "group": "de.maxi",
    "id": "ultimate_apple_mod",
    "version": "${version}",
    "entrypoints": {
      "init": ["de.maxi.ultimate_apple_mod.quilt.ultimate_apple_modQuilt"],
      "client_init": ["de.maxi.ultimate_apple_mod.quilt.QuiltModClient"]
    },
    "depends": [
      { "id": "quilt_loader", "versions": ">=0.22" },
      { "id": "quilted_fabric_api", "versions": "*" }
    ]
  }
}
```

---

## Phase 2 — 1.21.1 (Fabric + NeoForge + Quilt, no Forge)

### Dependency versions for 1.21.1

| Dependency | Version |
|---|---|
| `minecraft_version` | `1.21.1` |
| `architectury_api_version` | `13.0.6` (Architectury API for 1.21.1 — verify latest) |
| `fabric_loader_version` | `0.16.10` |
| `fabric_api_version` | `0.112.2+1.21.1` |
| `neoforge_version` | `21.1.172` (NeoForge 1.21.1 — verify latest) |
| `quilt_loader_version` | same or newer |
| Java | 21 |

### Key 1.21.1 API migrations

#### 1. Data Components (largest change)

In 1.21.1, `ItemStack.getTag()` / `getOrCreateTag()` no longer exist. Item-specific data uses typed `DataComponentType<T>`.

**ShakeItem** currently stores on the stack NBT:
- `isBomb: Boolean`
- `effects: ListTag`
- `isCoalFuel: Boolean`

Migration pattern:
1. Define a record: `record ShakeData(List<EffectEntry> effects, boolean isBomb, boolean isCoalFuel)`
2. Register `DataComponentType<ShakeData>` in `common/` via Architectury's registry system
3. Replace every `stack.getOrCreateTag().putX(...)` with `stack.set(SHAKE_DATA, ...)`
4. Replace every `stack.getTag().getX(...)` with `stack.getOrDefault(SHAKE_DATA, ShakeData.EMPTY).field()`

**Other affected files** (tooltip NBT reads in ClientEventHandler):
- `DecayEventHandler`: uses `stack.getOrCreateTag()` to store decay timestamp → migrate to `DataComponentType<Long>`
- `CopperAppleItem.appendHoverText`: if it reads NBT, migrate similarly
- `LapislazuliAppleItem`: check for NBT usage

#### 2. FoodProperties API (minor)

In 1.21.1, `FoodProperties.Builder.effect(Supplier<MobEffectInstance>, float)` is gone. Use `.effect(MobEffectInstance, float)` directly (like Fabric 1.20.1 already does).

This means Forge-style `() ->` lambdas in food registration are removed. The common module registration pattern (if food is registered there) needs updating.

#### 3. NeoForge 1.21.1

NeoForge 1.21.1 is architecturally different from NeoForge 1.20.1:
- Registration: still `DeferredRegister` but event bus wiring changed
- Network: new `IPayloadRegistrar` / `CustomPacketPayload` API (replaces `SimpleChannel`)
- `@Mod.EventBusSubscriber` still works
- No `FMLJavaModLoadingContext.get()` — event bus injected into `@Mod` constructor

The `1.21.1/neoforge/` network handler needs to be rewritten using `NetworkPayloadSetup`:
```java
// Replace SimpleChannel with:
event.register(FireDragonBreathPayload.TYPE, FireDragonBreathPayload::new,
    NetworkDirection.PLAY_TO_SERVER);
```

#### 4. Mixin config path

1.21.1 NeoForge moved mixin config from `META-INF/mods.toml` to `META-INF/neoforge.mods.toml`.

### File structure for 1.21.1/common

Copy `common/src/main/java/` then apply migrations:
- `item/ShakeItem.java` — Data Component migration
- `block/MixerBlockEntityBase.java` — reads ShakeItem data
- Any item with `getTag()` / `getOrCreateTag()` calls

---

## Phase 3 — 1.20.4 (Forge + Fabric + NeoForge + Quilt)

### Why 1.20.4 is the simplest

- No Data Components (Mojang added those in 1.20.5/1.21)
- No NeoForge network overhaul (1.21.1 only)
- Java 17 still
- Essentially the same API as 1.20.1 with minor version bumps

### Dependency versions for 1.20.4

| Dependency | Version |
|---|---|
| `minecraft_version` | `1.20.4` |
| `architectury_api_version` | `11.1.x` |
| `fabric_api_version` | `0.97.x+1.20.4` |
| `forge_version` | `1.20.4-49.x.x` |
| `neoforge_version` | `20.4.x` |

### Known 1.20.4 API differences vs 1.20.1

1. `MenuType` constructor: 3rd arg `FeatureFlags.VANILLA_SET` is required in Fabric registration (already present in current code)
2. `ResourceLocation` constructor: still `new ResourceLocation(ns, path)` in 1.20.4 (changes to `ResourceLocation.fromNamespaceAndPath` in 1.21)
3. `FoodProperties.Builder` API: same as 1.20.1
4. `ItemStack.getTag()` still works (no Data Components yet)

Strategy: copy the `1.20.1/` root structure (after Phase 1 is complete), bump all version numbers, run the build, fix any compile errors.

---

## Execution Order

1. **Phase 1** — NeoForge + Quilt added to 1.20.1 root
2. **Phase 2** — 1.21.1 independent project (biggest migration effort)
3. **Phase 3** — 1.20.4 independent project (largely copy + version bump)

Each phase is a standalone commit set. Phases do not depend on each other for compilation.
