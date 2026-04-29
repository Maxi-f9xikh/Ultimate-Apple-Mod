package de.maxi.ultimate_apple_mod.item;

import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Eating this apple grants the "Totem Protection" effect for an extremely long
 * duration (10 in-game days ≈ ~3.3 real hours at 20 TPS).
 *
 * When the player would die while carrying this effect, PlayerEffectEventHandler
 * cancels the death, removes the effect, and plays the vanilla totem animation.
 * Because the effect is persisted in the player's NBT, it survives server restarts.
 */
public class TotemAppleItem extends Item {

    /** 10 in-game days × 24000 ticks/day = 240 000 ticks ≈ 3.3 real hours. */
    public static final int PROTECTION_DURATION = 24_000 * 10;

    public TotemAppleItem() {
        super(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(4)
                .saturationMod(0.5f)
                .alwaysEat()
                .build())
            .stacksTo(64));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof Player player) {
            // Eating another Totem Apple while already protected resets the timer.
            player.removeEffect(ultimate_apple_modForge.TOTEM_PROTECTION_EFFECT.get());
            player.addEffect(new MobEffectInstance(
                ultimate_apple_modForge.TOTEM_PROTECTION_EFFECT.get(),
                PROTECTION_DURATION, 0,
                false,   // not ambient
                false,   // no particles
                true     // show icon so the player knows they are protected
            ));
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("§6Prevents your next death.")
            .withStyle(ChatFormatting.GOLD));
        components.add(Component.literal("§7Effect persists across restarts. One use only.")
            .withStyle(ChatFormatting.GRAY));
    }
}
