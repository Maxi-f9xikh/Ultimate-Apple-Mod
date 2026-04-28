package de.maxi.ultimate_apple_mod.event;

import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
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
        boolean isRottenActive = player.hasEffect(ultimate_apple_modForge.CURSE_OF_ROTTEN.get());
        if (isRottenActive != wasRottenActive) {
            player.refreshDimensions();
            wasRottenActive = isRottenActive;
        }
    }

    /**
     * END-phase mirror: after the client's updatePlayerPose() runs, force STANDING
     * so the crawl/swim animation is never shown on land while cursed.
     * Both client and server converge to STANDING every tick, so no rubber-banding occurs.
     */
    @SubscribeEvent
    public static void onClientTickEnd(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        try {
            if (player.hasEffect(ultimate_apple_modForge.CURSE_OF_ROTTEN.get())
                    && player.getPose() == Pose.SWIMMING
                    && !player.isInWater()) {
                player.setPose(Pose.STANDING);
            }
        } catch (NullPointerException ignored) {}
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (event.getEntity().hasEffect(ultimate_apple_modForge.CURSE_OF_ROTTEN.get())) {
            event.getPoseStack().scale(0.35f, 0.35f, 0.35f);
        }
    }
}
