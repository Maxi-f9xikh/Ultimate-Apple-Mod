package de.maxi.ultimate_apple_mod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CurseOfRotten extends MobEffect {

    public CurseOfRotten() {
        super(MobEffectCategory.HARMFUL, 0x7e5c3d); // Braun-Grün

        // +60% Speed (wie Babyzombie)
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                "7107DE5E-7CE8-4030-940E-514C1F160890",
                1.5D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        // +20 Health (10 Herzen mehr)
        this.addAttributeModifier(
                Attributes.MAX_HEALTH,
                "5D6F0BA2-1186-46AC-B896-C61C5CEE99CC",
                20.0D,
                AttributeModifier.Operation.ADDITION
        );

        // +2 Attack Speed
        this.addAttributeModifier(
                Attributes.ATTACK_SPEED,
                "3FA243A0-4953-4B13-801F-79B0F5D6A093",
                2.0D,
                AttributeModifier.Operation.ADDITION
        );
    }
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
