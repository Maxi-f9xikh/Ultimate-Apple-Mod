package de.maxi.ultimate_apple_mod.event;

import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
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

    // ── Hitbox resize ─────────────────────────────────────────────────────────

    /**
     * Returns shrunken dimensions (0.25 × 0.6) whenever a player has Curse of Rotten.
     * Fires whenever getDimensions(Pose) is called, so it covers both standing-height
     * checks (for pose selection) and actual bounding-box updates.
     */
    @SubscribeEvent
    public static void onEntitySize(EntityEvent.Size event) {
        if (!(event.getEntity() instanceof Player player)) return;
        try {
            if (player.hasEffect(ultimate_apple_modForge.CURSE_OF_ROTTEN.get())) {
                event.setNewSize(EntityDimensions.scalable(0.25f, 0.6f));
            }
        } catch (NullPointerException ignored) {
            // EntityEvent.Size fires during entity construction before activeEffects is initialized
        }
    }

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
        // Only hostile mobs count (not animals, not players)
        if (!(event.getEntity() instanceof Enemy)) return;

        LivingEntity dying = event.getEntity();

        // 1. Prefer the direct killer if they carry Lifesteal
        ServerPlayer recipient = null;
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof ServerPlayer sp
                && sp.hasEffect(ultimate_apple_modForge.LIFESTEAL_EFFECT.get())) {
            recipient = sp;
        }

        // 2. Fallback: any nearby player with Lifesteal (covers Wither / DoT deaths)
        if (recipient == null) {
            for (ServerPlayer sp : serverLevel.players()) {
                if (sp.distanceTo(dying) <= 16.0f
                        && sp.hasEffect(ultimate_apple_modForge.LIFESTEAL_EFFECT.get())) {
                    recipient = sp;
                    break;
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
