package de.maxi.ultimate_apple_mod.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Coal Apple — can be eaten (bad effects via FoodProperties), used as furnace
 * fuel (burns for 20 000 ticks = 100 items, same as a lava bucket), and mixed
 * in the Mixer for special combo behaviour.
 */
public class CoalAppleItem extends Item {

    /**
     * Burn time in ticks when used directly as furnace fuel.
     * 20 000 t = 100 items smelted (same as vanilla lava bucket).
     */
    public static final int BURN_TIME = 20_000;

    /**
     * Burn time of a coal-infused shake in ticks.
     * The mixing bonus grants +20 %, so 24 000 t = 120 items.
     */
    public static final int SHAKE_BURN_TIME = 24_000;

    public CoalAppleItem(Properties properties) {
        super(properties);
    }

    // ── Furnace fuel ─────────────────────────────────────────────────────────

    @Override
    public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
        return BURN_TIME;
    }

    // ── Tooltip ──────────────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("tooltip.ultimate_apple_mod.coal_apple.line1"));
        components.add(Component.translatable("tooltip.ultimate_apple_mod.coal_apple.line2"));
    }
}
