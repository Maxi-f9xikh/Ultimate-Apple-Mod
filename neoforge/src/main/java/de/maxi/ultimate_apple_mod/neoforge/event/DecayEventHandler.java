package de.maxi.ultimate_apple_mod.neoforge.event;

import de.maxi.ultimate_apple_mod.ModRegistries;
import de.maxi.ultimate_apple_mod.event.DecayHelper;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.TickEvent;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.NEOFORGE)
public class DecayEventHandler {

    public static final String DECAY_TAG = DecayHelper.DECAY_TAG;
    public static final long APPLE_DECAY_TICKS           = DecayHelper.APPLE_DECAY_TICKS;
    public static final long GOLDEN_APPLE_DECAY_TICKS    = 20L * 60 * 45;
    public static final long ENCHANTED_APPLE_DECAY_TICKS = 20L * 60 * 60;

    public static long getDecayThreshold(net.minecraft.world.item.Item item) {
        return DecayHelper.getDecayThreshold(item);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player.level() instanceof ServerLevel serverLevel)) return;
        if (serverLevel.getGameTime() % 20 != 0) return;

        Player player = event.player;
        long now = serverLevel.getGameTime();
        Inventory inv = player.getInventory();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            long threshold = getDecayThreshold(stack.getItem());
            if (threshold == 0) continue;

            CompoundTag tag = stack.getOrCreateTag();
            if (!tag.contains(DECAY_TAG)) {
                tag.putLong(DECAY_TAG, now);
                continue;
            }

            long elapsed = now - tag.getLong(DECAY_TAG);
            if (elapsed < threshold) continue;

            ItemStack replacement = getDecayReplacement(stack);
            inv.setItem(i, replacement);
        }
    }

    private static ItemStack getDecayReplacement(ItemStack original) {
        int count = original.getCount();
        if (original.getItem() == Items.APPLE) {
            return new ItemStack(ModRegistries.ROTTEN_APPLE.get(), count);
        }
        return ItemStack.EMPTY;
    }
}
