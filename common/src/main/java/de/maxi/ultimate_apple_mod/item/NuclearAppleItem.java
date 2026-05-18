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

import org.jetbrains.annotations.Nullable;
import java.util.List;

/**
 * Nuclear Apple — thrown projectile that wipes every block in the hit chunk,
 * from the minimum build height to the maximum build height (Y -64 → 320).
 *
 * Recipe: Netherite Apple centre, TNT Apple at each corner, TNT in edge slots.
 */
public class NuclearAppleItem extends Item {

    public NuclearAppleItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ultimate_apple_mod.nuclear_apple.line1"));
        tooltip.add(Component.translatable("tooltip.ultimate_apple_mod.nuclear_apple.line2"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TNT_PRIMED, SoundSource.PLAYERS,
                0.8f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));

        if (!level.isClientSide()) {
            NuclearAppleEntity projectile = new NuclearAppleEntity(player, level);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(),
                    0.0f, 1.5f, 1.0f);
            level.addFreshEntity(projectile);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
