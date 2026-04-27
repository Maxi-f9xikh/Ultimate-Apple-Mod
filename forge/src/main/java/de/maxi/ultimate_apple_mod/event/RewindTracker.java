package de.maxi.ultimate_apple_mod.event;

import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RewindTracker {

    private static final Map<UUID, ArrayDeque<Vec3>> positionHistory = new HashMap<>();
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (++tickCounter % 20 != 0) return; // once per second

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        for (ServerLevel level : server.getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                ArrayDeque<Vec3> history = positionHistory.computeIfAbsent(
                    player.getUUID(), k -> new ArrayDeque<>());
                history.addLast(player.position());
                while (history.size() > 5) history.removeFirst();
            }
        }
    }

    /**
     * Returns the position the player was at ~5 seconds ago.
     * Returns null if no history is available yet.
     */
    public static Vec3 getPositionFiveSecondsAgo(Player player) {
        ArrayDeque<Vec3> history = positionHistory.get(player.getUUID());
        if (history == null || history.isEmpty()) return null;
        return history.peekFirst(); // oldest entry = ~5 seconds ago
    }
}
