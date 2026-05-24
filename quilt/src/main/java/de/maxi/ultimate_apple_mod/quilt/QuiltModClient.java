package de.maxi.ultimate_apple_mod.quilt;

import de.maxi.ultimate_apple_mod.fabric.FabricModClient;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class QuiltModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient(ModContainer container) {
        new FabricModClient().onInitializeClient();
    }
}
