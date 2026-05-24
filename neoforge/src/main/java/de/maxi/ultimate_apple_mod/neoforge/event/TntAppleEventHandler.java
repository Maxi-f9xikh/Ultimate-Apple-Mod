package de.maxi.ultimate_apple_mod.neoforge.event;

import de.maxi.ultimate_apple_mod.item.TntAppleEntity;
import de.maxi.ultimate_apple_mod.item.TntAppleItem;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ghast;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.NEOFORGE)
public class TntAppleEventHandler {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Ghast)) return;

        Entity directEntity = event.getSource().getDirectEntity();
        if (!(directEntity instanceof TntAppleEntity tntApple)) return;

        Entity owner = tntApple.getOwner();
        if (!(owner instanceof ServerPlayer player)) return;

        TntAppleItem.grantAdvancement(player, "tnt_apple_ghast");
    }
}
