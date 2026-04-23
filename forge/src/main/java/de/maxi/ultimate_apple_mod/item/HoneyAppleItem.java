package de.maxi.ultimate_apple_mod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class HoneyAppleItem extends Item {

    public HoneyAppleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide()) {
            livingEntity.removeAllEffects();
        }
        return super.finishUsingItem(stack, level, livingEntity);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("tooltip.ultimate_apple_mod.honey_apple.line1"));
        components.add(Component.translatable("tooltip.ultimate_apple_mod.honey_apple.line2"));
    }
}
