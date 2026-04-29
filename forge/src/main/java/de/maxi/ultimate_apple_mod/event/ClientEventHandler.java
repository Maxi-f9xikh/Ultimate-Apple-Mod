package de.maxi.ultimate_apple_mod.event;

import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-only event handler.
 * Currently handles the decay countdown tooltip for vanilla apples.
 */
@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID,
                        bus   = Mod.EventBusSubscriber.Bus.FORGE,
                        value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(DecayEventHandler.DECAY_TAG)) return;

        long threshold = DecayEventHandler.getDecayThreshold(stack.getItem());
        if (threshold == 0) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        long decayStart = tag.getLong(DecayEventHandler.DECAY_TAG);
        long remaining  = threshold - (mc.level.getGameTime() - decayStart);

        if (remaining <= 0) {
            event.getToolTip().add(
                Component.literal("§cRots any moment now!"));
            return;
        }

        long totalSecs = remaining / 20;
        long minutes   = totalSecs / 60;
        long seconds   = totalSecs % 60;

        event.getToolTip().add(
            Component.literal(String.format("§7Rots in: §e%d:%02d", minutes, seconds)));
    }
}
