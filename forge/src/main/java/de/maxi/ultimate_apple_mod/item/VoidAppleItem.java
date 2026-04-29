package de.maxi.ultimate_apple_mod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Void Apple — emergency aerial rescue.
 *
 * While falling: massive upward velocity burst (~55+ blocks) + 15 s Slow Falling.
 * While on ground: moderate kick + 8 s Slow Falling for intentional cliff-diving.
 *
 * No per-tick glide handler — Slow Falling handles the descent naturally.
 */
public class VoidAppleItem extends Item {

    public VoidAppleItem() {
        super(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(2)
                .saturationMod(0.2f)
                .alwaysEat()
                .build())
            .stacksTo(64));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            Vec3 motion = player.getDeltaMovement();
            boolean falling = motion.y < -0.05;

            if (falling) {
                // Emergency rescue: ~55+ blocks upward, horizontal dampened
                player.setDeltaMovement(motion.x * 0.2, 6.5, motion.z * 0.2);
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 15, 0));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 10, 1));
            } else {
                // Intentional ground launch
                player.setDeltaMovement(motion.x, 2.5, motion.z);
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 8, 0));
            }

            // Force-sync velocity to client immediately
            player.connection.send(
                new ClientboundSetEntityMotionPacket(player.getId(), player.getDeltaMovement()));
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("§6Eat while falling for a massive upward launch!")
            .withStyle(ChatFormatting.GOLD));
        components.add(Component.literal("§7Slow Falling carries you safely to the ground.")
            .withStyle(ChatFormatting.GRAY));
    }
}
