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
