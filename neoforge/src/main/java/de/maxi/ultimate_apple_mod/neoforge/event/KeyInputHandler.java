package de.maxi.ultimate_apple_mod.neoforge.event;

import de.maxi.ultimate_apple_mod.neoforge.ModClient;
import de.maxi.ultimate_apple_mod.neoforge.network.FireDragonBreathPacket;
import de.maxi.ultimate_apple_mod.neoforge.network.NetworkHandler;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.NEOFORGE, value = Dist.CLIENT)
public class KeyInputHandler {

    private static boolean prevFireBreathDown = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        long window = Minecraft.getInstance().getWindow().getWindow();
        boolean isFireDown = false;
        boolean isBoundToMouse = false;
        for (int btn = 0; btn <= 7; btn++) {
            if (ModClient.FIRE_DRAGON_BREATH_KEY.matchesMouse(btn)) {
                isFireDown = GLFW.glfwGetMouseButton(window, btn) == GLFW.GLFW_PRESS;
                isBoundToMouse = true;
                break;
            }
        }
        if (!isBoundToMouse) {
            isFireDown = ModClient.FIRE_DRAGON_BREATH_KEY.isDown();
        }
        if (isFireDown && !prevFireBreathDown) {
            boolean aimingAtEntity = mc.hitResult instanceof EntityHitResult;
            var mainHand = mc.player.getMainHandItem();
            boolean holdingMeleeWeapon = mainHand.getItem() instanceof SwordItem
                || mainHand.getItem() instanceof AxeItem;

            if (!(aimingAtEntity && holdingMeleeWeapon)) {
                NetworkHandler.CHANNEL.sendToServer(new FireDragonBreathPacket());
            }
        }
        prevFireBreathDown = isFireDown;
    }
}
