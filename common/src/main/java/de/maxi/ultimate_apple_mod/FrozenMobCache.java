package de.maxi.ultimate_apple_mod;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;

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
 * tick handlers (Fabric / Forge) can restore AI for exactly the mobs that were
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

    /**
     * Restore AI for every mob frozen by {@code playerId} and clear the tracking
     * entry.  Mobs are looked up by UUID across all dimensions, so this works no
     * matter how far the player travelled — or whether they disconnected — since
     * freezing them.  Called on effect expiry (platform tick handlers) and on
     * player logout.
     */
    public static void releaseAll(MinecraftServer server, UUID playerId) {
        Set<UUID> mobs = byPlayer.remove(playerId);
        if (mobs == null || mobs.isEmpty()) return;
        frozenMobs.removeAll(mobs);
        for (UUID mobId : mobs) {
            for (ServerLevel level : server.getAllLevels()) {
                if (level.getEntity(mobId) instanceof Mob mob) {
                    mob.setNoAi(false);
                    break;
                }
            }
        }
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    public static boolean isFrozen(UUID mobId) {
        return frozenMobs.contains(mobId);
    }

    public static Set<UUID> getFrozenMobsForPlayer(UUID playerId) {
        return byPlayer.getOrDefault(playerId, Collections.emptySet());
    }

    public static boolean hasFrozenMobs(UUID playerId) {
        Set<UUID> s = byPlayer.get(playerId);
        return s != null && !s.isEmpty();
    }
}
