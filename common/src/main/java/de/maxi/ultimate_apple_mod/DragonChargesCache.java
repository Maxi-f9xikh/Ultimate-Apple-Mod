package de.maxi.ultimate_apple_mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Platform-neutral store for per-player dragon breath charges.
 * Replaces Forge's getPersistentData() in common code.
 * Note: not persisted across server restarts (acceptable for this mod).
 */
public class DragonChargesCache {

    private static final Map<UUID, Integer> charges = new HashMap<>();

    public static int getCharges(UUID playerId) {
        return charges.getOrDefault(playerId, 0);
    }

    public static void addCharges(UUID playerId, int amount) {
        charges.merge(playerId, amount, Integer::sum);
    }

    public static void setCharges(UUID playerId, int amount) {
        if (amount <= 0) charges.remove(playerId);
        else charges.put(playerId, amount);
    }

    public static void clearOnDisconnect(UUID playerId) {
        charges.remove(playerId);
    }
}
