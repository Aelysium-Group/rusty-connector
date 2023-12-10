package group.aelysium.rustyconnector.core.mcloader.lib.dynamic_teleport.handlers;

import group.aelysium.rustyconnector.toolkit.core.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.MCLoaderTinder;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.packet.variants.CoordinateRequestQueuePacket;
import group.aelysium.rustyconnector.core.TinderAdapterForCore;
import group.aelysium.rustyconnector.core.mcloader.lib.lang.MCLoaderLang;
import group.aelysium.rustyconnector.core.mcloader.lib.dynamic_teleport.CoordinateRequest;

import java.util.UUID;

public class CoordinateRequestListener implements PacketListener {
    protected MCLoaderTinder api;

    public CoordinateRequestListener(MCLoaderTinder api) {
        this.api = api;
    }

    public PacketType.Mapping identifier() {
        return PacketType.COORDINATE_REQUEST_QUEUE;
    }

    @Override
    public <TPacket extends IPacket> void execute(TPacket genericPacket) throws Exception {
        CoordinateRequestQueuePacket packet = (CoordinateRequestQueuePacket) genericPacket;

        UUID target = api.getPlayerUUID(packet.targetUsername());
        if(target == null) return;
        if(!api.isOnline(target)) return;

        CoordinateRequest coordinateRequest = (CoordinateRequest) api.services().dynamicTeleport().newRequest(packet.sourceUsername(), target);

        // Attempt to resolve the tpa right away! If the player isn't on the server, this should fail silently.
        try {
            coordinateRequest.resolveClient();

            try {
                coordinateRequest.teleport();
            } catch (Exception e) {
                e.printStackTrace();
                api.sendMessage(coordinateRequest.client().orElseThrow(), MCLoaderLang.TPA_FAILED_TELEPORT.build(TinderAdapterForCore.getTinder().getPlayerName(coordinateRequest.target())));
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
