package de.maxi.ultimate_apple_mod.forge;

import de.maxi.ultimate_apple_mod.block.MixerScreen;
import de.maxi.ultimate_apple_mod.item.AppleBombEntity;
import de.maxi.ultimate_apple_mod.item.NuclearAppleEntity;
import de.maxi.ultimate_apple_mod.item.ShakeBombEntity;
import de.maxi.ultimate_apple_mod.item.TntAppleEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import de.maxi.ultimate_apple_mod.forge.block.ModBlocks;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import static de.maxi.ultimate_apple_mod.ultimate_apple_mod.MOD_ID;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

@EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClient {

    /**
     * Configurable keybinding — default R.
     * Shows up in Options → Controls → "Ultimate Apple Mod".
     */
    /**
     * Default: Left Mouse Button. Fires a dragon fireball if charges > 0.
     * Suppressed client-side when aiming at an entity with a melee weapon (normal attack takes priority).
     * Rebindable in Options → Controls → "Ultimate Apple Mod".
     */
    public static final KeyMapping FIRE_DRAGON_BREATH_KEY = new KeyMapping(
        "key.ultimate_apple_mod.fire_dragon_breath",
        InputConstants.Type.MOUSE,
        GLFW.GLFW_MOUSE_BUTTON_LEFT,   // = 0  (left click)
        "key.categories.ultimate_apple_mod"
    );

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(FIRE_DRAGON_BREATH_KEY);
    }

    @SubscribeEvent
    public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ultimate_apple_modForge.APPLE_BOMB_ENTITY.get(),
            ThrownItemRenderer::new);
        event.registerEntityRenderer(ultimate_apple_modForge.SHAKE_BOMB_ENTITY.get(),
            ThrownItemRenderer::new);
        event.registerEntityRenderer(ultimate_apple_modForge.TNT_APPLE_ENTITY.get(),
            ThrownItemRenderer::new);
        event.registerEntityRenderer(ultimate_apple_modForge.NUCLEAR_APPLE_ENTITY.get(),
            ThrownItemRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ultimate_apple_modForge.MIXER_MENU_TYPE.get(), MixerScreen::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Mixer uses custom transparent glass textures → needs cutout render type
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.MIXER.get(), RenderType.cutoutMipped());
        });
    }
}
