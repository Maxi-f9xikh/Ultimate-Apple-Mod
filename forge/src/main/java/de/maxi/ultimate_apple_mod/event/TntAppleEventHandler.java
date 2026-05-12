package de.maxi.ultimate_apple_mod.event;

import de.maxi.ultimate_apple_mod.item.TntAppleEntity;
import de.maxi.ultimate_apple_mod.item.TntAppleItem;
import de.maxi.ultimate_apple_mod.ultimate_apple_mod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ultimate_apple_mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TntAppleEventHandler {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // We only care about Ghasts being killed
        if (!(event.getEntity() instanceof Ghast)) return;

        // The explosion from TntAppleEntity is created via level().explode(this, ...)
        // so the damage source's direct entity is the TntAppleEntity
        Entity directEntity = event.getSource().getDirectEntity();
        if (!(directEntity instanceof TntAppleEntity tntApple)) return;

        // The owner of the projectile is the player who threw it
        Entity owner = tntApple.getOwner();
        if (!(owner instanceof ServerPlayer player)) return;

        TntAppleItem.grantAdvancement(player, "tnt_apple_ghast");
    }
}
