package de.maxi.ultimate_apple_mod.forge.compat;

import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IIngredientInfoRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * JEI integration for Ultimate Apple Mod.
 *
 * <p>Registers ingredient info (description pages) for every apple that has
 * a custom or otherwise non-obvious effect.  These pages are shown in JEI when
 * the player presses "?" / "R" on an item.
 *
 * <p>The class is discovered automatically at runtime thanks to the
 * {@link JeiPlugin} annotation — no explicit registration is needed.
 */
@JeiPlugin
public class JeiIntegrationPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_UID =
        new ResourceLocation("ultimate_apple_mod", "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    // ── Ingredient info (description pages) ─────────────────────────────────

    @Override
    public void registerIngredientInfo(IIngredientInfoRegistration reg) {

        // ── Totem Apple ──────────────────────────────────────────────────────
        reg.addIngredientInfo(
            List.of(new ItemStack(ultimate_apple_modForge.TOTEM_APPLE.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.ultimate_apple_mod.totem_apple.info1"),
            Component.translatable("jei.ultimate_apple_mod.totem_apple.info2")
        );

        // ── Wither Apple (Lifesteal) ─────────────────────────────────────────
        reg.addIngredientInfo(
            List.of(new ItemStack(ultimate_apple_modForge.WITHER_APPLE.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.ultimate_apple_mod.wither_apple.info1"),
            Component.translatable("jei.ultimate_apple_mod.wither_apple.info2")
        );

        // ── Moon Apple ───────────────────────────────────────────────────────
        reg.addIngredientInfo(
            List.of(new ItemStack(ultimate_apple_modForge.MOON_APPLE.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.ultimate_apple_mod.moon_apple.info1"),
            Component.translatable("jei.ultimate_apple_mod.moon_apple.info2")
        );

        // ── Time Freeze Apple ────────────────────────────────────────────────
        reg.addIngredientInfo(
            List.of(new ItemStack(ultimate_apple_modForge.TIME_FREEZE_APPLE.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.ultimate_apple_mod.time_freeze_apple.info1"),
            Component.translatable("jei.ultimate_apple_mod.time_freeze_apple.info2")
        );

        // ── Rotten Apple (Curse of Rotten) ───────────────────────────────────
        reg.addIngredientInfo(
            List.of(new ItemStack(ultimate_apple_modForge.ROTTEN_APPLE.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.ultimate_apple_mod.rotten_apple.info1"),
            Component.translatable("jei.ultimate_apple_mod.rotten_apple.info2")
        );

        // ── Quantum Apple (all effects) ──────────────────────────────────────
        reg.addIngredientInfo(
            List.of(new ItemStack(ultimate_apple_modForge.QUANTUM_APPLE.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.ultimate_apple_mod.quantum_apple.info1"),
            Component.translatable("jei.ultimate_apple_mod.quantum_apple.info2"),
            Component.translatable("jei.ultimate_apple_mod.quantum_apple.info3")
        );

        // ── Blaze Apple — mob drop source ────────────────────────────────────
        reg.addIngredientInfo(
            List.of(new ItemStack(ultimate_apple_modForge.BLAZE_APPLE.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.ultimate_apple_mod.blaze_apple.info")
        );

        // ── Ender Pearl Apple — mob drop source ──────────────────────────────
        reg.addIngredientInfo(
            List.of(new ItemStack(ultimate_apple_modForge.ENDER_PEARL_APPLE.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.ultimate_apple_mod.ender_pearl_apple.info")
        );

        // ── Copper Apple oxidation chain ─────────────────────────────────────
        reg.addIngredientInfo(
            List.of(
                new ItemStack(ultimate_apple_modForge.COPPER_APPLE.get()),
                new ItemStack(ultimate_apple_modForge.EXPOSED_COPPER_APPLE.get()),
                new ItemStack(ultimate_apple_modForge.WEATHERED_COPPER_APPLE.get()),
                new ItemStack(ultimate_apple_modForge.OXIDIZED_COPPER_APPLE.get())
            ),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.ultimate_apple_mod.copper_apple.info1"),
            Component.translatable("jei.ultimate_apple_mod.copper_apple.info2"),
            Component.translatable("jei.ultimate_apple_mod.copper_apple.info3")
        );

        // ── Void Apple ───────────────────────────────────────────────────────
        reg.addIngredientInfo(
            List.of(new ItemStack(ultimate_apple_modForge.VOID_APPLE.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.ultimate_apple_mod.void_apple.info")
        );

        // ── Prism Apple ──────────────────────────────────────────────────────
        reg.addIngredientInfo(
            List.of(new ItemStack(ultimate_apple_modForge.PRISM_APPLE.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.ultimate_apple_mod.prism_apple.info")
        );

        // ── Nether Star Apple ────────────────────────────────────────────────
        reg.addIngredientInfo(
            List.of(new ItemStack(ultimate_apple_modForge.NETHER_STAR_APPLE.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.ultimate_apple_mod.nether_star_apple.info")
        );

        // ── Echo Apple ───────────────────────────────────────────────────────
        reg.addIngredientInfo(
            List.of(new ItemStack(ultimate_apple_modForge.ECHO_APPLE.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.ultimate_apple_mod.echo_apple.info")
        );

        // ── Rewind Apple ─────────────────────────────────────────────────────
        reg.addIngredientInfo(
            List.of(new ItemStack(ultimate_apple_modForge.REWIND_APPLE.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.ultimate_apple_mod.rewind_apple.info")
        );
    }
}
