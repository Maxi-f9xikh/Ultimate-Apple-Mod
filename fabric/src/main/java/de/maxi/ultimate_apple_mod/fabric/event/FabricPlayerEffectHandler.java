package de.maxi.ultimate_apple_mod.fabric.event;

import de.maxi.ultimate_apple_mod.ModRegistries;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.WeakHashMap;

public class FabricPlayerEffectHandler {

    private static final WeakHashMap<Player, Boolean> serverRottenState = new WeakHashMap<>();

    public static void register() {

        // ── Totem Apple: cancel death ─────────────────────────────────────────
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (!(entity instanceof ServerPlayer player)) return true;
            try {
                if (!player.hasEffect(ModRegistries.TOTEM_PROTECTION.get())) return true;
            } catch (NullPointerException ignored) { return true; }

            player.removeEffect(ModRegistries.TOTEM_PROTECTION.get());
            player.setHealth(1.0f);
            player.removeAllEffects();
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 45, 1));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 40, 0));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20 * 15, 3));
            player.level().broadcastEntityEvent(player, (byte) 35);
            player.displayClientMessage(
                Component.translatable("message.ultimate_apple_mod.totem_apple_triggered"), true);
            return false; // cancel death
        });

        // ── Lifesteal: heal on nearby mob death ───────────────────────────────
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity.level() instanceof ServerLevel serverLevel)) return;
            if (entity instanceof Player) return;

            LivingEntity dying = entity;
            ServerPlayer recipient = null;
            Entity attacker = damageSource.getEntity();
            if (attacker instanceof ServerPlayer sp
                    && sp.hasEffect(ModRegistries.LIFESTEAL.get())) {
                recipient = sp;
            }
            if (recipient == null) {
                double best = Double.MAX_VALUE;
                for (ServerPlayer sp : serverLevel.players()) {
                    double dist = sp.distanceToSqr(dying);
                    if (dist <= 32.0 * 32.0 && dist < best
                            && sp.hasEffect(ModRegistries.LIFESTEAL.get())) {
                        recipient = sp; best = dist;
                    }
                }
            }
            if (recipient == null) return;

            recipient.heal(2.0f);
            recipient.displayClientMessage(
                Component.translatable("message.ultimate_apple_mod.lifesteal_heal"), true);
            serverLevel.playSound(null,
                recipient.getX(), recipient.getY(), recipient.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8f, 0.4f);
            serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE,
                recipient.getX(), recipient.getY() + 1.0, recipient.getZ(),
                14, 0.5, 0.9, 0.5, 0.04);
            serverLevel.sendParticles(ParticleTypes.HEART,
                recipient.getX(), recipient.getY() + 2.1, recipient.getZ(),
                4, 0.4, 0.15, 0.4, 0.0);
        });

        // ── CurseOfRotten: refreshDimensions + pose fix (server-side) ─────────
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                for (ServerPlayer player : level.players()) {
                    boolean hasEffect;
                    try { hasEffect = player.hasEffect(ModRegistries.CURSE_OF_ROTTEN.get()); }
                    catch (NullPointerException ignored) { continue; }

                    Boolean prev = serverRottenState.get(player);
                    if (prev == null || prev != hasEffect) {
                        serverRottenState.put(player, hasEffect);
                        player.refreshDimensions();
                    }
                    // Fix swimming pose on land
                    if (hasEffect && player.getPose() == Pose.SWIMMING && !player.isInWater()) {
                        player.setPose(Pose.STANDING);
                    }
                }
            }
        });
    }
}
