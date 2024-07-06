package group.aelysium.rustyconnector.plugin.fabric.events;

import group.aelysium.lib.dynamic_teleport.CoordinateRequest;
import group.aelysium.lib.lang.MCLoaderLang;
import group.aelysium.rustyconnector.plugin.fabric.central.Tinder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerJoin {

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            Tinder api = Tinder.get();

            CoordinateRequest tpaRequest = api.services().dynamicTeleport().findClient(player.getName().getString());
            if(tpaRequest == null) return;
            try {
                tpaRequest.resolveClient();

                try {
                    tpaRequest.teleport();
                } catch (NullPointerException e) {
                    player.sendMessage(MCLoaderLang.TPA_FAILED_TELEPORT.build(api.getPlayerName(tpaRequest.target())));
                }
            } catch (Exception e) {
                player.sendMessage(MCLoaderLang.TPA_FAILED_TELEPORT.build(api.getPlayerName(tpaRequest.target())));
            }

            api.services().dynamicTeleport().removeAllPlayersRequests(player.getUuid());
        });
    }
}