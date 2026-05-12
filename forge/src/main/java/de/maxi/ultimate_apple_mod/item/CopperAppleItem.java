package de.maxi.ultimate_apple_mod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Copper Apple — oxidises over time in the player's inventory.
 *
 * <p>Four stages (stage 0–3):
 * <ol>
 *   <li>copper_apple       — full effects (Haste II, Strength I, Resistance I)</li>
 *   <li>exposed_copper_apple   — reduced (Haste I, Strength I)</li>
 *   <li>weathered_copper_apple — minimal (Haste I short)</li>
 *   <li>oxidized_copper_apple  — negative (Slowness I, Weakness I)</li>
 * </ol>
 *
 * <p>Progress is stored as NBT key {@code oxidationTicks} on the ItemStack.
 * The counter increments once per second; rain doubles the rate.
 * Waxed variants (waxed=true) never advance.
 */
public class CopperAppleItem extends Item {

    /**
     * Seconds of dry inventory time before one oxidation stage advances.
     * 1 200 s = 20 real-time minutes.  Rain counts as 2 s per real second.
     */
    public static final int OXIDATION_THRESHOLD = 1_200;

    private static final String[] NEXT_STAGE_IDS = {
        "exposed_copper_apple",
        "weathered_copper_apple",
        "oxidized_copper_apple"
    };

    private static final String[] OXIDATION_MESSAGES = {
        "message.ultimate_apple_mod.copper_apple.stage1",
        "message.ultimate_apple_mod.copper_apple.stage2",
        "message.ultimate_apple_mod.copper_apple.stage3"
    };

    private final int stage;    // 0 = fresh copper, 3 = fully oxidised
    private final boolean waxed;

    public CopperAppleItem(Properties properties, int stage, boolean waxed) {
        super(properties);
        this.stage = stage;
        this.waxed = waxed;
    }

    public int getStage()   { return stage; }
    public boolean isWaxed(){ return waxed; }

    // ── Oxidation tick ──────────────────────────────────────────────────────

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        // Only server-side, only unwaxed, only stages that can still advance
        if (level.isClientSide || waxed || stage >= 3) return;
        if (!(entity instanceof Player player)) return;
        // Sample once per second to keep overhead low
        if (level.getGameTime() % 20 != 0) return;

        CompoundTag tag = stack.getOrCreateTag();
        int ticks = tag.getInt("oxidationTicks");
        ticks += player.isInRain() ? 2 : 1;

        if (ticks >= OXIDATION_THRESHOLD) {
            // Advance one stage
            ResourceLocation nextId = new ResourceLocation(
                "ultimate_apple_mod", NEXT_STAGE_IDS[stage]);
            Item nextItem = ForgeRegistries.ITEMS.getValue(nextId);
            if (nextItem != null && nextItem != net.minecraft.world.item.Items.AIR) {
                ItemStack nextStack = new ItemStack(nextItem, stack.getCount());
                player.getInventory().setItem(slot, nextStack);
                player.displayClientMessage(
                    Component.translatable(OXIDATION_MESSAGES[stage]), true);
            }
        } else {
            tag.putInt("oxidationTicks", ticks);
        }
    }

    // ── Tooltip ─────────────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> components, TooltipFlag flag) {
        if (waxed) {
            components.add(Component.translatable(
                "tooltip.ultimate_apple_mod.copper_apple.waxed")
                .withStyle(ChatFormatting.YELLOW));
        } else if (stage < 3) {
            // Show oxidation progress as a percentage
            int ticks   = stack.hasTag() ? stack.getTag().getInt("oxidationTicks") : 0;
            int percent = (int)(ticks * 100.0 / OXIDATION_THRESHOLD);
            components.add(Component.translatable(
                "tooltip.ultimate_apple_mod.copper_apple.oxidation", percent)
                .withStyle(ChatFormatting.GRAY));
        } else {
            components.add(Component.translatable(
                "tooltip.ultimate_apple_mod.copper_apple.fully_oxidized")
                .withStyle(ChatFormatting.DARK_GREEN));
        }
    }

    // ── Barcode (optional): show oxidation as durability-style bar ──────────

    /**
     * Show a faint green "oxidation" bar on the item icon — only for non-waxed
     * apples that are progressing toward the next stage.
     */
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !waxed && stage < 3
            && stack.hasTag()
            && stack.getTag().getInt("oxidationTicks") > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int ticks = stack.hasTag() ? stack.getTag().getInt("oxidationTicks") : 0;
        return Math.round(13.0f * ticks / OXIDATION_THRESHOLD);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        // Teal colour matching oxidised copper
        return 0x4EC994;
    }
}
