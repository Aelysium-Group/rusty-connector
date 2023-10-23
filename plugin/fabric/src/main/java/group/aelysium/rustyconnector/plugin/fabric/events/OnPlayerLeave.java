package group.aelysium.rustyconnector.plugin.fabric.events;

import group.aelysium.rustyconnector.core.central.Tinder;
import group.aelysium.rustyconnector.core.plugin.Plugin;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerLeave {

    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            Tinder api = Plugin.getAPI();

            api.services().dynamicTeleport().removeAllPlayersRequests(player.getUuid());
        });
    }
}