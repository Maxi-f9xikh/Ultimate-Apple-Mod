package de.maxi.ultimate_apple_mod.event;

import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Decay System — vanilla apples in a player's inventory rot over time.
 *
 * Only INVENTORY items are tracked (not items in chests / dropped items).
 * A {@code "uam:decay_start"} NBT long tag records the world tick at which
 * the item first entered the player's inventory.  Once enough ticks have
 * elapsed the stack is replaced with Rotten Apples.
 *
 * Thresholds:
 *   Apple                  →  Rotten Apple  after 30 min (36 000 ticks)
 *   Golden Apple           →  Apple         after 45 min (54 000 ticks)
 *   Enchanted Golden Apple →  Golden Apple  after 60 min (72 000 ticks)
 *
 * The decay tag is synced to the client via normal inventory sync,
 * so ClientEventHandler can show a live countdown in the item tooltip.
 */
@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DecayEventHandler {

    public static final String DECAY_TAG = "uam:decay_start";

    public static final long APPLE_DECAY_TICKS              = 20L * 60 * 30;  // 30 min
    public static final long GOLDEN_APPLE_DECAY_TICKS       = 20L * 60 * 45;  // 45 min
    public static final long ENCHANTED_APPLE_DECAY_TICKS    = 20L * 60 * 60;  // 60 min

    /** Returns the decay threshold in ticks for the given item, or 0 if it does not decay. */
    public static long getDecayThreshold(Item item) {
        if (item == Items.APPLE)                  return APPLE_DECAY_TICKS;
        if (item == Items.GOLDEN_APPLE)           return GOLDEN_APPLE_DECAY_TICKS;
        if (item == Items.ENCHANTED_GOLDEN_APPLE) return ENCHANTED_APPLE_DECAY_TICKS;
        return 0;
    }

    // ── Server-side tick: stamp new items and decay expired ones ──────────────

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player.level() instanceof ServerLevel serverLevel)) return;

        // Check once per second to keep overhead negligible
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
                // First time we see this apple — stamp it
                tag.putLong(DECAY_TAG, now);
                continue;
            }

            long elapsed = now - tag.getLong(DECAY_TAG);
            if (elapsed < threshold) continue;

            // Time's up — replace with the next decay stage
            ItemStack replacement = getDecayReplacement(stack);
            inv.setItem(i, replacement);
        }
    }

    /** Returns what this apple decays INTO. */
    private static ItemStack getDecayReplacement(ItemStack original) {
        int count = original.getCount();
        if (original.getItem() == Items.APPLE) {
            return new ItemStack(ultimate_apple_modForge.ROTTEN_APPLE.get(), count);
        }
        if (original.getItem() == Items.GOLDEN_APPLE) {
            return new ItemStack(Items.APPLE, count);
        }
        if (original.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
            return new ItemStack(Items.GOLDEN_APPLE, count);
        }
        return ItemStack.EMPTY;
    }
}
