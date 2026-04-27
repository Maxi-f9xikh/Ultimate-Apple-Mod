package de.maxi.ultimate_apple_mod.forge;

import de.maxi.ultimate_apple_mod.item.AppleBombEntity;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import static de.maxi.ultimate_apple_mod.ultimate_apple_mod.MOD_ID;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClient {

    /**
     * Configurable keybinding — default R.
     * Shows up in Options → Controls → "Ultimate Apple Mod".
     */
    public static final KeyMapping FIRE_DRAGON_BREATH_KEY = new KeyMapping(
        "key.ultimate_apple_mod.fire_dragon_breath",
        GLFW.GLFW_KEY_R,
        "key.categories.ultimate_apple_mod"
    );

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(FIRE_DRAGON_BREATH_KEY);
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> 0x00BBFF, ultimate_apple_modForge.REWIND_APPLE.get());
        event.register((stack, tintIndex) -> 0xFF6600, ultimate_apple_modForge.APPLE_BOMB.get());
        event.register((stack, tintIndex) -> 0xB87333, ultimate_apple_modForge.COPPER_APPLE.get());
        event.register((stack, tintIndex) -> 0x3E1F5C, ultimate_apple_modForge.WITHER_APPLE.get());
        event.register((stack, tintIndex) -> 0xFFCC00, ultimate_apple_modForge.GOLDEN_CARROT_APPLE.get());
        event.register((stack, tintIndex) -> 0xFFAA22, ultimate_apple_modForge.HONEY_APPLE.get());
        event.register((stack, tintIndex) -> 0xCC0000, ultimate_apple_modForge.DRAGON_APPLE.get());
        event.register((stack, tintIndex) -> 0xFFFFFF, ultimate_apple_modForge.NETHER_STAR_APPLE.get());
    }

    @SubscribeEvent
    public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ultimate_apple_modForge.APPLE_BOMB_ENTITY.get(),
            ThrownItemRenderer::new);
    }
}
