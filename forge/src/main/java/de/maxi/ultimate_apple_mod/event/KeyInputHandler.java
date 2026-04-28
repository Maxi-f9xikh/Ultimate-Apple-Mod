package de.maxi.ultimate_apple_mod.event;

import de.maxi.ultimate_apple_mod.forge.ModClient;
import de.maxi.ultimate_apple_mod.forge.network.FireDragonBreathPacket;
import de.maxi.ultimate_apple_mod.forge.network.NetworkHandler;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.EntityHitResult;
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

        while (ModClient.FIRE_DRAGON_BREATH_KEY.consumeClick()) {
            // Suppress fireball if the player is looking directly at an entity
            // AND is holding a melee weapon — that means they're trying to attack, not fire.
            boolean aimingAtEntity = mc.hitResult instanceof EntityHitResult;
            var mainHand = mc.player.getMainHandItem();
            boolean holdingMeleeWeapon = mainHand.getItem() instanceof SwordItem
                || mainHand.getItem() instanceof AxeItem;

            if (aimingAtEntity && holdingMeleeWeapon) continue;

            NetworkHandler.CHANNEL.sendToServer(new FireDragonBreathPacket());
        }
    }
}
