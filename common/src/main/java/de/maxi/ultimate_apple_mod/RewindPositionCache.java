// common/src/main/java/de/maxi/ultimate_apple_mod/RewindPositionCache.java
package de.maxi.ultimate_apple_mod;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Platform-neutral position history store.
 * Each platform ticks this via its own server-tick event.
 * ShakeBombEntity reads it via getPositionTenSecondsAgo().
 *
 * Positions are only valid within one dimension: the history is cleared when
 * the player changes dimension, and lookups in a different dimension return
 * null.  Otherwise rewinding right after entering a nether portal would
 * teleport the player to OVERWORLD coordinates inside the nether — potentially
 * into bedrock or the void.
 */
public class RewindPositionCache {

    private static final Map<UUID, ArrayDeque<Vec3>> history = new ConcurrentHashMap<>();

    /** Dimension the player's current history belongs to. */
    private static final Map<UUID, String> historyDimension = new ConcurrentHashMap<>();

    /** Called once per second by each platform's server-tick handler. */
    public static void recordAll(Iterable<? extends Player> players) {
        for (Player player : players) {
            UUID id = player.getUUID();
            String dim = player.level().dimension().location().toString();
            ArrayDeque<Vec3> q = history.computeIfAbsent(id, k -> new ArrayDeque<>());
            // Dimension changed — the old coordinates are meaningless here
            if (!dim.equals(historyDimension.put(id, dim))) {
                q.clear();
            }
            q.addLast(player.position());
            while (q.size() > 10) q.removeFirst();
        }
    }

    /**
     * Returns the position ~10 seconds ago, or null if there is no history yet
     * or the history was recorded in a different dimension.
     */
    public static Vec3 getPositionTenSecondsAgo(Player player) {
        String dim = player.level().dimension().location().toString();
        if (!dim.equals(historyDimension.get(player.getUUID()))) return null;
        ArrayDeque<Vec3> q = history.get(player.getUUID());
        if (q == null || q.isEmpty()) return null;
        return q.peekFirst();
    }

    /** Drops a player's history.  Called when the player disconnects. */
    public static void clearPlayer(UUID playerId) {
        history.remove(playerId);
        historyDimension.remove(playerId);
    }
}
