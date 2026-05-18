package de.maxi.ultimate_apple_mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Platform-neutral store for per-player echo apple position data.
 * Replaces Forge's getPersistentData() in common code.
 * Note: not persisted across server restarts (acceptable for this mod).
 */
public class EchoPositionCache {

    public static final class SavedPosition {
        public final double x, y, z;
        public final String dimension;

        public SavedPosition(double x, double y, double z, String dimension) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.dimension = dimension;
        }
    }

    private static final Map<UUID, SavedPosition> positions = new HashMap<>();

    public static boolean hasPosition(UUID playerId) {
        return positions.containsKey(playerId);
    }

    public static SavedPosition getPosition(UUID playerId) {
        return positions.get(playerId);
    }

    public static void savePosition(UUID playerId, double x, double y, double z, String dimension) {
        positions.put(playerId, new SavedPosition(x, y, z, dimension));
    }

    public static void clearPosition(UUID playerId) {
        positions.remove(playerId);
    }
}
