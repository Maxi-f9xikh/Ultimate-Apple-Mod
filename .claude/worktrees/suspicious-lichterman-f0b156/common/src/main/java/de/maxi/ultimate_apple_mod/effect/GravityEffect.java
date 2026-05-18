package de.maxi.ultimate_apple_mod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class GravityEffect extends MobEffect {

    public GravityEffect() {
        super(MobEffectCategory.NEUTRAL, 0x9B00FF);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        if (!entity.level().isClientSide()) {
            entity.setNoGravity(true);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (!entity.level().isClientSide()) {
            entity.setNoGravity(false);
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return;
        // 0.08 = vanilla gravity constant, 0.4 = vanilla terminal velocity (mirrored upward)
        var movement = entity.getDeltaMovement();
        entity.setDeltaMovement(movement.x, Math.min(movement.y + 0.08, 0.4), movement.z);
    }
}
