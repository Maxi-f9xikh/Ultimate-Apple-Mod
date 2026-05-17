package de.maxi.ultimate_apple_mod.fabric;

import de.maxi.ultimate_apple_mod.ModRegistries;
import de.maxi.ultimate_apple_mod.block.MixerMenu;
import de.maxi.ultimate_apple_mod.block.MixerScreen;
import de.maxi.ultimate_apple_mod.fabric.event.FabricClientHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.inventory.MenuType;
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
            (MenuType<MixerMenu>) ModRegistries.MIXER_MENU_TYPE.get(),
            MixerScreen::new);

        // ── Client events ─────────────────────────────────────────────────────
        FabricClientHandler.register();
    }
}
