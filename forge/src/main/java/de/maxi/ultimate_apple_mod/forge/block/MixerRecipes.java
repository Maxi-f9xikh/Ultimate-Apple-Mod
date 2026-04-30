package de.maxi.ultimate_apple_mod.forge.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Maps each mixable item to its ShakeContribution.
 * Contributions from two different items are combined in MixerBlockEntity:
 * effects are deduplicated (max amp, max duration), charges summed, flags OR'd.
 */
public final class MixerRecipes {

    // ── Data types ─────────────────────────────────────────────────────────

    /** A single mob-effect stored in the shake's NBT. */
    public record EffectData(ResourceLocation id, int duration, int amplifier) {}

    /** What one apple item contributes to a shake. */
    public record ShakeContribution(
        List<EffectData> effects,
        int dragonCharges,
        boolean lifesteal,
        boolean witherCurse,
        /**
         * When true this ingredient removes all active effects from the player when
         * the shake is drunk, just like eating a Honey Apple.
         * When ANY ingredient in the pair has this set, NO effects at all are added —
         * the resulting shake only cleanses.
         */
        boolean clearsEffects,
        /**
         * Multiplier applied to every effect duration in the combined shake.
         * The Longevity Apple sets this to 2.0; all others keep the default 1.0.
         * The build step takes the MAX of the two ingredient multipliers.
         */
        double durationMultiplier,
        /**
         * When true, drinking/throwing the shake applies a massive upward velocity burst
         * (same mechanic as eating the Void Apple directly).
         */
        boolean voidLaunch,
        /**
         * When true, drinking the shake teleports the player back 5 seconds in time
         * using the RewindTracker position history (same as Rewind Apple).
         */
        boolean rewindEffect,
        /**
         * When true, drinking the shake plants up to 6 oak trees around the player
         * (same as Orchard Apple, but 2 more trees because of the mixing bonus).
         */
        boolean orchardSpawn,
        /**
         * When true, drinking/applying the shake teleports the entity up to 256 blocks
         * in their current look direction (same as Ender Pearl Apple).
         */
        boolean enderTeleport,
        /**
         * When true, the shake is a throwable item instead of a drinkable one.
         * Right-clicking throws a ShakeBombEntity that applies all stored effects to
         * any entity it hits (direct hit) or nearby entities (AOE on block hit).
         */
        boolean isBomb
    ) {}

    // ── Registry ───────────────────────────────────────────────────────────

    private static final Map<ResourceLocation, ShakeContribution> REGISTRY = new HashMap<>();

    static {
        // ── Vanilla effect shortcuts ───────────────────────────────────────
        ResourceLocation regen       = mc("regeneration");
        ResourceLocation healthBoost = mc("health_boost");
        ResourceLocation resistance  = mc("resistance");
        ResourceLocation strength    = mc("strength");
        ResourceLocation speed       = mc("speed");
        ResourceLocation haste       = mc("haste");
        ResourceLocation fireRes     = mc("fire_resistance");
        ResourceLocation nightVision = mc("night_vision");
        ResourceLocation luck        = mc("luck");
        ResourceLocation jump        = mc("jump_boost");
        ResourceLocation waterBreath = mc("water_breathing");
        ResourceLocation saturation  = mc("saturation");
        ResourceLocation absorption  = mc("absorption");
        ResourceLocation hunger      = mc("hunger");
        ResourceLocation nausea      = mc("nausea");
        ResourceLocation slowness    = mc("slowness");
        ResourceLocation curseOfRotten = mod("curse_of_rotten");

        // ── Vanilla apples ─────────────────────────────────────────────────

        registerVanilla("apple", List.of(
            new EffectData(regen,      20 * 5, 0),     // Regen, 5s
            new EffectData(saturation, 20 * 5, 0)      // Saturation, 5s
        ), 0, false, false);

        registerVanilla("golden_apple", List.of(
            new EffectData(absorption, 20 * 120, 0),   // Absorption I, 2 min
            new EffectData(regen,      20 *   5, 1)    // Regen II, 5s
        ), 0, false, false);

        registerVanilla("enchanted_golden_apple", List.of(
            new EffectData(absorption, 20 * 120, 3),   // Absorption IV, 2 min
            new EffectData(regen,      20 *  30, 4),   // Regen V, 30s
            new EffectData(resistance, 20 * 300, 0),   // Resistance I, 5 min
            new EffectData(fireRes,    20 * 300, 0)    // Fire Resistance, 5 min
        ), 0, false, false);

        // ── Mod apples ─────────────────────────────────────────────────────

        register("diamond_apple", List.of(
            new EffectData(healthBoost, 20 * 60, 2),   // Health Boost III, 60s
            new EffectData(regen,       20 * 20, 1),   // Regen II, 20s
            new EffectData(resistance,  20 * 30, 1),   // Resistance II, 30s
            new EffectData(absorption,  20 * 60, 0)    // Absorption I, 60s
        ), 0, false, false);

        register("lapislazuli_apple", List.of(
            new EffectData(nightVision, 20 * 300, 0),  // Night Vision, 5 min
            new EffectData(luck, 20 * 120, 0)           // Luck I, 2 min
        ), 0, false, false);

        register("emerald_apple", List.of(
            new EffectData(luck,        20 * 60, 1),   // Luck II, 60s
            new EffectData(nightVision, 20 * 30, 0)    // Night Vision, 30s
        ), 0, false, false);

        ResourceLocation glowing  = mc("glowing");
        register("redstone_apple", List.of(
            new EffectData(speed,    20 * 20, 2),      // Speed III, 20s
            new EffectData(haste,    20 * 20, 2),      // Haste III, 20s
            new EffectData(glowing,  20 * 20, 0),      // Glowing, 20s
            new EffectData(strength, 20 * 15, 0)       // Strength I, 15s
        ), 0, false, false);

        register("netherite_apple", List.of(
            new EffectData(resistance,  20 * 120, 2),  // Resistance III, 2 min
            new EffectData(fireRes,     20 * 120, 0),  // Fire Resistance, 2 min
            new EffectData(healthBoost, 20 * 120, 3),  // Health Boost IV, 2 min
            new EffectData(regen,       20 *  30, 2),  // Regen III, 30s
            new EffectData(strength,    20 *  30, 1),  // Strength II, 30s
            new EffectData(absorption,  20 * 120, 2)   // Absorption III, 2 min
        ), 0, false, false);

        register("iron_apple", List.of(
            new EffectData(healthBoost, 20 * 30, 0),   // Health Boost I, 30s
            new EffectData(regen,       20 * 10, 0),   // Regen I, 10s
            new EffectData(resistance,  20 * 15, 0)    // Resistance I, 15s
        ), 0, false, false);

        register("rotten_apple", List.of(
            new EffectData(curseOfRotten, 20 * 20, 0), // Curse of Rotten, 20s
            new EffectData(nausea,        20 *  5, 0)  // Nausea, 5s
        ), 0, false, false);

        register("roasted_apple", List.of(
            new EffectData(healthBoost, 20 * 20, 0),   // Health Boost, 20s
            new EffectData(saturation,  20 * 10, 0)    // Saturation, 10s
        ), 0, false, false);

        register("baked_apple", List.of(
            new EffectData(regen, 20 * 5, 0)           // Regen, 5s
        ), 0, false, false);

        register("burnt_apple", List.of(
            new EffectData(hunger,   20 * 15, 1),      // Hunger II, 15s
            new EffectData(nausea,   20 *  5, 0),      // Nausea, 5s
            new EffectData(strength, 20 *  5, 0),      // Strength, 5s
            new EffectData(fireRes,  20 * 15, 0)       // Fire Resistance, 15s
        ), 0, false, false);

        register("blaze_apple", List.of(
            new EffectData(regen,    20 *  5, 0),      // Regen, 5s
            new EffectData(strength, 20 *  5, 0),      // Strength, 5s
            new EffectData(fireRes,  20 * 15, 0)       // Fire Resistance, 15s
        ), 0, false, false);

        register("pear_apple", List.of(
            new EffectData(regen,      20 * 15, 0),    // Regen, 15s
            new EffectData(saturation, 20 *  5, 0)     // Saturation, 5s
        ), 0, false, false);

        register("copper_apple", List.of(
            new EffectData(haste,      20 * 25, 1),    // Haste II, 25s
            new EffectData(strength,   20 * 25, 0),    // Strength I, 25s
            new EffectData(resistance, 20 * 25, 0)     // Resistance I, 25s
        ), 0, false, false);

        // ender_pearl_apple registered below via registerEnderTeleport (line ~295)

        register("moon_apple", List.of(
            new EffectData(mod("moon_gravity"), 20 * 30, 0), // Moon Gravity, 30s
            new EffectData(jump,                20 * 30, 1)  // Jump Boost II, 30s
        ), 0, false, false);

        // echo_apple and rewind_apple intentionally excluded —
        // their core abilities (teleportation) cannot be meaningfully stored in a shake.

        // apple_bomb registered below via registerBomb (line ~300)

        register("wither_apple", List.of(
            new EffectData(absorption,  20 * 30, 2),   // Absorption IV, 30s
            new EffectData(resistance,  20 * 10, 1),   // Resistance II, 10s
            new EffectData(regen,       20 *  5, 1)    // Regen II, 5s
        ), 0, true, true);  // + lifesteal + witherCurse

        // honey_apple cleanses all effects — its own effects are intentionally excluded
        // so the resulting shake clears the player's effects when drunk.
        registerCleansing("honey_apple");

        register("dragon_apple", List.of(
            new EffectData(absorption, 20 * 10, 3),    // Absorption IV, 10s
            new EffectData(strength,   20 * 10, 1),    // Strength II, 10s
            new EffectData(regen,      20 * 10, 2)     // Regen III, 10s
        ), 1, false, false);                           // +1 dragon charge

        register("nether_star_apple", List.of(
            new EffectData(resistance, 40, 4),         // Resistance V, 2s
            new EffectData(absorption, 20 * 30, 3),    // Absorption IV, 30s
            new EffectData(strength,   20 * 10, 2),    // Strength III, 10s
            new EffectData(regen,      20 *  5, 3)     // Regen IV, 5s
        ), 0, false, false);

        register("dirt_apple", List.of(
            new EffectData(hunger, 20 * 30, 2),        // Hunger III, 30s
            new EffectData(nausea, 20 * 10, 0)         // Nausea, 10s
        ), 0, false, false);

        // ── New apples ─────────────────────────────────────────────────────────

        // Totem Apple is intentionally NOT registered — its one-time death-cancellation
        // ability is too powerful to combine in a shake, and any effect approximation
        // would be confusing or overpowered.

        // Void Apple shake: triggers the massive upward launch + Slow Falling,
        // exactly as eating the apple directly when falling.
        registerVoidLaunch("void_apple", List.of(
            new EffectData(new ResourceLocation("minecraft", "slow_falling"), 20 * 15, 0)
        ));

        // Time Freeze Apple shake: shorter Time Freeze (10 s instead of 30 s).
        register("time_freeze_apple", List.of(
            new EffectData(mod("time_freeze"), 20 * 10, 0) // Time Freeze, 10s
        ), 0, false, false);

        // Quantum Apple is intentionally not added to the Mixer — its whole point
        // is unpredictability, which doesn't map to a deterministic shake contribution.

        // Longevity Apple: doubles the duration of ALL effects in the combined shake.
        // Its own base effects (Absorption IV + Health Boost I + Regen I) also appear.
        register("longevity_apple", List.of(
            new EffectData(absorption,   20 * 120, 3), // Absorption IV, 2 min
            new EffectData(healthBoost,  20 *  60, 0), // Health Boost I, 1 min
            new EffectData(regen,        20 *  15, 0)  // Regen I, 15s
        ), 0, false, false, 2.0);                      // ← 2× duration multiplier

        // Prism Apple shake: full ocean-speed package.
        ResourceLocation dolphinsGrace = mc("dolphins_grace");
        register("prism_apple", List.of(
            new EffectData(waterBreath,  20 * 300, 0), // Water Breathing, 5 min
            new EffectData(dolphinsGrace, 20 * 300, 0), // Dolphin's Grace, 5 min
            new EffectData(speed,        20 * 300, 1)  // Speed II, 5 min
        ), 0, false, false);

        // Quantum Apple: this registration exists only so the item can be placed in
        // the Mixer. The actual effects are RANDOMISED at mix-time by MixerBlockEntity —
        // the fixed effects here are never used in buildShakeNbt.
        register("quantum_apple", List.of(
            new EffectData(regen,      20 * 15, 1), // placeholder (overridden at build-time)
            new EffectData(absorption, 20 * 60, 1),
            new EffectData(strength,   20 * 15, 0)
        ), 0, false, false);

        // ── Special behaviour contributors ─────────────────────────────────────

        // Rewind Apple shake: teleports the drinker back 5 seconds using position history.
        registerRewind("rewind_apple");

        // Orchard Apple shake: plants up to 6 oak trees at the drink/impact location.
        registerOrchard("orchard_apple");

        // Ender Pearl Apple shake: also teleports the drinker/target in their look direction.
        // Speed II is kept as a secondary effect to make the shake useful on its own.
        registerEnderTeleport("ender_pearl_apple", List.of(
            new EffectData(speed, 20 * 15, 1)  // Speed II, 15s
        ));

        // Apple Bomb shake: makes the shake THROWABLE instead of drinkable.
        // The combined effects of the OTHER ingredient are applied to hit entities.
        registerBomb("apple_bomb");
    }

    // ── Incompatibility rules ──────────────────────────────────────────────

    private static final ResourceLocation ID_APPLE_BOMB        = mod("apple_bomb");
    private static final ResourceLocation ID_ENDER_PEARL_APPLE = mod("ender_pearl_apple");
    private static final ResourceLocation ID_REWIND_APPLE      = mod("rewind_apple");

    /**
     * Returns true when the two items form a forbidden combination and cannot be
     * mixed together. The check is symmetric (order does not matter).
     * <ul>
     *   <li>Apple Bomb + Ender Pearl Apple — a bomb that also teleports would be
     *       an exploit (instant escape + ranged teleport).</li>
     *   <li>Ender Pearl Apple + Rewind Apple — two opposing teleport effects that
     *       conflict with each other.</li>
     * </ul>
     */
    public static boolean areIncompatible(ItemStack a, ItemStack b) {
        if (a.isEmpty() || b.isEmpty()) return false;
        ResourceLocation idA = ForgeRegistries.ITEMS.getKey(a.getItem());
        ResourceLocation idB = ForgeRegistries.ITEMS.getKey(b.getItem());
        if (idA == null || idB == null) return false;
        return pair(idA, idB, ID_APPLE_BOMB,        ID_ENDER_PEARL_APPLE)
            || pair(idA, idB, ID_ENDER_PEARL_APPLE, ID_REWIND_APPLE);
    }

    /** Returns true if {a,b} is the symmetric pair {x,y}. */
    private static boolean pair(ResourceLocation a, ResourceLocation b,
                                 ResourceLocation x, ResourceLocation y) {
        return (a.equals(x) && b.equals(y)) || (a.equals(y) && b.equals(x));
    }

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Returns every registered ShakeContribution — used by QuantumAppleItem to pick
     * a random apple's effects when eaten.
     */
    public static java.util.List<ShakeContribution> getAllContributions() {
        return new java.util.ArrayList<>(REGISTRY.values());
    }

    public static Optional<ShakeContribution> getContribution(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) return Optional.empty();
        return Optional.ofNullable(REGISTRY.get(id));
    }

    public static Optional<ShakeContribution> getContribution(Item item) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null) return Optional.empty();
        return Optional.ofNullable(REGISTRY.get(id));
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    /** Register an item from this mod (ultimate_apple_mod:<name>). */
    private static void register(String itemName, List<EffectData> effects,
                                  int dragonCharges, boolean lifesteal, boolean witherCurse) {
        REGISTRY.put(mod(itemName),
            new ShakeContribution(effects, dragonCharges, lifesteal, witherCurse,
                false, 1.0, false, false, false, false, false));
    }

    /**
     * Register a mod item with a custom duration multiplier.
     * The Longevity Apple uses 2.0 — all other items call the 5-arg overload above (1.0).
     */
    private static void register(String itemName, List<EffectData> effects,
                                  int dragonCharges, boolean lifesteal, boolean witherCurse,
                                  double durationMultiplier) {
        REGISTRY.put(mod(itemName),
            new ShakeContribution(effects, dragonCharges, lifesteal, witherCurse,
                false, durationMultiplier, false, false, false, false, false));
    }

    /** Register a mod item whose shake cleanses all effects (e.g. honey_apple). */
    private static void registerCleansing(String itemName) {
        REGISTRY.put(mod(itemName),
            new ShakeContribution(List.of(), 0, false, false, true, 1.0, false, false, false, false, false));
    }

    /** Register a mod item that triggers the Void Apple launch mechanic. */
    private static void registerVoidLaunch(String itemName, List<EffectData> effects) {
        REGISTRY.put(mod(itemName),
            new ShakeContribution(effects, 0, false, false, false, 1.0, true, false, false, false, false));
    }

    /** Register a mod item whose shake rewinds the drinker 5 seconds back in time. */
    private static void registerRewind(String itemName) {
        REGISTRY.put(mod(itemName),
            new ShakeContribution(List.of(), 0, false, false, false, 1.0, false, true, false, false, false));
    }

    /** Register a mod item whose shake plants up to 6 trees at the drink/impact location. */
    private static void registerOrchard(String itemName) {
        REGISTRY.put(mod(itemName),
            new ShakeContribution(List.of(), 0, false, false, false, 1.0, false, false, true, false, false));
    }

    /** Register a mod item whose shake also teleports the entity in their look direction. */
    private static void registerEnderTeleport(String itemName, List<EffectData> effects) {
        REGISTRY.put(mod(itemName),
            new ShakeContribution(effects, 0, false, false, false, 1.0, false, false, false, true, false));
    }

    /** Register a mod item that turns the shake into a throwable bomb. */
    private static void registerBomb(String itemName) {
        REGISTRY.put(mod(itemName),
            new ShakeContribution(List.of(), 0, false, false, false, 1.0, false, false, false, false, true));
    }

    /** Register a vanilla Minecraft item (minecraft:<name>). */
    private static void registerVanilla(String itemName, List<EffectData> effects,
                                         int dragonCharges, boolean lifesteal, boolean witherCurse) {
        REGISTRY.put(mc(itemName),
            new ShakeContribution(effects, dragonCharges, lifesteal, witherCurse,
                false, 1.0, false, false, false, false, false));
    }

    /**
     * Returns all contributions that have regular mob effects (no isBomb).
     * Used by Quantum Apple to pick a random contribution at mix-time.
     */
    public static java.util.List<ShakeContribution> getRandomizableContributions() {
        return REGISTRY.values().stream()
            .filter(c -> !c.isBomb())
            .collect(java.util.stream.Collectors.toList());
    }

    private static ResourceLocation mc(String path)  { return new ResourceLocation("minecraft",         path); }
    private static ResourceLocation mod(String path) { return new ResourceLocation("ultimate_apple_mod", path); }

    private MixerRecipes() {}
}
