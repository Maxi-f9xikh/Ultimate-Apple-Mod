package de.maxi.ultimate_apple_mod.item;

import de.maxi.ultimate_apple_mod.forge.block.MixerRecipes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * Picks a random apple from the Mixer registry and applies its effects.
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
            .stacksTo(16));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            List<MixerRecipes.ShakeContribution> pool = MixerRecipes.getAllContributions();
            if (!pool.isEmpty()) {
                MixerRecipes.ShakeContribution chosen = pool.get(RNG.nextInt(pool.size()));

                if (chosen.clearsEffects()) {
                    player.removeAllEffects();
                } else {
                    for (MixerRecipes.EffectData e : chosen.effects()) {
                        MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(
                            ResourceLocation.tryParse(e.id().toString()));
                        if (effect != null) {
                            player.addEffect(new MobEffectInstance(
                                effect, e.duration(), e.amplifier()));
                        }
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
