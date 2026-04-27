package de.maxi.ultimate_apple_mod.forge.network;

import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class NetworkHandler {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(ultimate_apple_mod.MOD_ID, "main"),
        () -> PROTOCOL,
        PROTOCOL::equals,
        PROTOCOL::equals
    );

    public static void register() {
        CHANNEL.registerMessage(
            0,
            FireDragonBreathPacket.class,
            FireDragonBreathPacket::encode,
            FireDragonBreathPacket::decode,
            FireDragonBreathPacket::handle,
            Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }
}
