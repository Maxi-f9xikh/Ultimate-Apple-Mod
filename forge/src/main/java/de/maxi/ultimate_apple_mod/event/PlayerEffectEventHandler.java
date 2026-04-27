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
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEffectEventHandler {

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
