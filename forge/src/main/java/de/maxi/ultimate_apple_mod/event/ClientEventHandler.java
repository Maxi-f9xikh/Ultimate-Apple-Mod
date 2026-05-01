package de.maxi.ultimate_apple_mod.event;

import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * Client-only event handler.
 *
 * Responsibilities:
 *  1. Decay countdown tooltip on vanilla apples (apple / golden_apple / enchanted_golden_apple).
 *  2. Shift-sensitive tooltip on all Ultimate Apple Mod items:
 *       – Shift NOT held → item name + "Hold Shift for more info"
 *       – Shift held     → custom appendHoverText content + formatted food effects (if any)
 */
@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID,
                        bus   = Mod.EventBusSubscriber.Bus.FORGE,
                        value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tips = event.getToolTip();

        // ── 1. Decay countdown (vanilla apples) ──────────────────────────────
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(DecayEventHandler.DECAY_TAG)) {
            long threshold = DecayEventHandler.getDecayThreshold(stack.getItem());
            if (threshold > 0) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level != null) {
                    long decayStart = tag.getLong(DecayEventHandler.DECAY_TAG);
                    long remaining  = threshold - (mc.level.getGameTime() - decayStart);
                    if (remaining <= 0) {
                        tips.add(Component.literal("§cRots any moment now!"));
                    } else {
                        long totalSecs = remaining / 20;
                        // Color based on time remaining:
                        //   > 15 min → green  |  ≤ 15 min → yellow
                        //   ≤  5 min → orange |  ≤  1 min → red
                        String color;
                        if      (remaining > 20L * 60 * 15) color = "§a"; // green
                        else if (remaining > 20L * 60 *  5) color = "§e"; // yellow
                        else if (remaining > 20L * 60 *  1) color = "§6"; // orange
                        else                                 color = "§c"; // red
                        tips.add(Component.literal(
                            String.format("%sRots in: %d:%02d", color, totalSecs / 60, totalSecs % 60)));
                    }
                }
            }
        }

        // ── 2. Shift tooltip for Ultimate Apple Mod items ─────────────────────
        Item item = stack.getItem();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null || !id.getNamespace().equals(ultimate_apple_mod.MOD_ID)) return;

        // Exclude shake (complex NBT tooltip handled by ShakeItem) and cup (no info needed).
        String path = id.getPath();
        if (path.equals("shake") || path.equals("cup")) return;

        if (!Screen.hasShiftDown()) {
            // Suppress descriptive lines, show the hint instead
            while (tips.size() > 1) tips.remove(1);
            tips.add(Component.literal("§7Hold §eShift §7for more info")
                .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            // ── Mixer block: describe what it does ─────────────────────────
            if (path.equals("mixer")) {
                tips.add(Component.literal("Combine two apple items to brew a Shake.")
                    .withStyle(ChatFormatting.GRAY));
                tips.add(Component.literal("Effects from both ingredients are merged.")
                    .withStyle(ChatFormatting.GRAY));
                tips.add(Component.literal("Requires a Cup in the bottom slot.")
                    .withStyle(ChatFormatting.DARK_GRAY));
                tips.add(Component.literal("⊕ All effect durations receive a +20% bonus.")
                    .withStyle(ChatFormatting.DARK_GREEN));
                tips.add(Component.literal("⊕ Add a Longevity Apple to double all durations.")
                    .withStyle(ChatFormatting.DARK_GREEN));
                return;
            }

            // ── Food items: show formatted effect list ──────────────────────
            FoodProperties food = item.getFoodProperties(stack, null);
            if (food != null && !food.getEffects().isEmpty()) {
                tips.add(Component.literal("Effects:").withStyle(ChatFormatting.GOLD));
                for (var pair : food.getEffects()) {
                    tips.add(formatEffect(pair.getFirst()));
                }
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static Component formatEffect(MobEffectInstance eff) {
        int amp = eff.getAmplifier();
        int dur = eff.getDuration();
        MutableComponent line = Component.literal("  ")
            .append(eff.getEffect().getDisplayName().copy().withStyle(ChatFormatting.GRAY));
        if (amp > 0) {
            line.append(Component.literal(" " + toRoman(amp + 1))
                .withStyle(ChatFormatting.GRAY));
        }
        line.append(Component.literal(" (" + formatDuration(dur) + ")")
            .withStyle(ChatFormatting.DARK_GRAY));
        return line;
    }

    private static String toRoman(int n) {
        return switch (n) {
            case 2  -> "II";   case 3 -> "III"; case 4 -> "IV";
            case 5  -> "V";    case 6 -> "VI";  case 7 -> "VII";
            case 8  -> "VIII"; case 9 -> "IX";  case 10 -> "X";
            default -> String.valueOf(n);
        };
    }

    private static String formatDuration(int ticks) {
        int s = ticks / 20;
        if (s >= 60) {
            int m = s / 60;
            int r = s % 60;
            return r == 0 ? m + "m" : m + "m " + r + "s";
        }
        return s + "s";
    }
}
