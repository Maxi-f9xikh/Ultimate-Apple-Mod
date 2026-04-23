# Ultimate Apple Mod Expansion — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add 5 new mechanic-changing apples (Gravity, Orchard Caller, Echo, Glitch, Apple Bomb), enhance the Rotten Apple with baby-zombie shrink behavior, add effects to 6 previously empty apples, and apply no-texture color tinting to new + Copper Apple.

**Architecture:** Custom `MobEffect` subclasses for time-limited state effects (GravityEffect, GlitchEffect); custom `Item`/`ThrowableItemProjectile` subclasses for one-shot complex behaviors; all registered via DeferredRegister in `ultimate_apple_modForge.java`. Color tinting via `IItemColor` in `ModClient.java`. Baby-zombie behavior via `RenderPlayerEvent.Pre` (client visual) + `EntityEvent.Size` (server hitbox).

**Tech Stack:** Minecraft Forge 1.20.1, Java 17, Gradle — compile with `./gradlew :forge:build` from project root.

---

## File Map

**New Java files:**
- `forge/src/main/java/de/maxi/ultimate_apple_mod/effect/GravityEffect.java`
- `forge/src/main/java/de/maxi/ultimate_apple_mod/effect/GlitchEffect.java`
- `forge/src/main/java/de/maxi/ultimate_apple_mod/item/OrchardCallerItem.java`
- `forge/src/main/java/de/maxi/ultimate_apple_mod/item/EchoAppleItem.java`
- `forge/src/main/java/de/maxi/ultimate_apple_mod/item/AppleBombEntity.java`
- `forge/src/main/java/de/maxi/ultimate_apple_mod/item/AppleBombItem.java`
- `forge/src/main/java/de/maxi/ultimate_apple_mod/event/PlayerEffectEventHandler.java`
- `forge/src/main/java/de/maxi/ultimate_apple_mod/event/ClientPlayerRenderHandler.java`

**Modified Java files:**
- `forge/src/main/java/de/maxi/ultimate_apple_mod/forge/ultimate_apple_modForge.java`
- `forge/src/main/java/de/maxi/ultimate_apple_mod/forge/ModClient.java`
- `forge/src/main/java/de/maxi/ultimate_apple_mod/effect/CurseOfRotten.java`

**New JSON resources:**
- `forge/src/main/resources/assets/ultimate_apple_mod/models/item/gravity_apple.json`
- `forge/src/main/resources/assets/ultimate_apple_mod/models/item/orchard_caller.json`
- `forge/src/main/resources/assets/ultimate_apple_mod/models/item/echo_apple.json`
- `forge/src/main/resources/assets/ultimate_apple_mod/models/item/glitch_apple.json`
- `forge/src/main/resources/assets/ultimate_apple_mod/models/item/apple_bomb.json`
- `forge/src/main/resources/data/ultimate_apple_mod/recipes/gravity_apple.json`
- `forge/src/main/resources/data/ultimate_apple_mod/recipes/orchard_caller.json`
- `forge/src/main/resources/data/ultimate_apple_mod/recipes/echo_apple.json`
- `forge/src/main/resources/data/ultimate_apple_mod/recipes/apple_bomb.json`
- `forge/src/main/resources/data/ultimate_apple_mod/loot_modifiers/glitch_apple_ancient_city.json`

**Modified JSON resources:**
- `forge/src/main/resources/assets/ultimate_apple_mod/models/item/copper_apple.json`
- `forge/src/main/resources/data/ultimate_apple_mod/loot_modifiers/global_loot_modifiers.json`
- All 17 lang files in `forge/src/main/resources/assets/ultimate_apple_mod/lang/`

---

## Task 1: GravityEffect

**Files:**
- Create: `forge/src/main/java/de/maxi/ultimate_apple_mod/effect/GravityEffect.java`

- [ ] **Step 1: Create GravityEffect.java**

```java
package de.maxi.ultimate_apple_mod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class GravityEffect extends MobEffect {

    public GravityEffect() {
        super(MobEffectCategory.NEUTRAL, 0x9B00FF);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        if (!entity.level().isClientSide()) {
            entity.setNoGravity(true);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (!entity.level().isClientSide()) {
            entity.setNoGravity(false);
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return;
        entity.setNoGravity(true);
        var movement = entity.getDeltaMovement();
        entity.setDeltaMovement(movement.x, Math.min(movement.y + 0.08, 0.4), movement.z);
    }
}
```

- [ ] **Step 2: Verify compilation**

```bash
./gradlew :forge:build
```
Expected: BUILD SUCCESSFUL (or only pre-existing errors — new class alone won't cause failures)

- [ ] **Step 3: Commit**

```bash
git add forge/src/main/java/de/maxi/ultimate_apple_mod/effect/GravityEffect.java
git commit -m "feat: add GravityEffect MobEffect"
```

---

## Task 2: GlitchEffect

**Files:**
- Create: `forge/src/main/java/de/maxi/ultimate_apple_mod/effect/GlitchEffect.java`

- [ ] **Step 1: Create GlitchEffect.java**

```java
package de.maxi.ultimate_apple_mod.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.phys.Vec3;

public class GlitchEffect extends MobEffect {

    private static final String ENTRY_KEY = "glitch_entry_pos";
    private static final double MAX_DEPTH = 3.0;

    public GlitchEffect() {
        super(MobEffectCategory.NEUTRAL, 0x00FF41);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        if (!entity.level().isClientSide()) {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("x", entity.getX());
            tag.putDouble("y", entity.getY());
            tag.putDouble("z", entity.getZ());
            entity.getPersistentData().put(ENTRY_KEY, tag);
            entity.noPhysics = true;
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return;
        entity.noPhysics = true;

        CompoundTag data = entity.getPersistentData();
        if (!data.contains(ENTRY_KEY)) return;

        CompoundTag entryTag = data.getCompound(ENTRY_KEY);
        Vec3 entry = new Vec3(
            entryTag.getDouble("x"),
            entryTag.getDouble("y"),
            entryTag.getDouble("z")
        );

        if (entity.isInWall() && entity.position().distanceTo(entry) > MAX_DEPTH) {
            entity.noPhysics = false;
            entity.teleportTo(entry.x, entry.y, entry.z);
            entity.removeEffect(this);
            data.remove(ENTRY_KEY);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity.level().isClientSide()) return;
        entity.noPhysics = false;
        if (entity.isInWall()) {
            extractFromWall(entity);
        }
        entity.getPersistentData().remove(ENTRY_KEY);
    }

    private void extractFromWall(LivingEntity entity) {
        for (int dy = 1; dy <= 3; dy++) {
            BlockPos check = entity.blockPosition().above(dy);
            if (entity.level().isEmptyBlock(check) && entity.level().isEmptyBlock(check.above())) {
                entity.teleportTo(check.getX() + 0.5, check.getY(), check.getZ() + 0.5);
                return;
            }
        }
        for (int dy = 1; dy <= 3; dy++) {
            BlockPos check = entity.blockPosition().below(dy);
            if (entity.level().isEmptyBlock(check) && entity.level().isEmptyBlock(check.above())) {
                entity.teleportTo(check.getX() + 0.5, check.getY(), check.getZ() + 0.5);
                return;
            }
        }
    }
}
```

- [ ] **Step 2: Verify compilation**

```bash
./gradlew :forge:build
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add forge/src/main/java/de/maxi/ultimate_apple_mod/effect/GlitchEffect.java
git commit -m "feat: add GlitchEffect MobEffect with wall-depth safety"
```

---

## Task 3: OrchardCallerItem

**Files:**
- Create: `forge/src/main/java/de/maxi/ultimate_apple_mod/item/OrchardCallerItem.java`

- [ ] **Step 1: Create OrchardCallerItem.java**

```java
package de.maxi.ultimate_apple_mod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;

public class OrchardCallerItem extends Item {

    public OrchardCallerItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(level instanceof ServerLevel serverLevel) || !(livingEntity instanceof Player player)) {
            return super.finishUsingItem(stack, level, livingEntity);
        }

        BlockPos playerPos = player.blockPosition();

        boolean hasSolidGround = false;
        outer:
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                if (serverLevel.getBlockState(playerPos.offset(dx, -1, dz)).isSolid()) {
                    hasSolidGround = true;
                    break outer;
                }
            }
        }

        if (!hasSolidGround) {
            player.displayClientMessage(
                Component.translatable("message.ultimate_apple_mod.nothing_can_grow"), true);
            return stack;
        }

        int treesPlanted = 0;
        int attempts = 0;
        while (treesPlanted < 4 && attempts < 30) {
            attempts++;
            int dx = serverLevel.getRandom().nextIntBetweenInclusive(-5, 5);
            int dz = serverLevel.getRandom().nextIntBetweenInclusive(-5, 5);
            if (dx == 0 && dz == 0) continue;

            BlockPos base = playerPos.offset(dx, 0, dz);
            for (int dy = 2; dy >= -3; dy--) {
                BlockPos groundPos = base.offset(0, dy - 1, 0);
                BlockPos saplingPos = base.offset(0, dy, 0);

                if (!serverLevel.getBlockState(groundPos).isSolid()) continue;
                if (!serverLevel.isEmptyBlock(saplingPos)) continue;
                if (!serverLevel.isEmptyBlock(saplingPos.above())) continue;
                if (!serverLevel.isEmptyBlock(saplingPos.above(2))) continue;

                var groundState = serverLevel.getBlockState(groundPos);
                if (!groundState.is(BlockTags.DIRT)) {
                    serverLevel.setBlock(groundPos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                }

                serverLevel.setBlock(saplingPos, Blocks.OAK_SAPLING.defaultBlockState(), 3);
                var saplingState = serverLevel.getBlockState(saplingPos);
                if (saplingState.getBlock() instanceof SaplingBlock saplingBlock) {
                    saplingBlock.performBonemeal(serverLevel, serverLevel.getRandom(), saplingPos, saplingState);
                }

                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    saplingPos.getX() + 0.5, saplingPos.getY() + 0.5, saplingPos.getZ() + 0.5,
                    5, 0.5, 0.5, 0.5, 0.1);

                treesPlanted++;
                break;
            }
        }

        return super.finishUsingItem(stack, level, livingEntity);
    }
}
```

- [ ] **Step 2: Verify compilation**

```bash
./gradlew :forge:build
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add forge/src/main/java/de/maxi/ultimate_apple_mod/item/OrchardCallerItem.java
git commit -m "feat: add OrchardCallerItem with ground/grass validation"
```

---

## Task 4: EchoAppleItem

**Files:**
- Create: `forge/src/main/java/de/maxi/ultimate_apple_mod/item/EchoAppleItem.java`

- [ ] **Step 1: Create EchoAppleItem.java**

```java
package de.maxi.ultimate_apple_mod.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EchoAppleItem extends Item {

    private static final String POS_KEY = "echo_apple_pos";

    public EchoAppleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (level.isClientSide() || !(livingEntity instanceof Player player)) {
            return super.finishUsingItem(stack, level, livingEntity);
        }

        CompoundTag data = player.getPersistentData();

        if (data.contains(POS_KEY)) {
            CompoundTag posTag = data.getCompound(POS_KEY);
            String savedDim = posTag.getString("dim");
            String currentDim = level.dimension().location().toString();

            if (!savedDim.equals(currentDim)) {
                player.displayClientMessage(
                    Component.translatable("message.ultimate_apple_mod.echo_wrong_dim"), true);
                return stack;
            }
        }

        ItemStack result = super.finishUsingItem(stack, level, livingEntity);
        ServerLevel serverLevel = (ServerLevel) level;

        if (!data.contains(POS_KEY)) {
            CompoundTag posTag = new CompoundTag();
            posTag.putDouble("x", player.getX());
            posTag.putDouble("y", player.getY());
            posTag.putDouble("z", player.getZ());
            posTag.putString("dim", level.dimension().location().toString());
            data.put(POS_KEY, posTag);

            player.displayClientMessage(
                Component.translatable("message.ultimate_apple_mod.echo_set"), true);
            serverLevel.sendParticles(ParticleTypes.SCULK_SOUL,
                player.getX(), player.getY() + 1.0, player.getZ(),
                8, 0.3, 0.3, 0.3, 0.05);
        } else {
            CompoundTag posTag = data.getCompound(POS_KEY);
            double x = posTag.getDouble("x");
            double y = posTag.getDouble("y");
            double z = posTag.getDouble("z");

            serverLevel.sendParticles(ParticleTypes.PORTAL,
                player.getX(), player.getY() + 1.0, player.getZ(),
                20, 0.5, 1.0, 0.5, 0.1);

            player.teleportTo(x, y, z);
            level.playSound(null, x, y, z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);

            serverLevel.sendParticles(ParticleTypes.PORTAL,
                x, y + 1.0, z, 20, 0.5, 1.0, 0.5, 0.1);

            data.remove(POS_KEY);
            player.displayClientMessage(
                Component.translatable("message.ultimate_apple_mod.echo_return"), true);
        }

        return result;
    }
}
```

- [ ] **Step 2: Verify compilation**

```bash
./gradlew :forge:build
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add forge/src/main/java/de/maxi/ultimate_apple_mod/item/EchoAppleItem.java
git commit -m "feat: add EchoAppleItem with NBT waypoint teleport"
```

---

## Task 5: AppleBombEntity + AppleBombItem

**Files:**
- Create: `forge/src/main/java/de/maxi/ultimate_apple_mod/item/AppleBombEntity.java`
- Create: `forge/src/main/java/de/maxi/ultimate_apple_mod/item/AppleBombItem.java`

- [ ] **Step 1: Create AppleBombEntity.java**

```java
package de.maxi.ultimate_apple_mod.item;

import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class AppleBombEntity extends ThrowableItemProjectile {

    public AppleBombEntity(EntityType<? extends AppleBombEntity> type, Level level) {
        super(type, level);
    }

    public AppleBombEntity(LivingEntity thrower, Level level) {
        super(ultimate_apple_modForge.APPLE_BOMB_ENTITY.get(), thrower, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ultimate_apple_modForge.APPLE_BOMB.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        applyExplosion();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        applyExplosion();
    }

    private void applyExplosion() {
        if (level().isClientSide()) return;
        ServerLevel serverLevel = (ServerLevel) level();

        serverLevel.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(4.0))
            .forEach(entity -> {
                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0));
            });

        serverLevel.sendParticles(ParticleTypes.CRIT,
            getX(), getY(), getZ(), 20, 0.5, 0.5, 0.5, 0.3);
        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
            getX(), getY(), getZ(), 15, 0.5, 0.5, 0.5, 0.1);
        level().playSound(null, getX(), getY(), getZ(),
            SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.5f, 2.0f);

        discard();
    }
}
```

- [ ] **Step 2: Create AppleBombItem.java**

```java
package de.maxi.ultimate_apple_mod.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AppleBombItem extends Item {

    public AppleBombItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL,
            0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));

        if (!level.isClientSide()) {
            AppleBombEntity bomb = new AppleBombEntity(player, level);
            bomb.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 1.0f);
            level.addFreshEntity(bomb);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
```

- [ ] **Step 3: Skip build here — compile in Task 6**

`AppleBombEntity` references `ultimate_apple_modForge.APPLE_BOMB_ENTITY` and `APPLE_BOMB` which don't exist until Task 6. Java compiles the whole module at once, so the build will succeed only after Task 6 adds those fields. Do not run `./gradlew :forge:build` here.

- [ ] **Step 4: Commit**

```bash
git add forge/src/main/java/de/maxi/ultimate_apple_mod/item/AppleBombEntity.java
git add forge/src/main/java/de/maxi/ultimate_apple_mod/item/AppleBombItem.java
git commit -m "feat: add AppleBombEntity projectile and AppleBombItem"
```

---

## Task 6: Register everything in ultimate_apple_modForge.java

**Files:**
- Modify: `forge/src/main/java/de/maxi/ultimate_apple_mod/forge/ultimate_apple_modForge.java`

This task adds: ENTITY_TYPES register, GravityEffect, GlitchEffect, 5 new items, updates creative tab, and updates FoodProperties for 6 existing apples.

- [ ] **Step 1: Replace the full contents of ultimate_apple_modForge.java**

```java
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
            .stacksTo(16)));

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
```

- [ ] **Step 2: Verify compilation**

```bash
./gradlew :forge:build
```
Expected: BUILD SUCCESSFUL — all new items, effects, and entity type registered without errors.

- [ ] **Step 3: Commit**

```bash
git add forge/src/main/java/de/maxi/ultimate_apple_mod/forge/ultimate_apple_modForge.java
git commit -m "feat: register new effects, entity type, 5 new apples, and fix existing apple effects"
```

---

## Task 7: ModClient.java — IItemColor + EntityRenderer

**Files:**
- Modify: `forge/src/main/java/de/maxi/ultimate_apple_mod/forge/ModClient.java`

- [ ] **Step 1: Replace full contents of ModClient.java**

```java
package de.maxi.ultimate_apple_mod.forge;

import de.maxi.ultimate_apple_mod.item.AppleBombEntity;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static de.maxi.ultimate_apple_mod.ultimate_apple_mod.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClient {

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> 0x9B00FF, ultimate_apple_modForge.GRAVITY_APPLE.get());
        event.register((stack, tintIndex) -> 0x44CC44, ultimate_apple_modForge.ORCHARD_CALLER.get());
        event.register((stack, tintIndex) -> 0x00CCDD, ultimate_apple_modForge.ECHO_APPLE.get());
        event.register((stack, tintIndex) -> 0x00FF41, ultimate_apple_modForge.GLITCH_APPLE.get());
        event.register((stack, tintIndex) -> 0xFF6600, ultimate_apple_modForge.APPLE_BOMB.get());
        event.register((stack, tintIndex) -> 0xB87333, ultimate_apple_modForge.COPPER_APPLE.get());
    }

    @SubscribeEvent
    public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ultimate_apple_modForge.APPLE_BOMB_ENTITY.get(),
            ThrownItemRenderer::new);
    }
}
```

- [ ] **Step 2: Verify compilation**

```bash
./gradlew :forge:build
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add forge/src/main/java/de/maxi/ultimate_apple_mod/forge/ModClient.java
git commit -m "feat: register IItemColor tints and AppleBomb entity renderer"
```

---

## Task 8: PlayerEffectEventHandler — Server Hitbox (Baby-Zombie)

**Files:**
- Create: `forge/src/main/java/de/maxi/ultimate_apple_mod/event/PlayerEffectEventHandler.java`

- [ ] **Step 1: Create PlayerEffectEventHandler.java**

```java
package de.maxi.ultimate_apple_mod.event;

import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEffectEventHandler {

    @SubscribeEvent
    public static void onEntitySize(EntityEvent.Size event) {
        if (event.getEntity() instanceof Player player
                && player.hasEffect(ultimate_apple_modForge.CURSE_OF_ROTTEN.get())) {
            event.setNewSize(EntityDimensions.scalable(0.3f, 0.9f));
        }
    }
}
```

- [ ] **Step 2: Verify compilation**

```bash
./gradlew :forge:build
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add forge/src/main/java/de/maxi/ultimate_apple_mod/event/PlayerEffectEventHandler.java
git commit -m "feat: shrink player hitbox to 0.3x0.9 during Curse of Rotten"
```

---

## Task 9: ClientPlayerRenderHandler — Visual Scale (Baby-Zombie)

**Files:**
- Create: `forge/src/main/java/de/maxi/ultimate_apple_mod/event/ClientPlayerRenderHandler.java`

- [ ] **Step 1: Create ClientPlayerRenderHandler.java**

```java
package de.maxi.ultimate_apple_mod.event;

import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientPlayerRenderHandler {

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (event.getEntity().hasEffect(ultimate_apple_modForge.CURSE_OF_ROTTEN.get())) {
            event.getPoseStack().scale(0.5f, 0.5f, 0.5f);
        }
    }
}
```

- [ ] **Step 2: Verify compilation**

```bash
./gradlew :forge:build
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add forge/src/main/java/de/maxi/ultimate_apple_mod/event/ClientPlayerRenderHandler.java
git commit -m "feat: scale player render to 50% during Curse of Rotten"
```

---

## Task 10: CurseOfRotten — Shrink Particles

**Files:**
- Modify: `forge/src/main/java/de/maxi/ultimate_apple_mod/effect/CurseOfRotten.java`

- [ ] **Step 1: Add addAttributeModifiers override to CurseOfRotten.java**

Replace the full file contents:

```java
package de.maxi.ultimate_apple_mod.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CurseOfRotten extends MobEffect {

    public CurseOfRotten() {
        super(MobEffectCategory.HARMFUL, 0x7e5c3d);

        this.addAttributeModifier(
            Attributes.MOVEMENT_SPEED,
            "7107DE5E-7CE8-4030-940E-514C1F160890",
            1.5D,
            AttributeModifier.Operation.MULTIPLY_TOTAL
        );
        this.addAttributeModifier(
            Attributes.MAX_HEALTH,
            "5D6F0BA2-1186-46AC-B896-C61C5CEE99CC",
            20.0D,
            AttributeModifier.Operation.ADDITION
        );
        this.addAttributeModifier(
            Attributes.ATTACK_SPEED,
            "3FA243A0-4953-4B13-801F-79B0F5D6A093",
            2.0D,
            AttributeModifier.Operation.ADDITION
        );
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        if (!entity.level().isClientSide() && entity.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.WITCH,
                entity.getX(), entity.getY() + 1.0, entity.getZ(),
                6, 0.3, 0.5, 0.3, 0.1);
        }
    }
}
```

- [ ] **Step 2: Verify compilation**

```bash
./gradlew :forge:build
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add forge/src/main/java/de/maxi/ultimate_apple_mod/effect/CurseOfRotten.java
git commit -m "feat: spawn witch particles on Curse of Rotten application"
```

---

## Task 11: Model JSON Files

**Files:**
- Create 5 new model files
- Modify `copper_apple.json`

- [ ] **Step 1: Create gravity_apple.json**

`forge/src/main/resources/assets/ultimate_apple_mod/models/item/gravity_apple.json`:
```json
{
  "parent": "item/generated",
  "textures": {
    "layer0": "minecraft:item/apple"
  }
}
```

- [ ] **Step 2: Create orchard_caller.json**

`forge/src/main/resources/assets/ultimate_apple_mod/models/item/orchard_caller.json`:
```json
{
  "parent": "item/generated",
  "textures": {
    "layer0": "minecraft:item/apple"
  }
}
```

- [ ] **Step 3: Create echo_apple.json**

`forge/src/main/resources/assets/ultimate_apple_mod/models/item/echo_apple.json`:
```json
{
  "parent": "item/generated",
  "textures": {
    "layer0": "minecraft:item/apple"
  }
}
```

- [ ] **Step 4: Create glitch_apple.json**

`forge/src/main/resources/assets/ultimate_apple_mod/models/item/glitch_apple.json`:
```json
{
  "parent": "item/generated",
  "textures": {
    "layer0": "minecraft:item/apple"
  }
}
```

- [ ] **Step 5: Create apple_bomb.json**

`forge/src/main/resources/assets/ultimate_apple_mod/models/item/apple_bomb.json`:
```json
{
  "parent": "item/generated",
  "textures": {
    "layer0": "minecraft:item/apple"
  }
}
```

- [ ] **Step 6: Update copper_apple.json to use vanilla apple base**

`forge/src/main/resources/assets/ultimate_apple_mod/models/item/copper_apple.json` — replace contents:
```json
{
  "parent": "item/generated",
  "textures": {
    "layer0": "minecraft:item/apple"
  }
}
```

- [ ] **Step 7: Commit**

```bash
git add forge/src/main/resources/assets/ultimate_apple_mod/models/item/gravity_apple.json
git add forge/src/main/resources/assets/ultimate_apple_mod/models/item/orchard_caller.json
git add forge/src/main/resources/assets/ultimate_apple_mod/models/item/echo_apple.json
git add forge/src/main/resources/assets/ultimate_apple_mod/models/item/glitch_apple.json
git add forge/src/main/resources/assets/ultimate_apple_mod/models/item/apple_bomb.json
git add forge/src/main/resources/assets/ultimate_apple_mod/models/item/copper_apple.json
git commit -m "feat: add model JSONs for new apples, update copper_apple to vanilla apple base"
```

---

## Task 12: Recipe JSON Files

**Files:** Create 4 recipe files in `forge/src/main/resources/data/ultimate_apple_mod/recipes/`

- [ ] **Step 1: Create gravity_apple.json**

```json
{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "PPP",
    "PAP",
    "PPP"
  ],
  "key": {
    "P": { "item": "minecraft:phantom_membrane" },
    "A": { "item": "minecraft:apple" }
  },
  "result": {
    "item": "ultimate_apple_mod:gravity_apple"
  }
}
```

- [ ] **Step 2: Create orchard_caller.json**

```json
{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "SBS",
    "BAB",
    "SBS"
  ],
  "key": {
    "S": { "item": "minecraft:oak_sapling" },
    "B": { "item": "minecraft:bone_meal" },
    "A": { "item": "minecraft:apple" }
  },
  "result": {
    "item": "ultimate_apple_mod:orchard_caller"
  }
}
```

- [ ] **Step 3: Create echo_apple.json**

4 echo shards (corners) + 4 amethyst shards (edges) + 1 apple (center):

```json
{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "EAE",
    "APA",
    "EAE"
  ],
  "key": {
    "E": { "item": "minecraft:echo_shard" },
    "A": { "item": "minecraft:amethyst_shard" },
    "P": { "item": "minecraft:apple" }
  },
  "result": {
    "item": "ultimate_apple_mod:echo_apple"
  }
}
```

- [ ] **Step 4: Create apple_bomb.json** (yields 2)

```json
{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "GGG",
    "GBG",
    "GAG"
  ],
  "key": {
    "G": { "item": "minecraft:gunpowder" },
    "B": { "item": "minecraft:blaze_powder" },
    "A": { "item": "ultimate_apple_mod:diamond_apple" }
  },
  "result": {
    "item": "ultimate_apple_mod:apple_bomb",
    "count": 2
  }
}
```

- [ ] **Step 5: Commit all recipe files**

```bash
git add forge/src/main/resources/data/ultimate_apple_mod/recipes/gravity_apple.json
git add forge/src/main/resources/data/ultimate_apple_mod/recipes/orchard_caller.json
git add forge/src/main/resources/data/ultimate_apple_mod/recipes/echo_apple.json
git add forge/src/main/resources/data/ultimate_apple_mod/recipes/apple_bomb.json
git commit -m "feat: add crafting recipes for 4 new apples"
```

---

## Task 13: Loot Modifier — Glitch Apple

**Files:**
- Modify: `forge/src/main/resources/data/ultimate_apple_mod/loot_modifiers/global_loot_modifiers.json`
- Create: `forge/src/main/resources/data/ultimate_apple_mod/loot_modifiers/glitch_apple_ancient_city.json`

- [ ] **Step 1: Create glitch_apple_ancient_city.json**

```json
{
  "type": "forge:add_items",
  "conditions": [
    {
      "condition": "forge:loot_table_id",
      "loot_table_id": "minecraft:chests/ancient_city"
    },
    {
      "condition": "random_chance",
      "chance": 0.25
    }
  ],
  "items": [
    {
      "item": "ultimate_apple_mod:glitch_apple",
      "weight": 1
    }
  ]
}
```

- [ ] **Step 2: Update global_loot_modifiers.json — add the new entry**

```json
{
  "replace": false,
  "entries": [
    "ultimate_apple_mod:BA_Nether_Fortress",
    "ultimate_apple_mod:EPA_Stronghold",
    "ultimate_apple_mod:EPA_End_City",
    "ultimate_apple_mod:glitch_apple_ancient_city"
  ]
}
```

- [ ] **Step 3: Commit**

```bash
git add forge/src/main/resources/data/ultimate_apple_mod/loot_modifiers/glitch_apple_ancient_city.json
git add forge/src/main/resources/data/ultimate_apple_mod/loot_modifiers/global_loot_modifiers.json
git commit -m "feat: add Glitch Apple to Ancient City chest loot (25% chance)"
```

---

## Task 14: Lang Files

**Files:** All 17 lang files in `forge/src/main/resources/assets/ultimate_apple_mod/lang/`

- [ ] **Step 1: Update en_us.json** — add all new keys to the existing JSON object

Add these entries to `en_us.json`:
```json
"item.ultimate_apple_mod.copper_apple": "Copper Apple",
"item.ultimate_apple_mod.gravity_apple": "Gravity Apple",
"item.ultimate_apple_mod.orchard_caller": "Orchard Caller",
"item.ultimate_apple_mod.echo_apple": "Echo Apple",
"item.ultimate_apple_mod.glitch_apple": "Glitch Apple",
"item.ultimate_apple_mod.apple_bomb": "Apple Bomb",
"effect.ultimate_apple_mod.gravity_inversion": "Gravity Inversion",
"effect.ultimate_apple_mod.glitch": "Glitch",
"tooltip.ultimate_apple_mod.echo_apple.line1": "§6First use sets a return point.",
"tooltip.ultimate_apple_mod.echo_apple.line2": "§7Second use teleports you back.",
"tooltip.ultimate_apple_mod.glitch_apple.line1": "§6Phase through walls for 5 seconds.",
"tooltip.ultimate_apple_mod.glitch_apple.line2": "§7Max wall thickness: 2 blocks.",
"tooltip.ultimate_apple_mod.apple_bomb.line1": "§6Throw to grant nearby entities power.",
"message.ultimate_apple_mod.nothing_can_grow": "Nothing can grow here.",
"message.ultimate_apple_mod.echo_set": "Return point set!",
"message.ultimate_apple_mod.echo_return": "Echo!",
"message.ultimate_apple_mod.echo_wrong_dim": "Cannot teleport across dimensions."
```

- [ ] **Step 2: Update de_de.json** — add German translations

Add these entries to `de_de.json`:
```json
"item.ultimate_apple_mod.copper_apple": "Kupferapfel",
"item.ultimate_apple_mod.gravity_apple": "Schwerkraft-Apfel",
"item.ultimate_apple_mod.orchard_caller": "Obstgarten-Rufer",
"item.ultimate_apple_mod.echo_apple": "Echo-Apfel",
"item.ultimate_apple_mod.glitch_apple": "Glitch-Apfel",
"item.ultimate_apple_mod.apple_bomb": "Apfel-Bombe",
"effect.ultimate_apple_mod.gravity_inversion": "Schwerkraft-Umkehr",
"effect.ultimate_apple_mod.glitch": "Glitch",
"tooltip.ultimate_apple_mod.echo_apple.line1": "§6Erster Verzehr setzt einen Rückkehrpunkt.",
"tooltip.ultimate_apple_mod.echo_apple.line2": "§7Zweiter Verzehr teleportiert zurück.",
"tooltip.ultimate_apple_mod.glitch_apple.line1": "§6Gehe 5 Sekunden durch Wände.",
"tooltip.ultimate_apple_mod.glitch_apple.line2": "§7Maximale Wanddicke: 2 Blöcke.",
"tooltip.ultimate_apple_mod.apple_bomb.line1": "§6Werfen verleiht Effekte im Umkreis.",
"message.ultimate_apple_mod.nothing_can_grow": "Hier kann nichts wachsen.",
"message.ultimate_apple_mod.echo_set": "Rückkehrpunkt gesetzt!",
"message.ultimate_apple_mod.echo_return": "Echo!",
"message.ultimate_apple_mod.echo_wrong_dim": "Dimensionswechsel nicht möglich."
```

- [ ] **Step 3: Add English fallback to all other 15 lang files**

For each of the remaining lang files (`en_gb.json`, `en_au.json`, `en_ca.json`, `en_nz.json`, `es_es.json`, `es_ar.json`, `es_cl.json`, `es_ec.json`, `es_mx.json`, `es_uy.json`, `es_ve.json`, `fr_ca.json`, `fr_fr.json`, `pt_br.json`, `pt_pt.json`, `ja_jp.json`, `ko_kr.json`, `ru_ru.json`, `zh_cn.json`, `zh_tw.json`, `ro_ro.json`, `tr_tr.json`, `da_dk.json`, `bar.json`), add the same 17 entries as en_us.json (English fallback). Only translate if you know the language — otherwise the English text is correct mod behavior.

- [ ] **Step 4: Commit**

```bash
git add forge/src/main/resources/assets/ultimate_apple_mod/lang/
git commit -m "feat: update all lang files with new apple and effect translations"
```

---

## Task 15: Tooltip Rendering for Echo + Glitch + Apple Bomb

The tooltips added in lang files are only displayed if the item's `appendHoverText()` method emits them. Add tooltip support to `EchoAppleItem`, and create inline subclasses for `GLITCH_APPLE` and `APPLE_BOMB` — or use a simpler approach: create a helper.

- [ ] **Step 1: Add appendHoverText to EchoAppleItem.java**

Add this method to the `EchoAppleItem` class body:

```java
@Override
public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level,
        java.util.List<net.minecraft.network.chat.Component> tooltipComponents,
        net.minecraft.world.item.TooltipFlag isAdvanced) {
    tooltipComponents.add(Component.translatable("tooltip.ultimate_apple_mod.echo_apple.line1"));
    tooltipComponents.add(Component.translatable("tooltip.ultimate_apple_mod.echo_apple.line2"));
}
```

Add the missing import to `EchoAppleItem.java`:
```java
import net.minecraft.world.item.TooltipFlag;
import java.util.List;
```

- [ ] **Step 2: Add appendHoverText to AppleBombItem.java**

Add this method to the `AppleBombItem` class body:

```java
@Override
public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level,
        java.util.List<net.minecraft.network.chat.Component> tooltipComponents,
        net.minecraft.world.item.TooltipFlag isAdvanced) {
    tooltipComponents.add(net.minecraft.network.chat.Component.translatable(
        "tooltip.ultimate_apple_mod.apple_bomb.line1"));
}
```

- [ ] **Step 3: Create GlitchAppleTooltipItem helper in ultimate_apple_modForge.java**

In `ultimate_apple_modForge.java`, replace the `GLITCH_APPLE` registration with an anonymous class that adds the tooltip:

```java
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
```

- [ ] **Step 4: Verify compilation**

```bash
./gradlew :forge:build
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add forge/src/main/java/de/maxi/ultimate_apple_mod/item/EchoAppleItem.java
git add forge/src/main/java/de/maxi/ultimate_apple_mod/item/AppleBombItem.java
git add forge/src/main/java/de/maxi/ultimate_apple_mod/forge/ultimate_apple_modForge.java
git commit -m "feat: add hover tooltips for Echo Apple, Glitch Apple, Apple Bomb"
```

---

## Task 16: Final Build + In-Game Verification

- [ ] **Step 1: Full clean build**

```bash
./gradlew clean :forge:build
```
Expected: BUILD SUCCESSFUL with 0 errors

- [ ] **Step 2: In-game checklist**

Start a creative world with `./gradlew :forge:runClient` and verify:

**Visual (tinting):**
- `/give @p ultimate_apple_mod:gravity_apple` → appears purple
- `/give @p ultimate_apple_mod:orchard_caller` → appears green
- `/give @p ultimate_apple_mod:echo_apple` → appears cyan
- `/give @p ultimate_apple_mod:glitch_apple` → appears matrix-green
- `/give @p ultimate_apple_mod:apple_bomb` → appears orange-red
- `/give @p ultimate_apple_mod:copper_apple` → appears copper-brown

**Gravity Apple:** Eat → float upward for 10s, `Gravity Inversion` HUD icon appears, fall normally after

**Orchard Caller:** Eat on grass → 4 oak trees grow nearby. Eat while airborne → "Nothing can grow here." in action bar, item not consumed

**Echo Apple:** Eat once → "Return point set!" action bar. Move far away. Eat second → "Echo!" teleport back

**Glitch Apple:** Eat → walk into a 1-2 block thick wall → pass through. Walk into a 4-block thick wall → auto-teleport back to entry

**Apple Bomb:** Right-click → projectile thrown. On impact → CRIT/HAPPY_VILLAGER particles + nearby entities get regen/speed/strength

**Rotten Apple:** Eat → shrink to baby-zombie size visually + hitbox (walk through 1-block gaps), witch particles on effect start, speed/attack bonuses active

**Existing apples:** `/give @p ultimate_apple_mod:lapislazuli_apple` → eat → Luck II for 30s

- [ ] **Step 3: Final commit**

```bash
git add -A
git commit -m "feat: Ultimate Apple Mod expansion complete — 5 new apples, baby-zombie Rotten Apple, effect updates"
```
