package group.aelysium.rustyconnector.core.plugin.lib.dynamic_teleport.handlers;

import group.aelysium.rustyconnector.api.mc_loader.central.MCLoaderTinder;
import group.aelysium.rustyconnector.core.lib.packets.PacketHandler;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.CoordinateRequestQueuePacket;
import group.aelysium.rustyconnector.core.plugin.Plugin;
import group.aelysium.rustyconnector.core.plugin.lib.lang.PluginLang;
import group.aelysium.rustyconnector.core.plugin.lib.dynamic_teleport.CoordinateRequest;

import java.util.UUID;

public class CoordinateRequestHandler extends PacketHandler {
    @Override
    public void execute(GenericPacket genericPacket) throws Exception {
        CoordinateRequestQueuePacket packet = (CoordinateRequestQueuePacket) genericPacket;
        MCLoaderTinder api = Plugin.getAPI();

        UUID target = api.getPlayerUUID(packet.targetUsername());
        if(target == null) return;
        if(!api.isOnline(target)) return;

        CoordinateRequest coordinateRequest = api.services().dynamicTeleport().newRequest(packet.sourceUsername(), target);

        // Attempt to resolve the tpa right away! If the player isn't on the server, this should fail silently.
        try {
            coordinateRequest.resolveClient();

            try {
                coordinateRequest.teleport();
            } catch (Exception e) {
                e.printStackTrace();
                api.sendMessage(coordinateRequest.client().orElseThrow(), PluginLang.TPA_FAILED_TELEPORT.build(Plugin.getAPI().getPlayerName(coordinateRequest.target())));
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
