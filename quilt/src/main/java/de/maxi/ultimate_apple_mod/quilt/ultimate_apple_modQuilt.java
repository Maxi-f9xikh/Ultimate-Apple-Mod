package de.maxi.ultimate_apple_mod.quilt;

import de.maxi.ultimate_apple_mod.fabric.ultimate_apple_modFabric;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class ultimate_apple_modQuilt implements ModInitializer {

    @Override
    public void onInitialize(ModContainer container) {
        new ultimate_apple_modFabric().onInitialize();
    }
}
