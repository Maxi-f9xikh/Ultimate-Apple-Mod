package de.maxi.ultimate_apple_mod.item;

import de.maxi.ultimate_apple_mod.event.RewindTracker;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class RewindAppleItem extends Item {

    public RewindAppleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            Vec3 oldPos = RewindTracker.getPositionFiveSecondsAgo(player);
            if (oldPos != null) {
                player.teleportTo(oldPos.x, oldPos.y, oldPos.z);
                // Reset fall state so the player doesn't take damage from the fall
                // that happened before the rewind (e.g. falling into the void).
                player.fallDistance = 0;
                player.setDeltaMovement(player.getDeltaMovement().x, 0, player.getDeltaMovement().z);
                player.displayClientMessage(
                    Component.translatable("message.ultimate_apple_mod.rewind"), true);
            } else {
                player.displayClientMessage(
                    Component.translatable("message.ultimate_apple_mod.rewind_no_history"), true);
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("tooltip.ultimate_apple_mod.rewind_apple.line1"));
        components.add(Component.translatable("tooltip.ultimate_apple_mod.rewind_apple.line2"));
    }
}
