package de.maxi.ultimate_apple_mod.item;

import de.maxi.ultimate_apple_mod.event.RewindTracker;
import de.maxi.ultimate_apple_mod.forge.block.MixerRecipes;
import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Picks a random apple from the Mixer registry and applies ALL of its effects,
 * including special flags (lifesteal, witherCurse, dragonCharges, clearsEffects,
 * voidLaunch, rewindEffect, orchardSpawn, enderTeleport).
 *
 * The picking is WEIGHTED so the mod's custom effects appear more often:
 *   weight 3 — CurseOfRotten, Lifesteal, VoidLaunch, TotemProtection, TimeFreeze
 *   weight 2 — Moon Gravity (notable but not overwhelming)
 *   weight 1 — everything else (still possible, just less frequent)
 *
 * The pool excludes Apple Bomb (isBomb = true) — a throwable can't be "eaten".
 */
public class QuantumAppleItem extends Item {

    private static final Random RNG = new Random();

    /**
     * Lazy-initialised weighted pool.
     * Built on first eat so MixerRecipes is guaranteed to be fully populated.
     */
    private static volatile List<MixerRecipes.ShakeContribution> WEIGHTED_POOL = null;

    public QuantumAppleItem() {
        super(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(6)
                .saturationMod(0.6f)
                .alwaysEat()
                .build())
            .stacksTo(64));
    }

    // ── Weighted pool ────────────────────────────────────────────────────────

    private static List<MixerRecipes.ShakeContribution> getWeightedPool() {
        if (WEIGHTED_POOL == null) {
            List<MixerRecipes.ShakeContribution> pool = new ArrayList<>();
            // getRandomizableContributions() excludes isBomb entries
            for (MixerRecipes.ShakeContribution c : MixerRecipes.getRandomizableContributions()) {
                int w = weight(c);
                for (int i = 0; i < w; i++) pool.add(c);
            }
            WEIGHTED_POOL = List.copyOf(pool);
        }
        return WEIGHTED_POOL;
    }

    /**
     * Returns the weight for a contribution.
     * Custom mod effects the user wants to see more often get weight 3,
     * Moon Gravity gets weight 2 ("not too often"), everything else weight 1.
     */
    private static int weight(MixerRecipes.ShakeContribution c) {
        // VoidLaunch → Void Apple
        if (c.voidLaunch()) return 3;
        // Lifesteal → Wither Apple (also has witherCurse)
        if (c.lifesteal()) return 3;

        for (MixerRecipes.EffectData e : c.effects()) {
            String path = e.id().getPath();
            // Boosted custom effects
            if (path.equals("curse_of_rotten"))  return 3;  // Rotten Apple
            if (path.equals("totem_protection")) return 3;  // Totem Apple
            if (path.equals("time_freeze"))      return 3;  // Time Freeze Apple
            // Slightly boosted
            if (path.equals("moon_gravity"))     return 2;  // Moon Apple
        }
        return 1;
    }

    // ── Eating ───────────────────────────────────────────────────────────────

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            List<MixerRecipes.ShakeContribution> pool = getWeightedPool();
            if (!pool.isEmpty()) {
                MixerRecipes.ShakeContribution chosen = pool.get(RNG.nextInt(pool.size()));
                applyContribution(chosen, player, level);
            }
        }
        return result;
    }

    private static void applyContribution(MixerRecipes.ShakeContribution chosen,
                                           ServerPlayer player, Level level) {

        if (chosen.clearsEffects()) {
            // Honey Apple behaviour: clears all active effects, nothing added
            player.removeAllEffects();
            return;
        }

        // ── Standard mob effects ──────────────────────────────────────────
        for (MixerRecipes.EffectData e : chosen.effects()) {
            MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(
                ResourceLocation.tryParse(e.id().toString()));
            if (effect != null) {
                player.addEffect(new MobEffectInstance(effect, e.duration(), e.amplifier()));
            }
        }

        // ── Dragon breath charges ─────────────────────────────────────────
        if (chosen.dragonCharges() > 0) {
            int existing = player.getPersistentData().getInt("dragonBreathCharges");
            player.getPersistentData().putInt(
                "dragonBreathCharges", existing + chosen.dragonCharges());
            player.displayClientMessage(
                Component.translatable(
                    "message.ultimate_apple_mod.dragon_charges_added",
                    chosen.dragonCharges()),
                true);
        }

        // ── Lifesteal (60 s) ──────────────────────────────────────────────
        if (chosen.lifesteal()) {
            player.addEffect(new MobEffectInstance(
                ultimate_apple_modForge.LIFESTEAL_EFFECT.get(), 20 * 60, 0));
        }

        // ── Wither curse — Wither II on nearby mobs ───────────────────────
        if (chosen.witherCurse()) {
            AABB area = player.getBoundingBox().inflate(8.0);
            List<LivingEntity> mobs = level.getEntitiesOfClass(
                LivingEntity.class, area, e -> e != player && e instanceof Mob);
            for (LivingEntity mob : mobs) {
                mob.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 60, 1));
            }
            if (!mobs.isEmpty()) {
                player.displayClientMessage(
                    Component.translatable("message.ultimate_apple_mod.wither_curse_applied"),
                    true);
            }
        }

        // ── Void launch ───────────────────────────────────────────────────
        if (chosen.voidLaunch()) {
            Vec3 motion = player.getDeltaMovement();
            boolean falling = motion.y < -0.05;
            if (falling) {
                player.setDeltaMovement(motion.x * 0.2, 6.5, motion.z * 0.2);
            } else {
                player.setDeltaMovement(motion.x, 2.5, motion.z);
            }
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 15, 0));
            player.connection.send(new ClientboundSetEntityMotionPacket(
                player.getId(), player.getDeltaMovement()));
        }

        // ── Rewind — teleport back 5 seconds ─────────────────────────────
        if (chosen.rewindEffect()) {
            Vec3 oldPos = RewindTracker.getPositionFiveSecondsAgo(player);
            if (oldPos != null) {
                player.teleportTo(oldPos.x, oldPos.y, oldPos.z);
                player.fallDistance = 0;
                player.setDeltaMovement(player.getDeltaMovement().x, 0,
                    player.getDeltaMovement().z);
                player.displayClientMessage(
                    Component.translatable("message.ultimate_apple_mod.rewind"), true);
            } else {
                player.displayClientMessage(
                    Component.translatable("message.ultimate_apple_mod.rewind_no_history"), true);
            }
        }

        // ── Orchard spawn — plant up to 4 trees ──────────────────────────
        if (chosen.orchardSpawn()) {
            if (level instanceof ServerLevel sl) {
                OrchardCallerItem.plantTrees(sl, player.blockPosition(), sl.getRandom(), 4);
            }
        }

        // ── Ender teleport — look-direction dash ──────────────────────────
        if (chosen.enderTeleport()) {
            ShakeBombEntity.performEnderTeleport(player);
        }
    }

    // ── Tooltip ───────────────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("§6Random apple effect on eat.")
            .withStyle(ChatFormatting.GOLD));
        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            components.add(Component.literal("§7Every effect from every registered apple")
                .withStyle(ChatFormatting.GRAY));
            components.add(Component.literal("§7is equally possible — including special")
                .withStyle(ChatFormatting.GRAY));
            components.add(Component.literal("§7abilities like void launch, rewind,")
                .withStyle(ChatFormatting.GRAY));
            components.add(Component.literal("§7wither curse and orchard spawning.")
                .withStyle(ChatFormatting.GRAY));
            components.add(Component.literal("§8Custom effects (Lifesteal, Void, Totem…)")
                .withStyle(ChatFormatting.DARK_GRAY));
            components.add(Component.literal("§8appear more often than vanilla effects.")
                .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            components.add(Component.literal("§7Could be anything. §8[SHIFT for details]")
                .withStyle(ChatFormatting.GRAY));
        }
    }
}
