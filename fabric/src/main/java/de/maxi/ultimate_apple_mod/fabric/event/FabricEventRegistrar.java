package de.maxi.ultimate_apple_mod.fabric.event;

public class FabricEventRegistrar {
    public static void register() {
        FabricMobDropHandler.register();
        FabricPlayerEffectHandler.register();
        FabricLootTableHandler.register();
        FabricDecayHandler.register();
        FabricTntAppleHandler.register();
        // Baby zombie rotten-apple drop is handled by the loot-table pool in
        // FabricLootTableHandler — a separate death-event handler would double it.
    }
}
