package de.maxi.ultimate_apple_mod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import java.util.List;

public class AppleBombItem extends Item {

    public AppleBombItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level,
            List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("tooltip.ultimate_apple_mod.apple_bomb.line1"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL,
            0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));

        if (!level.isClientSide()) {
            AppleBombEntity bomb = new AppleBombEntity(player, level);
            bomb.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 1.0f);
            level.addFreshEntity(bomb);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
