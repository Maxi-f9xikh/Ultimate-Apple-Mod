package de.maxi.ultimate_apple_mod.event;

import de.maxi.ultimate_apple_mod.forge.ModClient;
import de.maxi.ultimate_apple_mod.forge.network.FireDragonBreathPacket;
import de.maxi.ultimate_apple_mod.forge.network.NetworkHandler;
import de.maxi.ultimate_apple_mod.item.DragonAppleItem;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class KeyInputHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // consumeClick() drains all pending presses since last tick
        while (ModClient.FIRE_DRAGON_BREATH_KEY.consumeClick()) {
            if (mc.player.getMainHandItem().getItem() instanceof DragonAppleItem) {
                NetworkHandler.CHANNEL.sendToServer(new FireDragonBreathPacket());
            }
        }
    }
}
