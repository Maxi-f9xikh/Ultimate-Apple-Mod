package de.maxi.ultimate_apple_mod.forge;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ultimate_apple_mod.MOD_ID);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
