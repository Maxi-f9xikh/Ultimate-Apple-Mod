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
