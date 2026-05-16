# Multiplatform Restructure (Forge + Fabric) — Design Spec

## Goal

Restructure the Ultimate Apple Mod so that all platform-neutral code lives in `common/`,
Forge-specific code stays in `forge/`, and a full Fabric implementation is added in `fabric/`.
Both platforms must expose identical functionality: all apple items, effects, entities,
the Mixer block, keybindings, mob drops, loot-table injections, and networking.

## Approach

**Hybrid B — platform-separate implementations.**  
Common holds shared logic. Forge and Fabric each have their own registration,
event handlers, and BlockEntity subclass. No Architectury cross-platform event
abstractions are used; each platform uses its own native API.

---

## 1. Module Split

### `common/src/main/java/de/maxi/ultimate_apple_mod/`

| Package / File | Origin | Notes |
|---|---|---|
| `ultimate_apple_mod.java` | already here | expand with `MOD_ID` constant only |
| `ModRegistries.java` | **new** | platform-neutral `Supplier<>` fields |
| `item/*.java` (all 23 item classes) | moved from forge | `CopperAppleItem` gets `ForgeRegistries` → `BuiltInRegistries` |
| `effect/*.java` (all 5 effect classes) | moved from forge | no changes needed |
| `item/AppleBombEntity.java` | moved from forge | references → `ModRegistries` |
| `item/ShakeBombEntity.java` | moved from forge | references → `ModRegistries` |
| `item/TntAppleEntity.java` | moved from forge | references → `ModRegistries` |
| `item/NuclearAppleEntity.java` | moved from forge | references → `ModRegistries` |
| `block/MixerBlockEntityBase.java` | **new** (extracted) | full craft logic, `SimpleContainer` |
| `block/MixerBlock.java` | moved from forge | vanilla Block — no changes |
| `block/MixerMenu.java` | moved from forge | vanilla AbstractContainerMenu |
| `block/MixerScreen.java` | moved from forge | vanilla AbstractContainerScreen |
| `block/MixerRecipes.java` | moved from forge | custom logic — no changes |
| `network/FireDragonBreathPayload.java` | **new** | record holding packet data + TYPE + CODEC |

### `common/src/main/resources/`

All assets and data move from `forge/src/main/resources/` to here:

```
assets/ultimate_apple_mod/
  blockstates/, lang/, models/, textures/, mob_effect/
data/
  jeed/recipes/
  minecraft/tags/
  ultimate_apple_mod/
    advancements/, loot_modifiers/, loot_tables/, recipes/
META-INF/
  (mods.toml stays in forge; fabric.mod.json stays in fabric)
```

### `forge/src/main/java/de/maxi/ultimate_apple_mod/`

| File | Status | Notes |
|---|---|---|
| `forge/ultimate_apple_modForge.java` | keep, modify | DeferredRegister; fills `ModRegistries.*` on init |
| `forge/ModClient.java` | keep | Forge client events, keybinding |
| `forge/ModRecipes.java` | keep | Forge recipe serializer registration |
| `forge/network/NetworkHandler.java` | keep | Forge `SimpleChannel` |
| `forge/network/FireDragonBreathPacket.java` | keep | wraps `FireDragonBreathPayload` |
| `forge/block/MixerBlockEntity.java` | keep, modify | extends `MixerBlockEntityBase`; adds `IItemHandler` capability |
| `forge/block/ModBlocks.java` | keep | DeferredRegister for block + BE + MenuType |
| `forge/event/*.java` (all 13 handlers) | keep | unchanged `@SubscribeEvent` handlers |

`forge/src/main/resources/` keeps only:
- `META-INF/mods.toml`
- `META-INF/accesstransformer.cfg` (if present)

### `fabric/src/main/java/de/maxi/ultimate_apple_mod/`

| File | Status | Notes |
|---|---|---|
| `fabric/ultimate_apple_modFabric.java` | expand | Fabric `ModInitializer`; registers all items/blocks/entities; fills `ModRegistries.*` |
| `fabric/FabricModClient.java` | **new** | `ClientModInitializer`; registers keybinding + renderers + screen |
| `fabric/block/MixerBlockEntity.java` | **new** | extends `MixerBlockEntityBase`; implements `SidedInventory` |
| `fabric/block/ModBlocks.java` | **new** | `Registry.register(...)` for block + BE type + MenuType |
| `fabric/event/FabricEventRegistrar.java` | **new** | central entry point; calls `register()` on all handlers |
| `fabric/event/FabricMobDropHandler.java` | **new** | `ServerLivingEntityEvents.AFTER_DEATH` — Wither/Evoker drops |
| `fabric/event/FabricPlayerEffectHandler.java` | **new** | `ServerTickEvents.END_SERVER_TICK` — Lifesteal, Totem, etc. |
| `fabric/event/FabricLootTableHandler.java` | **new** | `LootTableEvents.MODIFY` — inject apple drops |
| `fabric/event/FabricDecayHandler.java` | **new** | `ServerTickEvents.END_SERVER_TICK` — rotten apple decay |
| `fabric/event/FabricTntAppleHandler.java` | **new** | `ServerLivingEntityEvents.AFTER_DEATH` — TNT apple explosion on entity kill |
| `fabric/event/FabricClientHandler.java` | **new** | `ClientTickEvents.END_CLIENT_TICK` — rewind render, keybind polling |
| `fabric/event/FabricBabyZombieHandler.java` | **new** | `ServerLivingEntityEvents.AFTER_DEATH` — baby zombie drop |
| `fabric/network/FabricNetworkHandler.java` | **new** | `ServerPlayNetworking` — receives FireDragonBreath C2S |

`fabric/src/main/resources/` keeps only:
- `fabric.mod.json`

### Fabric Entity Renderer Registration

Registered in `FabricModClient.java` inside `onInitializeClient()`:

```java
EntityRendererRegistry.register(ModRegistries.APPLE_BOMB_ENTITY.get(),    ThrownItemRenderer::new);
EntityRendererRegistry.register(ModRegistries.SHAKE_BOMB_ENTITY.get(),    ThrownItemRenderer::new);
EntityRendererRegistry.register(ModRegistries.TNT_APPLE_ENTITY.get(),     ThrownItemRenderer::new);
EntityRendererRegistry.register(ModRegistries.NUCLEAR_APPLE_ENTITY.get(), ThrownItemRenderer::new);
```

Fabric equivalent of Forge's `EntityRenderersEvent.RegisterRenderers` in `ModClient.java`.

---

## 2. Registry Pattern (`ModRegistries.java`)

The complete item list is taken 1-to-1 from `ultimate_apple_modForge.java` — every
`RegistryObject<Item>` becomes one `Supplier<Item>` field. All item/entity classes that
currently call `ultimate_apple_modForge.XYZ.get()` are updated to call `ModRegistries.XYZ.get()`.

```java
public class ModRegistries {
    // One field per registered item (exact list mirrors ultimate_apple_modForge.java)
    public static Supplier<Item> COAL_APPLE;
    public static Supplier<Item> IRON_APPLE;
    // ... all remaining items (copper variants, special apples, etc.)

    // Entity types
    public static Supplier<EntityType<AppleBombEntity>>    APPLE_BOMB_ENTITY;
    public static Supplier<EntityType<ShakeBombEntity>>    SHAKE_BOMB_ENTITY;
    public static Supplier<EntityType<TntAppleEntity>>     TNT_APPLE_ENTITY;
    public static Supplier<EntityType<NuclearAppleEntity>> NUCLEAR_APPLE_ENTITY;

    // Block + BE + Menu
    public static Supplier<Block>                          MIXER;
    public static Supplier<BlockEntityType<?>>             MIXER_BLOCK_ENTITY;
    public static Supplier<MenuType<MixerMenu>>            MIXER_MENU_TYPE;

    // Effects
    public static Supplier<MobEffect> LIFESTEAL;
    public static Supplier<MobEffect> TOTEM_PROTECTION;
    public static Supplier<MobEffect> MOON_GRAVITY;
    public static Supplier<MobEffect> TIME_FREEZE;
    public static Supplier<MobEffect> CURSE_OF_ROTTEN;
}
```

**Forge** fills fields during `FMLCommonSetupEvent` / static init from `RegistryObject<>` (which is already a `Supplier<>`).

**Fabric** fills fields in `onInitialize()` by wrapping registered instances in `() -> instance`.

Item/Entity classes in `common/` call `ModRegistries.NUCLEAR_APPLE_ENTITY.get()` instead of `ultimate_apple_modForge.NUCLEAR_APPLE_ENTITY.get()`.

---

## 3. Mixer Block

### `MixerBlockEntityBase` (common)

Contains:
- `SimpleContainer inventory` (all slots)
- Full crafting logic: `tick()` with recipe evaluation
- `load()` / `saveAdditional()` for NBT
- `clearContent()`, `getContainerSize()`, slot access helpers

Does NOT contain:
- Forge `LazyOptional` / `IItemHandler`
- Fabric `SidedInventory` interface

### Forge subclass

```java
// forge/block/MixerBlockEntity.java
public class MixerBlockEntity extends MixerBlockEntityBase {
    private final LazyOptional<IItemHandler> itemHandlerCapability;

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER)
            return itemHandlerCapability.cast();
        return super.getCapability(cap, side);
    }
    
    @Override
    public void invalidateCaps() { itemHandlerCapability.invalidate(); }
}
```

### Fabric subclass

```java
// fabric/block/MixerBlockEntity.java
public class MixerBlockEntity extends MixerBlockEntityBase implements SidedInventory {
    @Override public int[] getSlotsForFace(Direction side) { /* all slots */ }
    // Mixer accepts any item in any slot (no slot restrictions — intentional design)
    @Override public boolean canInsertItem(int slot, ItemStack stack, Direction dir) { return true; }
    @Override public boolean canExtractItem(int slot, ItemStack stack, Direction dir) { return true; }
    // All Inventory methods delegate to super.inventory (SimpleContainer)
}
```

`MixerBlock.java`, `MixerMenu.java`, `MixerScreen.java`, `MixerRecipes.java` are in `common/` and used by both platforms unchanged.

---

## 4. Events

### Forge
All 13 existing event handlers remain unchanged in `forge/event/`.

### Fabric — mapping table

| Forge handler | Fabric implementation |
|---|---|
| `MobDropEventHandler` (Wither, Evoker) | `FabricMobDropHandler` via `ServerLivingEntityEvents.AFTER_DEATH` |
| `PlayerEffectEventHandler` (Lifesteal, Totem, Moon Gravity, Time Freeze) | `FabricPlayerEffectHandler` via `ServerTickEvents.END_SERVER_TICK` |
| `LootTableHandler` (apple loot injections) | `FabricLootTableHandler` via `LootTableEvents.MODIFY` |
| `DecayEventHandler` (rotten apple) | `FabricDecayHandler` via `ServerTickEvents.END_SERVER_TICK` |
| `TntAppleEventHandler` | `FabricTntAppleHandler` via `ServerLivingEntityEvents.AFTER_DEATH` (mirrors Forge: triggers TNT explosion when entity dies from TNT apple projectile hit) |
| `ClientEventHandler` (HUD, render) | `FabricClientHandler` via `HudRenderCallback` / `WorldRenderEvents` |
| `KeyInputHandler` (dragon breath key) | `FabricClientHandler` via `ClientTickEvents.END_CLIENT_TICK` polling `KeyMapping.consumeClick()` |
| `RewindTracker` + `ClientPlayerRenderHandler` | `FabricClientHandler` via `ClientTickEvents` |
| `babyzombiedroppt` (baby zombie rotten-apple drop) | `FabricBabyZombieHandler` via `ServerLivingEntityEvents.AFTER_DEATH`, checks `entity instanceof Zombie && entity.isBaby()` |

All Fabric handlers are registered in `FabricEventRegistrar.java`, called from `ultimate_apple_modFabric.onInitialize()`.

---

## 5. Networking

### Common — `FireDragonBreathPayload`

```java
public record FireDragonBreathPayload() implements CustomPacketPayload {
    public static final Type<FireDragonBreathPayload> TYPE =
        new Type<>(new ResourceLocation("ultimate_apple_mod", "fire_dragon_breath"));
    public static final StreamCodec<ByteBuf, FireDragonBreathPayload> CODEC =
        StreamCodec.unit(new FireDragonBreathPayload());
}
```

### Forge — unchanged `NetworkHandler.java` + `FireDragonBreathPacket.java`
The existing packet wraps the payload data; no logic changes needed.

### Fabric — `FabricNetworkHandler.java`

```java
// Registration in FabricNetworkHandler.register()
PayloadTypeRegistry.playC2S().register(FireDragonBreathPayload.TYPE, FireDragonBreathPayload.CODEC);
ServerPlayNetworking.registerGlobalReceiver(FireDragonBreathPayload.TYPE, (payload, ctx) ->
    ctx.server().execute(() -> DragonAppleItem.handleFireBreath(ctx.player())));

// Client send (called from FabricClientHandler on key press)
ClientPlayNetworking.send(new FireDragonBreathPayload());
```

### Wichtig: Packet Direction

`FireDragonBreath` ist **C2S (Client → Server)** — der einzige Packet den der Mod sendet.
Fabric braucht kein S2C-Äquivalent: alle anderen Effekte sind vollständig server-seitig
und syncen automatisch über vanilla Entity- und BlockEntity-Datenmechanismen
(`SynchedEntityData`, `ClientboundBlockEntityDataPacket`).

---

## 6. Resources

All files currently under `forge/src/main/resources/assets/` and `forge/src/main/resources/data/` move to `common/src/main/resources/`.

Architectury's Gradle build automatically includes common resources in both the Forge and Fabric JARs — no duplication needed. This is already configured in the root `build.gradle` (verified: `common` is listed as a dependency in both `forge/build.gradle` and `fabric/build.gradle`).

Files that remain platform-specific:
- `forge/src/main/resources/META-INF/mods.toml` — Forge mod metadata
- `fabric/src/main/resources/fabric.mod.json` — Fabric mod metadata

---

## 7. `CopperAppleItem` Fix

Replace Forge-specific registry lookup:
```java
// Before (Forge-only)
Item nextItem = ForgeRegistries.ITEMS.getValue(nextId);

// After (vanilla — works on both platforms)
Item nextItem = BuiltInRegistries.ITEM.get(nextId);
```

---

## 8. Build System

No changes to `build.gradle` files — Architectury already bundles `common` into both `forge` and `fabric` jars. Only resource paths change.

`gradle.properties` at root already declares both platforms. Fabric's `fabric.mod.json` needs updating to list all entry points:
```json
{
  "entrypoints": {
    "main":   ["de.maxi.ultimate_apple_mod.fabric.ultimate_apple_modFabric"],
    "client": ["de.maxi.ultimate_apple_mod.fabric.FabricModClient"]
  }
}
```

---

## File Count Summary

| Module | Before | After |
|---|---|---|
| `common/` | 1 Java file | ~40 Java files + all resources |
| `forge/` | 48 Java files + all resources | ~15 Java files + mods.toml only |
| `fabric/` | 2 Java files | ~15 Java files + fabric.mod.json |
