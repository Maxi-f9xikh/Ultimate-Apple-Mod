package de.maxi.ultimate_apple_mod.fabric;

import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.fabricmc.api.ModInitializer;

public final class ultimate_apple_modFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        ultimate_apple_mod.init();
    }
}
