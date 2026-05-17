package de.maxi.ultimate_apple_mod.fabric.event;

public class FabricEventRegistrar {
    public static void register() {
        FabricMobDropHandler.register();
        FabricPlayerEffectHandler.register();
        FabricLootTableHandler.register();
        FabricDecayHandler.register();
        FabricTntAppleHandler.register();
        FabricBabyZombieHandler.register();
    }
}
