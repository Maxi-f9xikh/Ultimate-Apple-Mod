package de.maxi.ultimate_apple_mod.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.phys.Vec3;

public class GlitchEffect extends MobEffect {

    private static final String ENTRY_KEY = "glitch_entry_pos";
    private static final double MAX_DEPTH = 3.0;

    public GlitchEffect() {
        super(MobEffectCategory.NEUTRAL, 0x00FF41);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        if (!entity.level().isClientSide()) {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("x", entity.getX());
            tag.putDouble("y", entity.getY());
            tag.putDouble("z", entity.getZ());
            entity.getPersistentData().put(ENTRY_KEY, tag);
            entity.noPhysics = true;
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return;
        entity.noPhysics = true;

        CompoundTag data = entity.getPersistentData();
        if (!data.contains(ENTRY_KEY)) return;

        CompoundTag entryTag = data.getCompound(ENTRY_KEY);
        Vec3 entry = new Vec3(
            entryTag.getDouble("x"),
            entryTag.getDouble("y"),
            entryTag.getDouble("z")
        );

        if (entity.isInWall() && entity.position().distanceTo(entry) > MAX_DEPTH) {
            entity.noPhysics = false;
            entity.teleportTo(entry.x, entry.y, entry.z);
            // Use addEffect with duration 1 to schedule safe expiry next tick,
            // avoiding ConcurrentModificationException on the activeEffects iterator.
            entity.addEffect(new MobEffectInstance(this, 1, amplifier, false, false));
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity.level().isClientSide()) return;
        entity.noPhysics = false;
        if (entity.isInWall()) {
            extractFromWall(entity);
        }
        entity.getPersistentData().remove(ENTRY_KEY);
    }

    private void extractFromWall(LivingEntity entity) {
        for (int dy = 1; dy <= 3; dy++) {
            BlockPos check = entity.blockPosition().above(dy);
            if (entity.level().isEmptyBlock(check) && entity.level().isEmptyBlock(check.above())) {
                entity.teleportTo(check.getX() + 0.5, check.getY(), check.getZ() + 0.5);
                return;
            }
        }
        for (int dy = 1; dy <= 3; dy++) {
            BlockPos check = entity.blockPosition().below(dy);
            if (entity.level().isEmptyBlock(check) && entity.level().isEmptyBlock(check.above())) {
                entity.teleportTo(check.getX() + 0.5, check.getY(), check.getZ() + 0.5);
                return;
            }
        }
    }
}
