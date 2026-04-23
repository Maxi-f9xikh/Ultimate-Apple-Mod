package de.maxi.ultimate_apple_mod.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlazingAppleStewItem extends Item {
    public BlazingAppleStewItem() {
        super(new Properties()
                .food(new FoodProperties.Builder()
                        .nutrition(6)
                        .saturationMod(0.5f)
                        .alwaysEat()
                        .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 20 * 60, 0), 1.0f)
                        .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 60, 0), 1.0f)
                        .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 150, 0), 1.0f)
                        .effect(() -> new MobEffectInstance(MobEffects.HEAL, 20 * 120, 0), 1.0f)
                        .build())
                .stacksTo(1)
        );
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        super.finishUsingItem(stack, level, entity);
        return new ItemStack(Items.BOWL);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("§6Feuerfest und kraftvoll – hält dich am Leben!"));
        tooltip.add(Component.translatable("§7Gibt Regeneration, Stärke und Feuerresistenz."));
    }
}
