package de.maxi.ultimate_apple_mod.forge.network;

import de.maxi.ultimate_apple_mod.DragonChargesCache;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * C2S packet: sent by the client when the player presses the Fire Dragon Breath keybind.
 * Carries no data — the server reads charges from DragonChargesCache.
 */
public record FireDragonBreathPayload() implements CustomPacketPayload {

    public static final Type<FireDragonBreathPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(ultimate_apple_mod.MOD_ID, "fire_dragon_breath")
    );

    public static final StreamCodec<FriendlyByteBuf, FireDragonBreathPayload> STREAM_CODEC =
        StreamCodec.unit(new FireDragonBreathPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FireDragonBreathPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            int charges = DragonChargesCache.getCharges(player.getUUID());
            if (charges <= 0) return;

            // Fire the dragon fireball in the direction the player is looking
            Vec3 look = player.getLookAngle();
            DragonFireball fireball = new DragonFireball(player.level(), player, look);
            fireball.setPos(
                player.getX() + look.x * 1.5,
                player.getEyeY() - 0.1,
                player.getZ() + look.z * 1.5
            );
            player.level().addFreshEntity(fireball);

            int remaining = charges - 1;
            DragonChargesCache.setCharges(player.getUUID(), remaining);
            player.displayClientMessage(
                Component.translatable("message.ultimate_apple_mod.dragon_breath_remaining", remaining),
                true);
        });
    }
}
