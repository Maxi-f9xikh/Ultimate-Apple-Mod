package de.maxi.ultimate_apple_mod.forge.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {

    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
            FireDragonBreathPayload.TYPE,
            FireDragonBreathPayload.STREAM_CODEC,
            FireDragonBreathPayload::handle
        );
    }
}
