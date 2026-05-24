package de.maxi.ultimate_apple_mod.neoforge.event;

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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.registries.ForgeRegistries;

import java.util.List;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID,
                        bus   = Mod.EventBusSubscriber.Bus.NEOFORGE,
                        value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tips = event.getToolTip();

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
                        String color;
                        if      (remaining > 20L * 60 * 15) color = "§a";
                        else if (remaining > 20L * 60 *  5) color = "§e";
                        else if (remaining > 20L * 60 *  1) color = "§6";
                        else                                 color = "§c";
                        tips.add(Component.literal(
                            String.format("%sRots in: %d:%02d", color, totalSecs / 60, totalSecs % 60)));
                    }
                }
            }
        }

        Item item = stack.getItem();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null || !id.getNamespace().equals(ultimate_apple_mod.MOD_ID)) return;

        String path = id.getPath();
        if (path.equals("shake") || path.equals("cup")) return;

        if (!Screen.hasShiftDown()) {
            while (tips.size() > 1) tips.remove(1);
            tips.add(Component.literal("§7Hold §eShift §7for more info")
                .withStyle(ChatFormatting.DARK_GRAY));
        } else {
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

            FoodProperties food = item.getFoodProperties(stack, null);
            if (food != null && !food.getEffects().isEmpty()) {
                tips.add(Component.literal("Effects:").withStyle(ChatFormatting.GOLD));
                for (var pair : food.getEffects()) {
                    tips.add(formatEffect(pair.getFirst()));
                }
            }
        }
    }

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
