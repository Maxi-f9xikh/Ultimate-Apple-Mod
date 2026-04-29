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
 * Void Apple — emergency aerial recovery.
 *
 * While falling: strong upward velocity kick + 8 s Slow Fall + Speed II →
 * simulates catching yourself mid-air and gliding to safety.
 *
 * While on ground: smaller kick + 5 s Slow Fall (lets you leap off cliffs
 * intentionally and glide down).
 */
public class VoidAppleItem extends Item {

    public VoidAppleItem() {
        super(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(2)
                .saturationMod(0.2f)
                .alwaysEat()
                .build())
            .stacksTo(16));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            Vec3 motion = player.getDeltaMovement();
            boolean falling = motion.y < -0.1;

            if (falling) {
                // Emergency rescue: strong upward burst + 8 s glide
                player.setDeltaMovement(motion.x * 0.4, 1.7, motion.z * 0.4);
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 8, 0));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 8, 1));
            } else {
                // Intentional launch from ground: moderate kick + 5 s glide
                player.setDeltaMovement(motion.x, 0.9, motion.z);
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 5, 0));
            }

            // Force-sync the new velocity to the client immediately
            player.connection.send(
                new ClientboundSetEntityMotionPacket(player.getId(), player.getDeltaMovement()));
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("§6Eat while falling to launch upward and glide.")
            .withStyle(ChatFormatting.GOLD));
        components.add(Component.literal("§7Also works on the ground for a boost.")
            .withStyle(ChatFormatting.GRAY));
    }
}
