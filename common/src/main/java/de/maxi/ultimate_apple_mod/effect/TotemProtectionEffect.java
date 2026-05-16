package de.maxi.ultimate_apple_mod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Marker effect granted by the Totem Apple.
 * When the player would die while carrying this effect, death is cancelled in
 * PlayerEffectEventHandler.onPlayerTotemDeath() and the effect is consumed.
 * Duration is extremely long so it persists across server restarts / relogs.
 */
public class TotemProtectionEffect extends MobEffect {

    public TotemProtectionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFD700); // gold
    }
}
