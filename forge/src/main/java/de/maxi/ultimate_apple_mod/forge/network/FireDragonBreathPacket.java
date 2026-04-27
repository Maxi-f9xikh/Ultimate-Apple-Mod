package de.maxi.ultimate_apple_mod.forge.network;

import de.maxi.ultimate_apple_mod.item.DragonAppleItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent client → server when the player presses the "Fire Dragon Breath" keybind
 * while holding a Dragon Apple with at least 1 charge.
 */
public class FireDragonBreathPacket {

    private static final String CHARGES_KEY = "dragonBreathCharges";

    public FireDragonBreathPacket() {}

    public static void encode(FireDragonBreathPacket msg, FriendlyByteBuf buf) {
        // no payload
    }

    public static FireDragonBreathPacket decode(FriendlyByteBuf buf) {
        return new FireDragonBreathPacket();
    }

    public static void handle(FireDragonBreathPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // Safety: must actually be holding a Dragon Apple
            if (!(player.getMainHandItem().getItem() instanceof DragonAppleItem)) return;

            int charges = player.getPersistentData().getInt(CHARGES_KEY);
            if (charges <= 0) return;

            // Fire the dragon fireball in the direction the player is looking
            Vec3 look = player.getLookAngle();
            DragonFireball fireball = new DragonFireball(player.level(), player,
                look.x, look.y, look.z);
            fireball.setPos(
                player.getX() + look.x * 1.5,
                player.getEyeY() - 0.1,
                player.getZ() + look.z * 1.5
            );
            player.level().addFreshEntity(fireball);

            int remaining = charges - 1;
            player.getPersistentData().putInt(CHARGES_KEY, remaining);
            player.displayClientMessage(
                Component.translatable("message.ultimate_apple_mod.dragon_breath_remaining", remaining),
                true);
        });
        ctx.get().setPacketHandled(true);
    }
}
