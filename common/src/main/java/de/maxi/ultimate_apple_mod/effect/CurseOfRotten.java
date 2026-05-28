package de.maxi.ultimate_apple_mod.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class CurseOfRotten extends MobEffect {

    /** Check sun-burn once per second — same cadence Minecraft uses for zombie sunburn. */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    /**
     * While the curse is active and the sun is shining directly on the entity,
     * ignite them for 8 seconds — exactly like a zombie in daylight.
     */
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        Level level = entity.level();
        if (level.isClientSide()) return true;
        if (level.isDay()
                && level.canSeeSky(entity.blockPosition())
                && !entity.isInWater()
                && entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            entity.igniteForSeconds(8.0f);
        }
        return true;
    }

    public CurseOfRotten() {
        super(MobEffectCategory.HARMFUL, 0x7e5c3d);

        this.addAttributeModifier(
            Attributes.MOVEMENT_SPEED,
            ResourceLocation.fromNamespaceAndPath("ultimate_apple_mod", "curse_of_rotten_speed"),
            1.5D,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        this.addAttributeModifier(
            Attributes.MAX_HEALTH,
            ResourceLocation.fromNamespaceAndPath("ultimate_apple_mod", "curse_of_rotten_health"),
            20.0D,
            AttributeModifier.Operation.ADD_VALUE
        );
        this.addAttributeModifier(
            Attributes.ATTACK_SPEED,
            ResourceLocation.fromNamespaceAndPath("ultimate_apple_mod", "curse_of_rotten_attack_speed"),
            2.0D,
            AttributeModifier.Operation.ADD_VALUE
        );
    }

    @Override
    public void addAttributeModifiers(AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(attributeMap, amplifier);
    }

    @Override
    public void removeAttributeModifiers(AttributeMap attributeMap) {
        super.removeAttributeModifiers(attributeMap);
    }
}
