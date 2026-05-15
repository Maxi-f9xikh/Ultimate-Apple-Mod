package de.maxi.ultimate_apple_mod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
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
 *   <li>copper_apple            — full effects (Haste II, Strength I, Resistance I)</li>
 *   <li>exposed_copper_apple    — reduced (Haste I, Strength I)</li>
 *   <li>weathered_copper_apple  — minimal (Haste I short)</li>
 *   <li>oxidized_copper_apple   — negative (Slowness I, Weakness I)</li>
 * </ol>
 *
 * <p>Oxidation is probabilistic — no NBT is stored, so items of the same
 * stage always stack perfectly.  Each apple independently has a small
 * random chance to advance every second (expected: 15–20 minutes per stage).
 * Rain doubles the per-second probability.  Waxed variants never advance.
 *
 * <p>When a single apple in a stack triggers, exactly ONE item is consumed
 * and one next-stage item is added to the player's inventory, leaving the
 * rest of the stack unchanged.
 */
public class CopperAppleItem extends Item {

    /** Minimum dry seconds before a stage can advance (~40 min). */
    public static final int OXIDATION_MIN_SECONDS = 2_400;
    /** Maximum dry seconds before a stage can advance (~60 min). */
    public static final int OXIDATION_MAX_SECONDS = 3_600;

    /** Average seconds used for probability calculation. */
    private static final double OXIDATION_AVG_SECONDS =
        (OXIDATION_MIN_SECONDS + OXIDATION_MAX_SECONDS) / 2.0; // 1050 s

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

    public int getStage()    { return stage; }
    public boolean isWaxed() { return waxed; }

    // ── Oxidation tick ──────────────────────────────────────────────────────

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        // Only server-side, only unwaxed, only stages that can still advance
        if (level.isClientSide || waxed || stage >= 3) return;
        if (!(entity instanceof Player player)) return;
        // Sample once per second to keep overhead low
        if (level.getGameTime() % 20 != 0) return;

        // Probabilistic approach — no NBT stored.
        // P(one specific apple oxidizes this second) = 1 / avgSeconds
        // Rain counts double, so the apple effectively ages 2 s per real second.
        double p = 1.0 / OXIDATION_AVG_SECONDS;
        if (player.level().isRainingAt(player.blockPosition())) p *= 2.0;

        // P(at least one apple in a stack of N fires) = 1 - (1-p)^N
        int count = stack.getCount();
        double pAny = 1.0 - Math.pow(1.0 - p, count);

        if (level.getRandom().nextDouble() < pAny) {
            ResourceLocation nextId = new ResourceLocation(
                "ultimate_apple_mod", NEXT_STAGE_IDS[stage]);
            Item nextItem = ForgeRegistries.ITEMS.getValue(nextId);
            if (nextItem != null && nextItem != net.minecraft.world.item.Items.AIR) {
                // Oxidise exactly ONE apple — shrink the stack by one…
                stack.shrink(1);
                // …then hand the player a single next-stage apple.
                ItemStack nextStack = new ItemStack(nextItem, 1);
                if (!player.getInventory().add(nextStack)) {
                    player.drop(nextStack, false);
                }
                player.displayClientMessage(
                    Component.translatable(OXIDATION_MESSAGES[stage]), true);
            }
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
            components.add(Component.translatable(
                "tooltip.ultimate_apple_mod.copper_apple.time_to_oxidize")
                .withStyle(ChatFormatting.GRAY));
            if (Screen.hasShiftDown()) {
                components.add(Component.translatable(
                    "tooltip.ultimate_apple_mod.copper_apple.oxidizes_slowly")
                    .withStyle(ChatFormatting.GRAY));
                components.add(Component.translatable(
                    "tooltip.ultimate_apple_mod.copper_apple.rain_doubles")
                    .withStyle(ChatFormatting.GRAY));
                components.add(Component.translatable(
                    "tooltip.ultimate_apple_mod.copper_apple.wax_to_stop")
                    .withStyle(ChatFormatting.DARK_GRAY));
            }
        } else {
            components.add(Component.translatable(
                "tooltip.ultimate_apple_mod.copper_apple.fully_oxidized")
                .withStyle(ChatFormatting.DARK_GREEN));
        }
    }
}
