package de.maxi.ultimate_apple_mod.forge.event;

import de.maxi.ultimate_apple_mod.forge.ModClient;
import de.maxi.ultimate_apple_mod.forge.network.FireDragonBreathPacket;
import de.maxi.ultimate_apple_mod.forge.network.NetworkHandler;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class KeyInputHandler {

    /** Tracks the previous tick's key-down state for left-click edge detection. */
    private static boolean prevFireBreathDown = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // GLFW direct polling — bypasses KeyMapping.MAP entirely.
        // Both consumeClick() and isDown() are routed through MAP.get(key) and only
        // update the single "winning" keybind for a given physical key.  Left-click
        // is shared between vanilla key.attack and our keybind; one of them is always
        // starved.  Reading GLFW state directly is immune to this MAP conflict and
        // works for any user-rebinding (keyboard or mouse).
        // For mouse-button bindings: read GLFW state directly to bypass
        // KeyMapping.MAP (both consumeClick and isDown only update the single
        // MAP-winner for a given physical button; left-click conflicts with vanilla
        // key.attack). matchesMouse(btn) checks the CURRENT user binding, so
        // rebinding to a different mouse button is also handled correctly.
        // Keyboard bindings have no MAP conflict → isDown() works fine there.
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
            // Rising edge: key was just pressed this tick
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
