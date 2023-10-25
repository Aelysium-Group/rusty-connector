package group.aelysium.rustyconnector.plugin.fabric.events;

import group.aelysium.rustyconnector.plugin.fabric.central.Tinder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerLeave {

    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            Tinder api = Tinder.get();

            api.services().dynamicTeleport().removeAllPlayersRequests(player.getUuid());
        });
    }
}