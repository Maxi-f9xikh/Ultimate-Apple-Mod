package de.maxi.ultimate_apple_mod;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Platform-neutral store for tracking which mobs are currently frozen by TimeFreezeEffect.
 *
 * <p>Mobs are stored per caster UUID so that when the effect expires the platform
 * tick handlers (Fabric / NeoForge) can restore AI for exactly the mobs that were
 * frozen by a specific player.
 */
public class FrozenMobCache {

    /** Fast "is this mob currently frozen?" lookup. */
    private static final Set<UUID> frozenMobs =
            Collections.synchronizedSet(new HashSet<>());

    /** player UUID → set of mob UUIDs frozen by that player. */
    private static final Map<UUID, Set<UUID>> byPlayer = new ConcurrentHashMap<>();

    // ── Write ────────────────────────────────────────────────────────────────

    /**
     * Mark {@code mobId} as frozen by {@code playerId}.
     * Called every tick from {@link de.maxi.ultimate_apple_mod.effect.TimeFreezeEffect}.
     */
    public static void freeze(UUID playerId, UUID mobId) {
        frozenMobs.add(mobId);
        byPlayer.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet()).add(mobId);
    }

    /**
     * Remove a single mob from the frozen set and from its owner's player entry.
     * Rarely called directly — prefer {@link #clearPlayer(UUID)} for bulk cleanup.
     */
    public static void unfreeze(UUID mobId) {
        frozenMobs.remove(mobId);
        byPlayer.values().forEach(set -> set.remove(mobId));
    }

    /**
     * Remove all mobs frozen by {@code playerId} from the frozen set and clear
     * the player entry.  Called by the platform tick handler when the TIME_FREEZE
     * effect expires on a player.
     */
    public static void clearPlayer(UUID playerId) {
        Set<UUID> mobs = byPlayer.remove(playerId);
        if (mobs != null) {
            frozenMobs.removeAll(mobs);
        }
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    public static boolean isFrozen(UUID mobId) {
        return frozenMobs.contains(mobId);
    }

    /**
     * Returns the set of mob UUIDs currently frozen by {@code playerId}, or an
     * empty set if the player has no frozen mobs.  The returned set is a live
     * view — copy it before iterating if you intend to call {@link #clearPlayer}.
     */
    public static Set<UUID> getFrozenMobsForPlayer(UUID playerId) {
        return byPlayer.getOrDefault(playerId, Collections.emptySet());
    }

    public static boolean hasFrozenMobs(UUID playerId) {
        Set<UUID> s = byPlayer.get(playerId);
        return s != null && !s.isEmpty();
    }
}
