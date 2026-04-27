package de.maxi.ultimate_apple_mod.event;

import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityDimensions;
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
            // EntityEvent.Size fires during entity construction before LivingEntity.activeEffects is initialized
        }
    }

    /**
     * When a player with Lifesteal kills a hostile mob:
     *  - Heal 1 heart
     *  - Play a deep soul-drain sound
     *  - Burst crimson spore + heart particles around the player
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // Server-side only
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;
        // Killer must be a player
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;
        // Only when Lifesteal is active
        if (!killer.hasEffect(ultimate_apple_modForge.LIFESTEAL_EFFECT.get())) return;
        // Only on hostile mobs (not animals, not players)
        if (!(event.getEntity() instanceof Enemy)) return;

        // Heal 1 heart
        killer.heal(2.0f);

        // Deep, low-pitched XP sound — "soul absorbed"
        serverLevel.playSound(null,
            killer.getX(), killer.getY(), killer.getZ(),
            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
            0.8f, 0.4f);

        // Red crimson spore cloud around the player
        serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE,
            killer.getX(), killer.getY() + 1.0, killer.getZ(),
            14, 0.5, 0.9, 0.5, 0.04);

        // A few hearts floating up
        serverLevel.sendParticles(ParticleTypes.HEART,
            killer.getX(), killer.getY() + 2.1, killer.getZ(),
            4, 0.4, 0.15, 0.4, 0.0);
    }
}
