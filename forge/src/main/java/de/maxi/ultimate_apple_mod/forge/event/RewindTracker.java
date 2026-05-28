package de.maxi.ultimate_apple_mod.forge.event;

import de.maxi.ultimate_apple_mod.RewindPositionCache;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class RewindTracker {

    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (++tickCounter % 20 != 0) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        for (ServerLevel level : server.getAllLevels()) {
            RewindPositionCache.recordAll(level.players());
        }
    }
}
