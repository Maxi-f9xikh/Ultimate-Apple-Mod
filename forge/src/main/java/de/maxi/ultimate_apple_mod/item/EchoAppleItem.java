package de.maxi.ultimate_apple_mod.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import java.util.List;

public class EchoAppleItem extends Item {

    private static final String POS_KEY = "echo_apple_pos";

    public EchoAppleItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level,
            List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("tooltip.ultimate_apple_mod.echo_apple.line1"));
        tooltipComponents.add(Component.translatable("tooltip.ultimate_apple_mod.echo_apple.line2"));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (level.isClientSide() || !(livingEntity instanceof Player player)) {
            return super.finishUsingItem(stack, level, livingEntity);
        }

        CompoundTag data = player.getPersistentData();

        if (data.contains(POS_KEY)) {
            CompoundTag posTag = data.getCompound(POS_KEY);
            String savedDim = posTag.getString("dim");
            String currentDim = level.dimension().location().toString();

            if (!savedDim.equals(currentDim)) {
                player.displayClientMessage(
                    Component.translatable("message.ultimate_apple_mod.echo_wrong_dim"), true);
                return stack;
            }
        }

        ItemStack result = super.finishUsingItem(stack, level, livingEntity);
        ServerLevel serverLevel = (ServerLevel) level;

        if (!data.contains(POS_KEY)) {
            CompoundTag posTag = new CompoundTag();
            posTag.putDouble("x", player.getX());
            posTag.putDouble("y", player.getY());
            posTag.putDouble("z", player.getZ());
            posTag.putString("dim", level.dimension().location().toString());
            data.put(POS_KEY, posTag);

            player.displayClientMessage(
                Component.translatable("message.ultimate_apple_mod.echo_set"), true);
            serverLevel.sendParticles(ParticleTypes.SCULK_SOUL,
                player.getX(), player.getY() + 1.0, player.getZ(),
                8, 0.3, 0.3, 0.3, 0.05);
        } else {
            CompoundTag posTag = data.getCompound(POS_KEY);
            double x = posTag.getDouble("x");
            double y = posTag.getDouble("y");
            double z = posTag.getDouble("z");

            serverLevel.sendParticles(ParticleTypes.PORTAL,
                player.getX(), player.getY() + 1.0, player.getZ(),
                20, 0.5, 1.0, 0.5, 0.1);

            player.teleportTo(x, y, z);
            level.playSound(null, x, y, z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);

            serverLevel.sendParticles(ParticleTypes.PORTAL,
                x, y + 1.0, z, 20, 0.5, 1.0, 0.5, 0.1);

            data.remove(POS_KEY);
            player.displayClientMessage(
                Component.translatable("message.ultimate_apple_mod.echo_return"), true);
        }

        return result;
    }
}
