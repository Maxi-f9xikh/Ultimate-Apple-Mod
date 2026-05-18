package de.maxi.ultimate_apple_mod.fabric.network;

import de.maxi.ultimate_apple_mod.DragonChargesCache;
import de.maxi.ultimate_apple_mod.network.FireDragonBreathPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.phys.Vec3;

public class FabricNetworkHandler {

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(
            FireDragonBreathPayload.CHANNEL,
            (server, player, handler, buf, responseSender) -> {
                server.execute(() -> {
                    int charges = DragonChargesCache.getCharges(player.getUUID());
                    if (charges <= 0) return;

                    Vec3 look = player.getLookAngle();
                    DragonFireball fireball = new DragonFireball(
                        player.level(), player, look.x, look.y, look.z);
                    fireball.setPos(
                        player.getX() + look.x * 1.5,
                        player.getEyeY() - 0.1,
                        player.getZ() + look.z * 1.5);
                    player.level().addFreshEntity(fireball);

                    int remaining = charges - 1;
                    DragonChargesCache.setCharges(player.getUUID(), remaining);
                    player.displayClientMessage(
                        Component.translatable(
                            "message.ultimate_apple_mod.dragon_breath_remaining", remaining), true);
                });
            });
    }
}
