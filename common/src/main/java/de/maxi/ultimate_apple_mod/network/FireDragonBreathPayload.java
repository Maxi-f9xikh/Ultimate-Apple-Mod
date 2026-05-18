package de.maxi.ultimate_apple_mod.network;

import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.resources.ResourceLocation;

/**
 * C2S packet: sent by the client when the player presses the Fire Dragon Breath keybind.
 * Carries no data — the server reads charges from DragonChargesCache.
 */
public final class FireDragonBreathPayload {

    public static final ResourceLocation CHANNEL =
        new ResourceLocation(ultimate_apple_mod.MOD_ID, "fire_dragon_breath");

    private FireDragonBreathPayload() {}
}
