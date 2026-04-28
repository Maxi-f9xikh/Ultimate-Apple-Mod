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
        double durationMultiplier
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
            new EffectData(healthBoost, 20 * 30, 2),   // Health Boost III, 30s
            new EffectData(regen,       20 * 10, 1),   // Regen II, 10s
            new EffectData(resistance,  20 * 10, 1)    // Resistance II, 10s
        ), 0, false, false);

        register("lapislazuli_apple", List.of(
            new EffectData(luck, 20 * 30, 1)           // Luck II, 30s
        ), 0, false, false);

        register("emerald_apple", List.of(
            new EffectData(luck,        20 * 60, 1),   // Luck II, 60s
            new EffectData(nightVision, 20 * 30, 0)    // Night Vision, 30s
        ), 0, false, false);

        register("redstone_apple", List.of(
            new EffectData(speed, 20 * 20, 1),         // Speed II, 20s
            new EffectData(haste, 20 * 20, 0)          // Haste, 20s
        ), 0, false, false);

        register("netherite_apple", List.of(
            new EffectData(fireRes,    20 * 60, 0),    // Fire Resistance, 60s
            new EffectData(resistance, 20 * 30, 1),    // Resistance II, 30s
            new EffectData(strength,   20 * 15, 0)     // Strength, 15s
        ), 0, false, false);

        register("iron_apple", List.of(
            new EffectData(healthBoost, 20 * 10, 0),   // Health Boost, 10s
            new EffectData(regen,       20 *  3, 0)    // Regen, 3s
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

        register("blazing_apple_stew", List.of(
            new EffectData(regen,    20 * 10, 1),      // Regen II, 10s
            new EffectData(strength, 20 * 10, 1),      // Strength II, 10s
            new EffectData(fireRes,  20 * 30, 0)       // Fire Resistance, 30s
        ), 0, false, false);

        register("pear_apple", List.of(
            new EffectData(regen,      20 * 15, 0),    // Regen, 15s
            new EffectData(saturation, 20 *  5, 0)     // Saturation, 5s
        ), 0, false, false);

        register("copper_apple", List.of(
            new EffectData(haste,       20 * 20, 1),   // Haste II, 20s
            new EffectData(waterBreath, 20 * 30, 0)    // Water Breathing, 30s
        ), 0, false, false);

        register("ender_pearl_apple", List.of(
            new EffectData(speed, 20 * 15, 1)          // Speed II, 15s
        ), 0, false, false);

        register("moon_apple", List.of(
            new EffectData(jump, 200, 2)               // Jump Boost III, 10s
        ), 0, false, false);

        // echo_apple and rewind_apple intentionally excluded —
        // their core abilities (teleportation) cannot be meaningfully stored in a shake.

        register("apple_bomb", List.of(
            new EffectData(strength, 20 * 10, 1)       // Strength II, 10s
        ), 0, false, false);

        register("wither_apple", List.of(
            new EffectData(absorption,  20 * 30, 2),   // Absorption IV, 30s
            new EffectData(resistance,  20 * 10, 1),   // Resistance II, 10s
            new EffectData(regen,       20 *  5, 1)    // Regen II, 5s
        ), 0, true, true);                             // + lifesteal + witherCurse

        register("golden_carrot_apple", List.of(
            new EffectData(nightVision, 20 * 600, 0),  // Night Vision, 600s
            new EffectData(saturation,  20 *  30, 1)   // Saturation II, 30s
        ), 0, false, false);

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

        // Longevity Apple: doubles the duration of ALL effects in the combined shake.
        // Its own base effects (Absorption IV + Health Boost I + Regen I) also appear.
        register("longevity_apple", List.of(
            new EffectData(absorption,   20 * 120, 3), // Absorption IV, 2 min
            new EffectData(healthBoost,  20 *  60, 0), // Health Boost I, 1 min
            new EffectData(regen,        20 *  15, 0)  // Regen I, 15s
        ), 0, false, false, 2.0);                      // ← 2× duration multiplier
    }

    // ── Public API ─────────────────────────────────────────────────────────

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
            new ShakeContribution(effects, dragonCharges, lifesteal, witherCurse, false, 1.0));
    }

    /**
     * Register a mod item with a custom duration multiplier.
     * The Longevity Apple uses 2.0 — all other items call the 5-arg overload above (1.0).
     */
    private static void register(String itemName, List<EffectData> effects,
                                  int dragonCharges, boolean lifesteal, boolean witherCurse,
                                  double durationMultiplier) {
        REGISTRY.put(mod(itemName),
            new ShakeContribution(effects, dragonCharges, lifesteal, witherCurse, false, durationMultiplier));
    }

    /** Register a mod item whose shake cleanses all effects (e.g. honey_apple). */
    private static void registerCleansing(String itemName) {
        REGISTRY.put(mod(itemName),
            new ShakeContribution(List.of(), 0, false, false, true, 1.0));
    }

    /** Register a vanilla Minecraft item (minecraft:<name>). */
    private static void registerVanilla(String itemName, List<EffectData> effects,
                                         int dragonCharges, boolean lifesteal, boolean witherCurse) {
        REGISTRY.put(mc(itemName),
            new ShakeContribution(effects, dragonCharges, lifesteal, witherCurse, false, 1.0));
    }

    private static ResourceLocation mc(String path)  { return new ResourceLocation("minecraft",         path); }
    private static ResourceLocation mod(String path) { return new ResourceLocation("ultimate_apple_mod", path); }

    private MixerRecipes() {}
}
