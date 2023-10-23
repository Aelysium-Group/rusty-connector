package group.aelysium.rustyconnector.plugin.fabric.events;

import group.aelysium.rustyconnector.core.central.Tinder;
import group.aelysium.rustyconnector.core.plugin.Plugin;
import group.aelysium.rustyconnector.core.plugin.lib.dynamic_teleport.models.CoordinateRequest;
import group.aelysium.rustyconnector.core.plugin.lib.lang.PluginLang;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerJoin {

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            Tinder api = Plugin.getAPI();

            CoordinateRequest tpaRequest = api.services().dynamicTeleport().findClient(player.getName().getString());
            if(tpaRequest == null) return;
            try {
                tpaRequest.resolveClient();

                try {
                    tpaRequest.teleport();
                } catch (NullPointerException e) {
                    player.sendMessage(PluginLang.TPA_FAILED_TELEPORT.build(api.getPlayerName(tpaRequest.target())));
                }
            } catch (Exception e) {
                player.sendMessage(PluginLang.TPA_FAILED_TELEPORT.build(api.getPlayerName(tpaRequest.target())));
            }

            api.services().dynamicTeleport().removeAllPlayersRequests(player.getUuid());
        });
    }
}