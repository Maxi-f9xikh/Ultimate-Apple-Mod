package de.maxi.ultimate_apple_mod.fabric.event;

import de.maxi.ultimate_apple_mod.ModRegistries;
import de.maxi.ultimate_apple_mod.event.DecayHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FabricDecayHandler {

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                long now = level.getGameTime();
                if (now % 20 != 0) continue; // once per second

                for (Player player : level.players()) {
                    Inventory inv = player.getInventory();
                    for (int i = 0; i < inv.getContainerSize(); i++) {
                        ItemStack stack = inv.getItem(i);
                        if (stack.isEmpty()) continue;

                        long threshold = DecayHelper.getDecayThreshold(stack.getItem());
                        if (threshold == 0) continue;

                        CompoundTag tag = stack.getOrCreateTag();
                        if (!tag.contains(DecayHelper.DECAY_TAG)) {
                            tag.putLong(DecayHelper.DECAY_TAG, now);
                            continue;
                        }
                        long elapsed = now - tag.getLong(DecayHelper.DECAY_TAG);
                        if (elapsed < threshold) continue;

                        // Decay: apple → rotten apple
                        int count = stack.getCount();
                        inv.setItem(i, new ItemStack(ModRegistries.ROTTEN_APPLE.get(), count));
                    }
                }
            }
        });
    }
}
