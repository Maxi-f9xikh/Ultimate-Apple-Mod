package de.maxi.ultimate_apple_mod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class DragonAppleItem extends Item {

    public static final String CHARGES_KEY = "dragonBreathCharges";

    public DragonAppleItem(Properties properties) {
        super(properties);
    }

    /**
     * Right-clicking always eats the apple (loading 1 charge).
     * Firing is handled by the keybinding (default R) via FireDragonBreathPacket.
     */

    /** Each apple eaten loads 1 charge. */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide() && entity instanceof Player player) {
            int charges = player.getPersistentData().getInt(CHARGES_KEY) + 1;
            player.getPersistentData().putInt(CHARGES_KEY, charges);
            player.displayClientMessage(
                Component.translatable("message.ultimate_apple_mod.dragon_breath_remaining", charges), true);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("tooltip.ultimate_apple_mod.dragon_apple.line1"));
        components.add(Component.translatable("tooltip.ultimate_apple_mod.dragon_apple.line2"));
    }
}
