package de.maxi.ultimate_apple_mod.forge.event;

import de.maxi.ultimate_apple_mod.FrozenMobCache;
import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEffectEventHandler {

    /**
     * Tracks whether Curse of Rotten was active last tick for each server-side player.
     * Used to trigger refreshDimensions() exactly when the state changes, mirroring
     * what ClientPlayerRenderHandler does for the client player.
     */
    private static final WeakHashMap<Player, Boolean> serverRottenState = new WeakHashMap<>();

    /**
     * Server-side mirror of ClientPlayerRenderHandler.onClientTick.
     * Calls refreshDimensions() on the server player entity whenever Curse of Rotten
     * is applied or removed, so the server bounding box actually shrinks to 0.25×0.6
     * and the player can walk (not crawl) through 1-block gaps.
     */
    @SubscribeEvent
    public static void onServerPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Player player = event.player;
        // Server side only
        if (!(player.level() instanceof ServerLevel)) return;

        boolean hasEffect;
        try {
            hasEffect = player.hasEffect(ultimate_apple_modForge.CURSE_OF_ROTTEN.get());
        } catch (NullPointerException ignored) {
            return;
        }

        Boolean prev = serverRottenState.get(player);
        if (prev == null || prev != hasEffect) {
            serverRottenState.put(player, hasEffect);
            player.refreshDimensions();
        }
    }

    /**
     * After updatePlayerPose() runs (END phase), force the cursed player back to STANDING
     * when they are on land so they walk through 1-block gaps normally instead of crawling.
     * The entity dimensions are already 0.25×0.6, so the physics allow it — the only
     * issue is that updatePlayerPose() checks static 1.8-tall standing dimensions for
     * pose selection and forces SWIMMING (crawl) for any gap < 1.8 blocks.
     */
    @SubscribeEvent
    public static void onServerPlayerTickEnd(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (!(player.level() instanceof ServerLevel)) return;

        // ── Rotten Apple: fix swimming pose ──────────────────────────────────
        try {
            if (player.hasEffect(ultimate_apple_modForge.CURSE_OF_ROTTEN.get())
                    && player.getPose() == Pose.SWIMMING
                    && !player.isInWater()) {
                player.setPose(Pose.STANDING);
            }
        } catch (NullPointerException ignored) {}

        // ── Time Freeze cleanup: restore mob AI when effect expires ───────────
        // removeAttributeModifiers() lost its entity parameter in MC 1.20.4, so we
        // detect expiry here and call setNoAi(false) for every mob this player froze.
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (!FrozenMobCache.hasFrozenMobs(player.getUUID())) return;
        boolean hasFreeze;
        try { hasFreeze = player.hasEffect(ultimate_apple_modForge.TIME_FREEZE_EFFECT.get()); }
        catch (NullPointerException e) { hasFreeze = false; }
        if (!hasFreeze) {
            serverLevel.getEntitiesOfClass(Mob.class,
                    player.getBoundingBox().inflate(200),
                    mob -> FrozenMobCache.isFrozen(mob.getUUID()))
                .forEach(mob -> mob.setNoAi(false));
            FrozenMobCache.clearPlayer(player.getUUID());
        }
    }

    // ── Totem Apple — cancel death ────────────────────────────────────────────

    /**
     * If the dying player has the Totem Protection effect (from eating a Totem Apple),
     * cancel the death, consume the effect, restore health, and play the vanilla totem
     * animation + sound (entity event 35) so the player gets clear visual feedback.
     *
     * Priority HIGHEST ensures this runs before other death listeners.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerTotemDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel)) return;

        try {
            if (!player.hasEffect(ultimate_apple_modForge.TOTEM_PROTECTION_EFFECT.get())) return;
        } catch (NullPointerException ignored) { return; }

        // Cancel the death
        event.setCanceled(true);

        // Remove the one-time protection
        player.removeEffect(ultimate_apple_modForge.TOTEM_PROTECTION_EFFECT.get());

        // Restore health and apply the same buffs vanilla totem gives
        player.setHealth(1.0f);
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,     20 * 45, 1));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,  20 * 40, 0));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,       20 * 15, 3));

        // Entity event 35 → client plays totem-of-undying animation + sound
        player.level().broadcastEntityEvent(player, (byte) 35);

        player.displayClientMessage(
            Component.translatable("message.ultimate_apple_mod.totem_apple_triggered"), true);
    }

    /**
     * Lifesteal: heal 1 heart whenever a hostile mob dies near a player with the effect.
     *
     * Triggered by:
     *  - Direct player kill (sword, bow, etc.)
     *  - Wither/DoT kill — the mob may have gotten Wither II from the Wither Apple,
     *    so DamageSource.getEntity() is null in that case. We scan nearby players instead.
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // Server-side only
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;
        // Skip other players — lifesteal works on any mob or animal, just not players
        if (event.getEntity() instanceof Player) return;

        LivingEntity dying = event.getEntity();

        // 1. Prefer the direct killer if they carry Lifesteal
        ServerPlayer recipient = null;
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof ServerPlayer sp
                && sp.hasEffect(ultimate_apple_modForge.LIFESTEAL_EFFECT.get())) {
            recipient = sp;
        }

        // 2. Fallback: any nearby player with Lifesteal.
        //    32-block radius — Wither-cursed mobs can wander before dying.
        //    Also covers DoT / indirect damage sources where getEntity() is null.
        if (recipient == null) {
            double best = Double.MAX_VALUE;
            for (ServerPlayer sp : serverLevel.players()) {
                double dist = sp.distanceToSqr(dying);
                if (dist <= 32.0 * 32.0
                        && dist < best
                        && sp.hasEffect(ultimate_apple_modForge.LIFESTEAL_EFFECT.get())) {
                    recipient = sp;
                    best = dist;
                }
            }
        }

        if (recipient == null) return;

        // Heal 1 heart
        recipient.heal(2.0f);

        // Action bar feedback
        recipient.displayClientMessage(
            Component.translatable("message.ultimate_apple_mod.lifesteal_heal"), true);

        // Deep soul-drain sound
        serverLevel.playSound(null,
            recipient.getX(), recipient.getY(), recipient.getZ(),
            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
            0.8f, 0.4f);

        // Red crimson spore cloud
        serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE,
            recipient.getX(), recipient.getY() + 1.0, recipient.getZ(),
            14, 0.5, 0.9, 0.5, 0.04);

        // Hearts floating up
        serverLevel.sendParticles(ParticleTypes.HEART,
            recipient.getX(), recipient.getY() + 2.1, recipient.getZ(),
            4, 0.4, 0.15, 0.4, 0.0);
    }
}
