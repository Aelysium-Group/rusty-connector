package group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.handlers;

import group.aelysium.rustyconnector.core.lib.packets.PacketHandler;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.CoordinateRequestQueuePacket;
import group.aelysium.rustyconnector.plugin.paper.central.Tinder;
import group.aelysium.rustyconnector.plugin.paper.lib.lang_messaging.PaperLang;
import group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.models.CoordinateRequest;
import org.bukkit.entity.Player;

public class CoordinateRequestHandler implements PacketHandler {
    private final CoordinateRequestQueuePacket message;

    public CoordinateRequestHandler(GenericPacket message) {
        this.message = (CoordinateRequestQueuePacket) message;
    }

    @Override
    public void execute() {
        Tinder api = Tinder.get();

        Player target = api.paperServer().getPlayer(message.targetUsername());
        if(target == null) return;
        if(!target.isOnline()) return;

        CoordinateRequest coordinateRequest = api.services().dynamicTeleport().newRequest(message.sourceUsername(), target);

        // Attempt to resolve the tpa right away! If the player isn't on the server, this should fail silently.
        try {
            coordinateRequest.resolveClient();

            try {
                coordinateRequest.teleport();
            } catch (Exception e) {
                e.printStackTrace();
                coordinateRequest.client().sendMessage(PaperLang.TPA_FAILED_TELEPORT.build(coordinateRequest.target().getPlayerProfile().getName()));
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
