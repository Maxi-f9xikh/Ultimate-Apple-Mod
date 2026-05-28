package de.maxi.ultimate_apple_mod.fabric.network;

import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * C2S payload for the Fire Dragon Breath keybind on Fabric.
 * Carries no data — the server reads charges from DragonChargesCache.
 */
public record FabricFireDragonBreathPayload() implements CustomPacketPayload {

    public static final Type<FabricFireDragonBreathPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(ultimate_apple_mod.MOD_ID, "fire_dragon_breath")
    );

    public static final StreamCodec<FriendlyByteBuf, FabricFireDragonBreathPayload> CODEC =
        StreamCodec.unit(new FabricFireDragonBreathPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
