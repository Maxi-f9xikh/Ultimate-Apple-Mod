package de.maxi.ultimate_apple_mod.forge.network;

import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

public class NetworkHandler {

    public static final SimpleChannel CHANNEL = ChannelBuilder
        .named(new ResourceLocation(ultimate_apple_mod.MOD_ID, "main"))
        .networkProtocolVersion(1)
        .simpleChannel();

    public static void register() {
        CHANNEL.messageBuilder(FireDragonBreathPacket.class, NetworkDirection.PLAY_TO_SERVER)
            .encoder(FireDragonBreathPacket::encode)
            .decoder(FireDragonBreathPacket::decode)
            .consumerNetworkThread(FireDragonBreathPacket::handle)
            .add();
    }
}
