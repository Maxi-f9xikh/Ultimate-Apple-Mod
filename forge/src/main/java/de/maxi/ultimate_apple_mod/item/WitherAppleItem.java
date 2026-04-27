package de.maxi.ultimate_apple_mod.item;

import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class WitherAppleItem extends Item {

    public WitherAppleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide() && entity instanceof Player player) {
            AABB area = player.getBoundingBox().inflate(10.0);
            List<LivingEntity> nearbyMobs = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e instanceof Enemy);
            for (LivingEntity mob : nearbyMobs) {
                mob.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 8, 1));
            }
            if (!nearbyMobs.isEmpty()) {
                player.displayClientMessage(
                    Component.translatable("message.ultimate_apple_mod.wither_curse_applied"), true);
            }
            // Always grant 1 minute of lifesteal
            player.addEffect(new MobEffectInstance(ultimate_apple_modForge.LIFESTEAL_EFFECT.get(), 20 * 60, 0));
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("tooltip.ultimate_apple_mod.wither_apple.line1"));
        components.add(Component.translatable("tooltip.ultimate_apple_mod.wither_apple.line2"));
    }
}
