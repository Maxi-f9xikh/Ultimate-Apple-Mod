package de.maxi.ultimate_apple_mod.fabric.event;

import de.maxi.ultimate_apple_mod.ModRegistries;
import de.maxi.ultimate_apple_mod.event.DecayHelper;
import de.maxi.ultimate_apple_mod.fabric.FabricModClient;
import de.maxi.ultimate_apple_mod.network.FireDragonBreathPayload;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class FabricClientHandler {

    private static boolean wasRottenActive = false;

    public static void register() {

        // ── Tooltip event ─────────────────────────────────────────────────────
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            // 1. Decay countdown for vanilla apples
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains(DecayHelper.DECAY_TAG)) {
                long threshold = DecayHelper.getDecayThreshold(stack.getItem());
                if (threshold > 0) {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.level != null) {
                        long remaining = threshold - (mc.level.getGameTime() - tag.getLong(DecayHelper.DECAY_TAG));
                        if (remaining <= 0) {
                            lines.add(Component.literal("§cRots any moment now!"));
                        } else {
                            long s = remaining / 20;
                            String color = remaining > 20L*60*15 ? "§a"
                                : remaining > 20L*60*5 ? "§e"
                                : remaining > 20L*60 ? "§6" : "§c";
                            lines.add(Component.literal(String.format("%sRots in: %d:%02d", color, s/60, s%60)));
                        }
                    }
                }
            }

            // 2. Shift tooltip for mod items
            Item item = stack.getItem();
            var id = BuiltInRegistries.ITEM.getKey(item);
            if (id == null || !id.getNamespace().equals(ultimate_apple_mod.MOD_ID)) return;
            String path = id.getPath();
            if (path.equals("shake") || path.equals("cup")) return;

            if (!Screen.hasShiftDown()) {
                while (lines.size() > 1) lines.remove(1);
                lines.add(Component.literal("§7Hold §eShift §7for more info").withStyle(ChatFormatting.DARK_GRAY));
            } else {
                if (path.equals("mixer")) {
                    lines.add(Component.literal("Combine two apple items to brew a Shake.").withStyle(ChatFormatting.GRAY));
                    lines.add(Component.literal("Effects from both ingredients are merged.").withStyle(ChatFormatting.GRAY));
                    lines.add(Component.literal("Requires a Cup in the bottom slot.").withStyle(ChatFormatting.DARK_GRAY));
                    lines.add(Component.literal("⊕ All effect durations receive a +20% bonus.").withStyle(ChatFormatting.DARK_GREEN));
                    lines.add(Component.literal("⊕ Add a Longevity Apple to double all durations.").withStyle(ChatFormatting.DARK_GREEN));
                    return;
                }
                FoodProperties food = item.getFoodProperties();
                if (food != null && !food.getEffects().isEmpty()) {
                    lines.add(Component.literal("Effects:").withStyle(ChatFormatting.GOLD));
                    for (var pair : food.getEffects()) lines.add(formatEffect(pair.getFirst()));
                }
            }
        });

        // ── Client tick: keybind polling + CurseOfRotten pose fix ────────────
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            Player player = client.player;

            // Dragon breath keybind
            while (FabricModClient.FIRE_DRAGON_BREATH_KEY.consumeClick()) {
                boolean aimingAtEntity = client.hitResult instanceof net.minecraft.world.phys.EntityHitResult;
                var mainHand = player.getMainHandItem();
                boolean holdingMelee = mainHand.getItem() instanceof net.minecraft.world.item.SwordItem
                    || mainHand.getItem() instanceof net.minecraft.world.item.AxeItem;
                if (aimingAtEntity && holdingMelee) continue;
                // Fabric 1.20.1 channel-based packet send (no CustomPacketPayload)
                ClientPlayNetworking.send(FireDragonBreathPayload.CHANNEL, PacketByteBufs.empty());
            }

            // CurseOfRotten client-side dimension refresh + pose fix
            boolean isRottenActive = false;
            try { isRottenActive = player.hasEffect(ModRegistries.CURSE_OF_ROTTEN.get()); }
            catch (NullPointerException ignored) {}
            if (isRottenActive != wasRottenActive) {
                player.refreshDimensions();
                wasRottenActive = isRottenActive;
            }
            if (isRottenActive && player.getPose() == Pose.SWIMMING && !player.isInWater()) {
                player.setPose(Pose.STANDING);
            }
        });
    }

    private static Component formatEffect(MobEffectInstance eff) {
        int amp = eff.getAmplifier();
        int dur = eff.getDuration();
        MutableComponent line = Component.literal("  ")
            .append(eff.getEffect().getDisplayName().copy().withStyle(ChatFormatting.GRAY));
        if (amp > 0) line.append(Component.literal(" " + toRoman(amp + 1)).withStyle(ChatFormatting.GRAY));
        line.append(Component.literal(" (" + formatDuration(dur) + ")").withStyle(ChatFormatting.DARK_GRAY));
        return line;
    }

    private static String toRoman(int n) {
        return switch (n) { case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; case 5 -> "V";
            case 6 -> "VI"; case 7 -> "VII"; case 8 -> "VIII"; case 9 -> "IX"; case 10 -> "X";
            default -> String.valueOf(n); };
    }

    private static String formatDuration(int ticks) {
        int s = ticks / 20;
        if (s >= 60) { int m = s/60; int r = s%60; return r == 0 ? m+"m" : m+"m "+r+"s"; }
        return s + "s";
    }
}
