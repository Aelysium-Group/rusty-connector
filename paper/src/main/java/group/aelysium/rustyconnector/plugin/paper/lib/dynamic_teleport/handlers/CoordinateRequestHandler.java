package group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.handlers;

import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageHandler;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageCoordinateRequestQueue;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.lang_messaging.PaperLang;
import group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.models.CoordinateRequest;
import org.bukkit.entity.Player;

public class CoordinateRequestHandler implements MessageHandler {
    private final RedisMessageCoordinateRequestQueue message;

    public CoordinateRequestHandler(GenericRedisMessage message) {
        this.message = (RedisMessageCoordinateRequestQueue) message;
    }

    @Override
    public void execute() {
        PaperAPI api = PaperAPI.get();

        Player target = api.paperServer().getPlayer(message.targetUsername());
        if(target == null) return;
        if(!target.isOnline()) return;

        CoordinateRequest coordinateRequest = api.services().dynamicTeleportService().newRequest(message.sourceUsername(), target);

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
