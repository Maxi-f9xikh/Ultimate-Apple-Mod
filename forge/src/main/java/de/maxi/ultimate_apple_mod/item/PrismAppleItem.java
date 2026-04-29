package de.maxi.ultimate_apple_mod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Prism Apple — forged from the depths of the ocean monument.
 *
 * Grants Water Breathing + Dolphin's Grace + Speed II for 5 minutes,
 * making the player noticeably faster than a boat underwater.
 *
 * Obtainable by crafting (8 Prismarine Shards + 1 Golden Apple) or as
 * a rare drop from Drowned (5 %) and Elder Guardians (15 %).
 */
public class PrismAppleItem extends Item {

    public PrismAppleItem() {
        super(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(6)
                .saturationMod(0.8f)
                .alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.WATER_BREATHING, 20 * 300, 0), 1.0f) // 5 min
                .effect(() -> new MobEffectInstance(MobEffects.DOLPHINS_GRACE,  20 * 300, 0), 1.0f) // 5 min
                .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED,  20 * 300, 1), 1.0f) // Speed II, 5 min
                .build())
            .stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("§bBreath and speed of the deep sea.")
            .withStyle(ChatFormatting.AQUA));
        components.add(Component.literal("§7Water Breathing · Dolphin's Grace · Speed II — 5 min")
            .withStyle(ChatFormatting.GRAY));
    }
}
