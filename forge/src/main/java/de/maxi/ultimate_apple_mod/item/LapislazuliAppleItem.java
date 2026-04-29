package de.maxi.ultimate_apple_mod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Lapis Lazuli Apple — the enchanter's snack.
 *
 * Lapis Lazuli is the fuel of enchanting tables. Eating this apple channels
 * that mystic energy directly into the player: Night Vision illuminates your
 * surroundings (letting you read enchanting glyphs in the dark), and 5 free
 * experience levels are granted immediately.
 *
 * Luck I is included as a nod to the ore's historical "fortune" associations.
 */
public class LapislazuliAppleItem extends Item {

    /** Experience levels granted on each eat. */
    public static final int XP_LEVELS = 5;

    public LapislazuliAppleItem() {
        super(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(8)
                .saturationMod(0.9f)
                .alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 300, 0), 1.0f) // 5 min
                .effect(() -> new MobEffectInstance(MobEffects.LUCK,         20 * 120, 0), 1.0f) // 2 min
                .build())
            .stacksTo(64));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        // Grant experience levels — the true power of Lapis Lazuli
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            player.giveExperienceLevels(XP_LEVELS);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("§9Grants +" + XP_LEVELS + " experience levels on eat.")
            .withStyle(ChatFormatting.BLUE));
        components.add(Component.literal("§7Night Vision · Luck I — 5 & 2 min")
            .withStyle(ChatFormatting.GRAY));
    }
}
