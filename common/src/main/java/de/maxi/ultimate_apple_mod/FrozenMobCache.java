package de.maxi.ultimate_apple_mod;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Platform-neutral store for tracking which mobs are currently frozen by TimeFreezeEffect.
 * Replaces Forge's getPersistentData() in common code.
 */
public class FrozenMobCache {

    private static final Set<UUID> frozenMobs = Collections.synchronizedSet(new HashSet<>());

    public static void freeze(UUID mobId) {
        frozenMobs.add(mobId);
    }

    public static boolean isFrozen(UUID mobId) {
        return frozenMobs.contains(mobId);
    }

    public static void unfreeze(UUID mobId) {
        frozenMobs.remove(mobId);
    }
}
