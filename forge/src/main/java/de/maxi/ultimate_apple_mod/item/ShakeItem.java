package de.maxi.ultimate_apple_mod.item;

import de.maxi.ultimate_apple_mod.event.RewindTracker;
import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Result of mixing two apple items in the Mixer.
 * Effects and special flags are stored in NBT and applied when drunk.
 * After drinking, an empty Cup is returned to the player's inventory.
 */
public class ShakeItem extends Item {

    public ShakeItem() {
        super(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(4)
                .saturationMod(0.5f)
                .alwaysEat()
                .build())
            .stacksTo(1));   // shakes never stack — each has unique NBT
    }

    // ── Use animation ───────────────────────────────────────────────────────

    /** Shows the potion-drinking animation instead of the eating-bite animation. */
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    // ── Throw (bomb shake) ───────────────────────────────────────────────────

    /**
     * If the shake has {@code isBomb=true}, throw it as a ShakeBombEntity
     * instead of drinking it. Returns the empty cup as the replacement item.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getTag();

        if (tag != null && tag.getBoolean("isBomb")) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL,
                0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));

            if (!level.isClientSide()) {
                ShakeBombEntity bomb = new ShakeBombEntity(player, level, stack.copy());
                bomb.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 1.0f);
                level.addFreshEntity(bomb);
            }

            // Consuming the shake always leaves a cup in hand (like drinking does)
            if (player.getAbilities().instabuild) {
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
            return InteractionResultHolder.sidedSuccess(
                new ItemStack(ultimate_apple_modForge.CUP_ITEM.get()), level.isClientSide());
        }

        // Not a bomb — drink normally
        return super.use(level, player, hand);
    }

    // ── Consumption ─────────────────────────────────────────────────────────

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        // Save NBT before super() decrements / empties the stack
        CompoundTag savedTag = stack.hasTag() ? stack.getTag().copy() : null;

        // Super handles: hunger/saturation restore, eat sound, stack decrement
        super.finishUsingItem(stack, level, entity);

        // Apply the shake's custom effects on the server
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            applyShakeEffects(savedTag, player, level);
        }

        // Return an empty cup so it stays in the player's hand / inventory
        return new ItemStack(ultimate_apple_modForge.CUP_ITEM.get());
    }

    private void applyShakeEffects(@Nullable CompoundTag tag, ServerPlayer player, Level level) {
        if (tag == null) return;

        // Cleanse: remove all active effects first (Honey Apple behaviour)
        if (tag.getBoolean("clearsEffects")) {
            player.removeAllEffects();
        }

        // Regular mob effects
        ListTag effectsList = tag.getList("effects", Tag.TAG_COMPOUND);
        for (int i = 0; i < effectsList.size(); i++) {
            CompoundTag et = effectsList.getCompound(i);
            ResourceLocation id = ResourceLocation.tryParse(et.getString("id"));
            if (id == null) continue;
            MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(id);
            if (effect != null) {
                player.addEffect(new MobEffectInstance(
                    effect, et.getInt("duration"), et.getInt("amplifier")));
            }
        }

        // Dragon breath charges
        int dragonCharges = tag.getInt("dragonCharges");
        if (dragonCharges > 0) {
            int existing = player.getPersistentData().getInt("dragonBreathCharges");
            player.getPersistentData().putInt("dragonBreathCharges", existing + dragonCharges);
            player.displayClientMessage(
                Component.translatable("message.ultimate_apple_mod.dragon_charges_added", dragonCharges),
                true);
        }

        // Lifesteal effect (60 s)
        if (tag.getBoolean("lifesteal")) {
            player.addEffect(new MobEffectInstance(
                ultimate_apple_modForge.LIFESTEAL_EFFECT.get(), 20 * 60, 0));
        }

        // Wither curse — applies Wither II to nearby mobs
        if (tag.getBoolean("witherCurse")) {
            AABB area = player.getBoundingBox().inflate(8.0);
            List<LivingEntity> mobs = level.getEntitiesOfClass(
                LivingEntity.class, area, e -> e != player && e instanceof Mob);
            for (LivingEntity mob : mobs) {
                mob.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 60, 1));
            }
            if (!mobs.isEmpty()) {
                player.displayClientMessage(
                    Component.translatable("message.ultimate_apple_mod.wither_curse_applied"),
                    true);
            }
        }

        // Void launch — same upward burst as eating the Void Apple directly
        if (tag.getBoolean("voidLaunch")) {
            Vec3 motion = player.getDeltaMovement();
            boolean falling = motion.y < -0.05;
            if (falling) {
                player.setDeltaMovement(motion.x * 0.2, 6.5, motion.z * 0.2);
            } else {
                player.setDeltaMovement(motion.x, 2.5, motion.z);
            }
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 15, 0));
            player.connection.send(new ClientboundSetEntityMotionPacket(
                player.getId(), player.getDeltaMovement()));
        }

        // Rewind: teleport back 5 seconds in position history
        if (tag.getBoolean("rewindEffect")) {
            Vec3 oldPos = RewindTracker.getPositionFiveSecondsAgo(player);
            if (oldPos != null) {
                player.teleportTo(oldPos.x, oldPos.y, oldPos.z);
                player.displayClientMessage(
                    Component.translatable("message.ultimate_apple_mod.rewind"), true);
            } else {
                player.displayClientMessage(
                    Component.translatable("message.ultimate_apple_mod.rewind_no_history"), true);
            }
        }

        // Orchard spawn: plant trees at the player's current location
        if (tag.getBoolean("orchardSpawn")) {
            if (level instanceof ServerLevel serverLevel) {
                OrchardCallerItem.plantTrees(
                    serverLevel, player.blockPosition(), serverLevel.getRandom(), 6);
            }
        }

        // Ender teleport: look-direction teleport up to 256 blocks
        if (tag.getBoolean("enderTeleport")) {
            ShakeBombEntity.performEnderTeleport(player);
        }
    }

    // ── Tooltip ─────────────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> components, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag == null || tag.isEmpty()) {
            components.add(Component.translatable("tooltip.ultimate_apple_mod.shake.no_effects")
                .withStyle(ChatFormatting.GRAY));
            return;
        }

        ListTag effectsList = tag.getList("effects", Tag.TAG_COMPOUND);
        if (!effectsList.isEmpty()) {
            components.add(Component.translatable("tooltip.ultimate_apple_mod.shake.effects")
                .withStyle(ChatFormatting.GOLD));
            for (int i = 0; i < effectsList.size(); i++) {
                CompoundTag et = effectsList.getCompound(i);
                ResourceLocation id = ResourceLocation.tryParse(et.getString("id"));
                if (id == null) continue;
                MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(id);
                if (effect == null) continue;

                int amp = et.getInt("amplifier");
                int dur = et.getInt("duration");
                MutableComponent line = Component.literal("  ")
                    .append(effect.getDisplayName().copy().withStyle(ChatFormatting.GRAY));
                if (amp > 0) {
                    line.append(Component.literal(" " + toRoman(amp + 1))
                        .withStyle(ChatFormatting.GRAY));
                }
                line.append(Component.literal(" (" + formatDuration(dur) + ")")
                    .withStyle(ChatFormatting.DARK_GRAY));
                components.add(line);
            }
        }

        int dragonCharges = tag.getInt("dragonCharges");
        if (dragonCharges > 0) {
            components.add(Component.literal("§6+" + dragonCharges + " Dragon Breath Charge(s)"));
        }
        if (tag.getBoolean("lifesteal")) {
            components.add(Component.literal("§c♥ Lifesteal (60s)"));
        }
        if (tag.getBoolean("witherCurse")) {
            components.add(Component.literal("§5Wither Curse on nearby mobs"));
        }
        if (tag.getBoolean("clearsEffects")) {
            components.add(Component.literal("§bClears all active effects"));
        }
        if (tag.getBoolean("voidLaunch")) {
            components.add(Component.literal("§9⬆ Void Launch on drink!"));
        }
        if (tag.getBoolean("rewindEffect")) {
            components.add(Component.literal("§b⏪ Rewinds you 5 seconds on drink"));
        }
        if (tag.getBoolean("orchardSpawn")) {
            components.add(Component.literal("§2🌳 Spawns up to 6 trees on drink"));
        }
        if (tag.getBoolean("enderTeleport")) {
            components.add(Component.literal("§5⚡ Ender-teleports you on drink"));
        }
        if (tag.getBoolean("isBomb")) {
            components.add(Component.literal("§c⚠ Right-click to throw!").withStyle(ChatFormatting.RED));
            components.add(Component.literal("§7Applies effects on impact.").withStyle(ChatFormatting.GRAY));
        } else {
            components.add(Component.literal("§8(+20% duration from mixing)").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

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
