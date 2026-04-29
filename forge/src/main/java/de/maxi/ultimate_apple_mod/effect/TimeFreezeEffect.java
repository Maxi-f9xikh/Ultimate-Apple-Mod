package de.maxi.ultimate_apple_mod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;

import java.util.List;

/**
 * Time Freeze: the player moves at 3× speed and attacks 2.5× faster while
 * every nearby hostile mob is slowed to a crawl.
 *
 * Implementation:
 *  - Attribute modifiers boost the player's movement and attack speed.
 *  - applyEffectTick (every 5 ticks / 0.25 s) re-applies extreme Slowness
 *    and hidden Haste to all hostile mobs and the player within 12 blocks.
 */
public class TimeFreezeEffect extends MobEffect {

    public TimeFreezeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x00CCFF); // electric blue

        // Player runs at 3× normal speed  (MULTIPLY_TOTAL +2.0 → base * 3.0)
        this.addAttributeModifier(
            Attributes.MOVEMENT_SPEED,
            "F9E6F56B-C834-4E6C-9A80-E453F49A3D3A",
            2.0D,
            AttributeModifier.Operation.MULTIPLY_TOTAL
        );
        // Player attacks 2.5× faster (MULTIPLY_TOTAL +1.5 → base * 2.5)
        this.addAttributeModifier(
            Attributes.ATTACK_SPEED,
            "B2D6B491-E5A7-4B92-A31E-C9F79B7FAD4A",
            1.5D,
            AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    /** Fire every 5 ticks (0.25 s) to keep nearby mobs frozen. */
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 5 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return;

        // Refresh hidden Haste III so the player mines faster
        entity.addEffect(new MobEffectInstance(
            MobEffects.DIG_SPEED, 10, 2, false, false, false));

        // Freeze all nearby hostile mobs (10 % of normal speed)
        List<LivingEntity> mobs = entity.level().getEntitiesOfClass(
            LivingEntity.class,
            entity.getBoundingBox().inflate(12.0),
            e -> e != entity && e instanceof Enemy
        );
        for (LivingEntity mob : mobs) {
            mob.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, 10, 6,
                false, false, false  // hidden — just works silently
            ));
        }
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        if (!entity.level().isClientSide()) {
            // Give Haste III for the full 30 s duration + a small buffer
            entity.addEffect(new MobEffectInstance(
                MobEffects.DIG_SPEED, 620, 2, false, false, false));
            // Freeze everything immediately on first tick
            applyEffectTick(entity, amplifier);
        }
    }
}
