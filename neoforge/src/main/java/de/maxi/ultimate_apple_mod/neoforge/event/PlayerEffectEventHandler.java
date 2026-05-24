package de.maxi.ultimate_apple_mod.neoforge.event;

import de.maxi.ultimate_apple_mod.neoforge.ultimate_apple_modNeoForge;
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
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.NEOFORGE)
public class PlayerEffectEventHandler {

    private static final WeakHashMap<Player, Boolean> serverRottenState = new WeakHashMap<>();
    private static final float ROTTEN_SCALE = 0.35f;

    @SubscribeEvent
    public static void onEntitySize(EntityEvent.Size event) {
        if (!(event.getEntity() instanceof Player player)) return;
        try {
            if (player.hasEffect(ultimate_apple_modNeoForge.CURSE_OF_ROTTEN.get())) {
                event.setNewSize(EntityDimensions.scalable(0.25f, 0.6f));
                event.setNewEyeHeight(1.62f * ROTTEN_SCALE);
            }
        } catch (NullPointerException ignored) {}
    }

    @SubscribeEvent
    public static void onServerPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Player player = event.player;
        if (!(player.level() instanceof ServerLevel)) return;

        boolean hasEffect;
        try {
            hasEffect = player.hasEffect(ultimate_apple_modNeoForge.CURSE_OF_ROTTEN.get());
        } catch (NullPointerException ignored) { return; }

        Boolean prev = serverRottenState.get(player);
        if (prev == null || prev != hasEffect) {
            serverRottenState.put(player, hasEffect);
            player.refreshDimensions();
        }
    }

    @SubscribeEvent
    public static void onServerPlayerTickEnd(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (!(player.level() instanceof ServerLevel)) return;

        try {
            if (player.hasEffect(ultimate_apple_modNeoForge.CURSE_OF_ROTTEN.get())
                    && player.getPose() == Pose.SWIMMING
                    && !player.isInWater()) {
                player.setPose(Pose.STANDING);
            }
        } catch (NullPointerException ignored) {}
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerTotemDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel)) return;

        try {
            if (!player.hasEffect(ultimate_apple_modNeoForge.TOTEM_PROTECTION_EFFECT.get())) return;
        } catch (NullPointerException ignored) { return; }

        event.setCanceled(true);
        player.removeEffect(ultimate_apple_modNeoForge.TOTEM_PROTECTION_EFFECT.get());
        player.setHealth(1.0f);
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,     20 * 45, 1));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,  20 * 40, 0));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,       20 * 15, 3));
        player.level().broadcastEntityEvent(player, (byte) 35);
        player.displayClientMessage(
            Component.translatable("message.ultimate_apple_mod.totem_apple_triggered"), true);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;
        if (event.getEntity() instanceof Player) return;

        LivingEntity dying = event.getEntity();
        ServerPlayer recipient = null;
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof ServerPlayer sp
                && sp.hasEffect(ultimate_apple_modNeoForge.LIFESTEAL_EFFECT.get())) {
            recipient = sp;
        }

        if (recipient == null) {
            double best = Double.MAX_VALUE;
            for (ServerPlayer sp : serverLevel.players()) {
                double dist = sp.distanceToSqr(dying);
                if (dist <= 32.0 * 32.0 && dist < best
                        && sp.hasEffect(ultimate_apple_modNeoForge.LIFESTEAL_EFFECT.get())) {
                    recipient = sp;
                    best = dist;
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
    }
}
