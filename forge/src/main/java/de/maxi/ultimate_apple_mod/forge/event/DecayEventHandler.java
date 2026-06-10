package de.maxi.ultimate_apple_mod.forge.event;

import de.maxi.ultimate_apple_mod.event.DecayHelper;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import de.maxi.ultimate_apple_mod.util.NbtCompat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

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
@EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class DecayEventHandler {

    // Constants delegated to the common DecayHelper
    public static final String DECAY_TAG = DecayHelper.DECAY_TAG;

    public static final long APPLE_DECAY_TICKS              = DecayHelper.APPLE_DECAY_TICKS;
    public static final long GOLDEN_APPLE_DECAY_TICKS       = DecayHelper.GOLDEN_APPLE_DECAY_TICKS;
    public static final long ENCHANTED_APPLE_DECAY_TICKS    = DecayHelper.ENCHANTED_APPLE_DECAY_TICKS;

    /** Returns the decay threshold in ticks for the given item, or 0 if it does not decay. */
    public static long getDecayThreshold(net.minecraft.world.item.Item item) {
        return DecayHelper.getDecayThreshold(item);
    }

    // ── Server-side tick: stamp new items and decay expired ones ──────────────

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;

        // Check once per second to keep overhead negligible
        if (serverLevel.getGameTime() % 20 != 0) return;

        Player player = event.getEntity();
        long now = serverLevel.getGameTime();
        Inventory inv = player.getInventory();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            long threshold = getDecayThreshold(stack.getItem());
            if (threshold == 0) continue;

            CompoundTag tag = NbtCompat.getOrCreateTag(stack);

            if (!tag.contains(DECAY_TAG)) {
                // First time we see this apple — stamp it
                tag.putLong(DECAY_TAG, now);
                NbtCompat.setTag(stack, tag);
                continue;
            }

            long elapsed = now - tag.getLong(DECAY_TAG);
            if (elapsed < threshold) continue;

            // Time's up — replace with the next decay stage
            // (enchanted golden → golden → apple → rotten apple)
            inv.setItem(i, DecayHelper.getDecayReplacement(stack));
        }
    }
}
