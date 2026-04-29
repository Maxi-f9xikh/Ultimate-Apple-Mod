package de.maxi.ultimate_apple_mod.item;

import de.maxi.ultimate_apple_mod.forge.block.MixerRecipes;
import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
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
import java.util.List;
import java.util.Random;

/**
 * Picks a random apple from the Mixer registry and applies ALL of its effects,
 * including special flags (lifesteal, witherCurse, dragonCharges, clearsEffects).
 * Every eat is a surprise — high risk, high reward.
 */
public class QuantumAppleItem extends Item {

    private static final Random RNG = new Random();

    public QuantumAppleItem() {
        super(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(6)
                .saturationMod(0.6f)
                .alwaysEat()
                .build())
            .stacksTo(64));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            List<MixerRecipes.ShakeContribution> pool = MixerRecipes.getAllContributions();
            if (!pool.isEmpty()) {
                MixerRecipes.ShakeContribution chosen = pool.get(RNG.nextInt(pool.size()));

                if (chosen.clearsEffects()) {
                    // Honey-Apple behaviour: cleanse only, no other effects
                    player.removeAllEffects();
                } else {
                    // ── Standard mob effects ────────────────────────────────
                    for (MixerRecipes.EffectData e : chosen.effects()) {
                        MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(
                            ResourceLocation.tryParse(e.id().toString()));
                        if (effect != null) {
                            player.addEffect(new MobEffectInstance(
                                effect, e.duration(), e.amplifier()));
                        }
                    }

                    // ── Dragon breath charges ───────────────────────────────
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

                    // ── Lifesteal (60 s) ────────────────────────────────────
                    if (chosen.lifesteal()) {
                        player.addEffect(new MobEffectInstance(
                            ultimate_apple_modForge.LIFESTEAL_EFFECT.get(), 20 * 60, 0));
                    }

                    // ── Wither curse — Wither II on nearby mobs ─────────────
                    if (chosen.witherCurse()) {
                        AABB area = player.getBoundingBox().inflate(8.0);
                        List<LivingEntity> mobs = level.getEntitiesOfClass(
                            LivingEntity.class, area, e -> e != player && e instanceof Mob);
                        for (LivingEntity mob : mobs) {
                            mob.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 60, 1));
                        }
                        if (!mobs.isEmpty()) {
                            player.displayClientMessage(
                                Component.translatable(
                                    "message.ultimate_apple_mod.wither_curse_applied"),
                                true);
                        }
                    }

                    // ── Void launch ─────────────────────────────────────────
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
                }
            }
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("§6Random apple effect on eat.")
            .withStyle(ChatFormatting.GOLD));
        components.add(Component.literal("§7Could be anything. Good luck.")
            .withStyle(ChatFormatting.GRAY));
    }
}
