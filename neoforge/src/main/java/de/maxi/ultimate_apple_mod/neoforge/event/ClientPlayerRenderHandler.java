package de.maxi.ultimate_apple_mod.neoforge.event;

import de.maxi.ultimate_apple_mod.neoforge.ultimate_apple_modNeoForge;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Pose;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.event.TickEvent;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.NEOFORGE, value = Dist.CLIENT)
public class ClientPlayerRenderHandler {

    private static boolean wasRottenActive = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            wasRottenActive = false;
            return;
        }
        boolean isRottenActive = player.hasEffect(ultimate_apple_modNeoForge.CURSE_OF_ROTTEN.get());
        if (isRottenActive != wasRottenActive) {
            player.refreshDimensions();
            wasRottenActive = isRottenActive;
        }
    }

    @SubscribeEvent
    public static void onClientTickEnd(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        try {
            if (player.hasEffect(ultimate_apple_modNeoForge.CURSE_OF_ROTTEN.get())
                    && player.getPose() == Pose.SWIMMING
                    && !player.isInWater()) {
                player.setPose(Pose.STANDING);
            }
        } catch (NullPointerException ignored) {}
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (event.getEntity().hasEffect(ultimate_apple_modNeoForge.CURSE_OF_ROTTEN.get())) {
            event.getPoseStack().scale(0.35f, 0.35f, 0.35f);
        }
    }
}
