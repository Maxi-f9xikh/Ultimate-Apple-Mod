// common/src/main/java/de/maxi/ultimate_apple_mod/RewindPositionCache.java
package de.maxi.ultimate_apple_mod;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Platform-neutral position history store.
 * Each platform ticks this via its own server-tick event.
 * ShakeBombEntity reads it via getPositionFiveSecondsAgo().
 */
public class RewindPositionCache {

    private static final Map<UUID, ArrayDeque<Vec3>> history = new HashMap<>();

    /** Called once per second by each platform's server-tick handler. */
    public static void recordAll(Iterable<? extends Player> players) {
        for (Player player : players) {
            ArrayDeque<Vec3> q = history.computeIfAbsent(player.getUUID(), k -> new ArrayDeque<>());
            q.addLast(player.position());
            while (q.size() > 5) q.removeFirst();
        }
    }

    /** Returns the position ~5 seconds ago, or null if no history yet. */
    public static Vec3 getPositionFiveSecondsAgo(Player player) {
        ArrayDeque<Vec3> q = history.get(player.getUUID());
        if (q == null || q.isEmpty()) return null;
        return q.peekFirst();
    }
}
