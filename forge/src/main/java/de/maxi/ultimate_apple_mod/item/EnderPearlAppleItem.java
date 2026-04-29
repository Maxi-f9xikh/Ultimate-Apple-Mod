package de.maxi.ultimate_apple_mod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnderPearlAppleItem extends Item {
    public EnderPearlAppleItem() {
        super(new Properties()
                .stacksTo(16)
                .food(new FoodProperties.Builder()
                        .nutrition(6)
                        .saturationMod(0.8f)
                        .alwaysEat()
                        .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 30, 2), 1.0f)
                        .build()));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            Vec3 start = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            Vec3 end = start.add(look.scale(256));

            BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

            if (hit.getType() == HitResult.Type.BLOCK) {
                // Berechne die Teleportationsposition basierend auf der Hitrichtung
                BlockPos hitPos = hit.getBlockPos();
                Vec3 hitVec = hit.getLocation();

                // Finde einen sicheren Teleportationsplatz
                BlockPos teleportPos = findSafeTeleportPosition(level, hitPos, hit.getDirection(), hitVec);

                if (teleportPos != null) {
                    // Teleportiere den Spieler
                    player.teleportTo(
                            teleportPos.getX() + 0.5,
                            teleportPos.getY(),
                            teleportPos.getZ() + 0.5
                    );

                    // Enderperlen-Schaden (2.5 Herzen)
                    player.hurt(level.damageSources().fall(), 5.0F);

                    // Sound und Effekte
                    level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    level.gameEvent(GameEvent.TELEPORT, player.position(), GameEvent.Context.of(player));

                    // Kurzer Cooldown
                    player.getCooldowns().addCooldown(this, 20);
                }
            }
        }

        return result;
    }

    private BlockPos findSafeTeleportPosition(Level level, BlockPos hitPos, net.minecraft.core.Direction hitDirection, Vec3 hitLocation) {
        // Versuche zuerst die Position direkt vor dem getroffenen Block
        BlockPos candidatePos = hitPos.relative(hitDirection);

        // Prüfe ob diese Position sicher ist (2 Blöcke hoch frei)
        if (isSafePosition(level, candidatePos)) {
            return candidatePos;
        }

        // Falls nicht sicher, suche in der Nähe nach einer sicheren Position
        for (int y = -3; y <= 3; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos testPos = candidatePos.offset(x, y, z);
                    if (isSafePosition(level, testPos)) {
                        return testPos;
                    }
                }
            }
        }

        // Als letzte Option, versuche eine Position in der Richtung des Hits zu finden
        Vec3 direction = Vec3.atLowerCornerOf(hitDirection.getNormal());
        for (int i = 1; i <= 5; i++) {
            BlockPos testPos = hitPos.offset(
                    (int)(direction.x * i),
                    0,
                    (int)(direction.z * i)
            );

            // Suche von oben nach unten nach einer sicheren Position
            for (int y = 10; y >= -10; y--) {
                BlockPos finalPos = testPos.offset(0, y, 0);
                if (isSafePosition(level, finalPos)) {
                    return finalPos;
                }
            }
        }

        return null; // Keine sichere Position gefunden
    }

    private boolean isSafePosition(Level level, BlockPos pos) {
        // Prüfe ob der Spieler hier stehen kann (2 Blöcke hoch)
        return level.isEmptyBlock(pos) &&
                level.isEmptyBlock(pos.above()) &&
                !level.isEmptyBlock(pos.below()); // Muss einen Block unter den Füßen haben
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ultimate_apple_mod.ender_pearl_apple.line1"));
        tooltip.add(Component.translatable("tooltip.ultimate_apple_mod.ender_pearl_apple.line2"));
    }
}