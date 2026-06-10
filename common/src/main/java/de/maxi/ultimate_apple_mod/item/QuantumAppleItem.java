package de.maxi.ultimate_apple_mod.item;

import de.maxi.ultimate_apple_mod.DragonChargesCache;
import de.maxi.ultimate_apple_mod.ModRegistries;
import de.maxi.ultimate_apple_mod.RewindPositionCache;
import de.maxi.ultimate_apple_mod.block.MixerRecipes;
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

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Picks a random apple from the Mixer registry and applies ALL of its effects,
 * including special flags (lifesteal, witherCurse, dragonCharges, clearsEffects,
 * voidLaunch, rewindEffect, orchardSpawn, enderTeleport).
 *
 * The outcome pool is split 60 % positive / 40 % negative:
 *   60 % — contributions whose primary effects benefit the player
 *   40 % — contributions that hurt the player (Hunger, Nausea, Slowness, etc.)
 *
 * The pool excludes Apple Bomb (isBomb = true) — a throwable can't be "eaten".
 */
public class QuantumAppleItem extends Item {

    private static final Random RNG = new Random();

    /**
     * Lazy-initialised 60 / 40 pools.
     * Built on first eat so MixerRecipes is guaranteed to be fully populated.
     */
    private static volatile List<MixerRecipes.ShakeContribution> POSITIVE_POOL = null;
    private static volatile List<MixerRecipes.ShakeContribution> NEGATIVE_POOL = null;

    public QuantumAppleItem() {
        super(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(6)
                .saturationModifier(0.6f)
                .alwaysEdible()
                .build())
            .stacksTo(64));
    }

    // ── 60 / 40 pool ─────────────────────────────────────────────────────────

    private static void ensurePools() {
        if (POSITIVE_POOL != null) return;
        List<MixerRecipes.ShakeContribution> pos = new ArrayList<>();
        List<MixerRecipes.ShakeContribution> neg = new ArrayList<>();
        for (MixerRecipes.ShakeContribution c : MixerRecipes.getRandomizableContributions()) {
            if (isNegative(c)) neg.add(c); else pos.add(c);
        }
        POSITIVE_POOL = List.copyOf(pos);
        NEGATIVE_POOL = List.copyOf(neg);
    }

    /**
     * Returns {@code true} when a contribution primarily harms the player.
     * Criteria: the effects list contains at least one negative vanilla effect
     * (hunger, nausea, slowness, weakness, blindness) or the custom
     * curse_of_rotten effect.
     */
    private static boolean isNegative(MixerRecipes.ShakeContribution c) {
        for (MixerRecipes.EffectData e : c.effects()) {
            String path = e.id().getPath();
            if (path.equals("hunger")
                    || path.equals("nausea")
                    || path.equals("slowness")
                    || path.equals("weakness")
                    || path.equals("blindness")
                    || path.equals("curse_of_rotten")) {
                return true;
            }
        }
        return false;
    }

    // ── Eating ───────────────────────────────────────────────────────────────

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            ensurePools();
            // 60 % positive, 40 % negative
            List<MixerRecipes.ShakeContribution> pool =
                RNG.nextDouble() < 0.6 ? POSITIVE_POOL : NEGATIVE_POOL;
            if (pool != null && !pool.isEmpty()) {
                MixerRecipes.ShakeContribution chosen = pool.get(RNG.nextInt(pool.size()));
                applyContribution(chosen, player, level);
            }
        }
        return result;
    }

    private static void applyContribution(MixerRecipes.ShakeContribution chosen,
                                           ServerPlayer player, Level level) {

        if (chosen.clearsEffects()) {
            // Honey Apple behaviour: clear active effects FIRST, then fall through
            // so the contribution's own effects (e.g. Honey's Slowness) still apply —
            // matching how ShakeItem handles cleansing shakes.
            player.removeAllEffects();
        }

        // ── Standard mob effects ──────────────────────────────────────────
        for (MixerRecipes.EffectData e : chosen.effects()) {
            MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(
                ResourceLocation.tryParse(e.id().toString()));
            if (effect != null) {
                player.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect), e.duration(), e.amplifier()));
            }
        }

        // ── Dragon breath charges ─────────────────────────────────────────
        if (chosen.dragonCharges() > 0) {
            DragonChargesCache.addCharges(player.getUUID(), chosen.dragonCharges());
            player.displayClientMessage(
                Component.translatable(
                    "message.ultimate_apple_mod.dragon_charges_added",
                    chosen.dragonCharges()),
                true);
        }

        // ── Lifesteal (60 s) ──────────────────────────────────────────────
        if (chosen.lifesteal()) {
            player.addEffect(new MobEffectInstance(
                ModRegistries.LIFESTEAL.get(), 20 * 60, 0));
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

        // ── Rewind — teleport back 10 seconds ────────────────────────────
        if (chosen.rewindEffect()) {
            Vec3 oldPos = RewindPositionCache.getPositionTenSecondsAgo(player);
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
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
