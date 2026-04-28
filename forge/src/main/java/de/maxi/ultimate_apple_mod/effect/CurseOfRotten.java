package de.maxi.ultimate_apple_mod.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CurseOfRotten extends MobEffect {

    public CurseOfRotten() {
        super(MobEffectCategory.HARMFUL, 0x7e5c3d);

        this.addAttributeModifier(
            Attributes.MOVEMENT_SPEED,
            "7107DE5E-7CE8-4030-940E-514C1F160890",
            1.5D,
            AttributeModifier.Operation.MULTIPLY_TOTAL
        );
        this.addAttributeModifier(
            Attributes.MAX_HEALTH,
            "5D6F0BA2-1186-46AC-B896-C61C5CEE99CC",
            20.0D,
            AttributeModifier.Operation.ADDITION
        );
        this.addAttributeModifier(
            Attributes.ATTACK_SPEED,
            "3FA243A0-4953-4B13-801F-79B0F5D6A093",
            2.0D,
            AttributeModifier.Operation.ADDITION
        );
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        entity.refreshDimensions();
        // Witch particles on application
        if (!entity.level().isClientSide() && entity.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.WITCH,
                entity.getX(), entity.getY() + 1.0, entity.getZ(),
                6, 0.3, 0.5, 0.3, 0.1);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        entity.refreshDimensions();
    }
}
