package de.maxi.ultimate_apple_mod.fabric.event;

import de.maxi.ultimate_apple_mod.item.TntAppleEntity;
import de.maxi.ultimate_apple_mod.item.TntAppleItem;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ghast;

public class FabricTntAppleHandler {

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof Ghast)) return;
            Entity directEntity = damageSource.getDirectEntity();
            if (!(directEntity instanceof TntAppleEntity tntApple)) return;
            Entity owner = tntApple.getOwner();
            if (!(owner instanceof ServerPlayer player)) return;
            TntAppleItem.grantAdvancement(player, "tnt_apple_ghast");
        });
    }
}
