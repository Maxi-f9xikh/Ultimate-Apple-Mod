package de.maxi.ultimate_apple_mod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class DragonAppleItem extends Item {

    private static final String CHARGES_KEY = "dragonBreathCharges";

    public DragonAppleItem(Properties properties) {
        super(properties);
    }

    /**
     * Sneak + right-click: shoot a Dragon Fireball (if charges > 0).
     * Regular right-click: eat the apple.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        int charges = player.getPersistentData().getInt(CHARGES_KEY);
        if (charges > 0 && player.isCrouching()) {
            if (!level.isClientSide()) {
                spawnDragonFireball(level, player);
                int remaining = charges - 1;
                player.getPersistentData().putInt(CHARGES_KEY, remaining);
                player.displayClientMessage(
                    Component.translatable("message.ultimate_apple_mod.dragon_breath_remaining", remaining), true);
            }
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }

    /** Each apple eaten adds 1 charge. */
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

    private static void spawnDragonFireball(Level level, Player player) {
        Vec3 look = player.getLookAngle();
        DragonFireball fireball = new DragonFireball(level, player, look.x, look.y, look.z);
        fireball.setPos(
            player.getX() + look.x * 1.5,
            player.getEyeY() - 0.1,
            player.getZ() + look.z * 1.5
        );
        level.addFreshEntity(fireball);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("tooltip.ultimate_apple_mod.dragon_apple.line1"));
        components.add(Component.translatable("tooltip.ultimate_apple_mod.dragon_apple.line2"));
    }
}
