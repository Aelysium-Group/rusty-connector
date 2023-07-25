package group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.handlers;

import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageHandler;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageTPAQueuePlayer;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.lang_messaging.PaperLang;
import group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.models.DynamicTeleport_TPARequest;
import org.bukkit.entity.Player;

import java.util.Objects;

public class TPAQueuePlayerHandler implements MessageHandler {
    private final RedisMessageTPAQueuePlayer message;

    public TPAQueuePlayerHandler(GenericRedisMessage message) {
        this.message = (RedisMessageTPAQueuePlayer) message;
    }

    @Override
    public void execute() {
        PaperAPI api = PaperAPI.get();

        if(!Objects.equals(this.message.getTargetServer(), api.services().serverInfoService().getAddress()))
            throw new IllegalStateException("Message is not addressed to me!");

        Player target = api.getServer().getPlayer(message.getTargetUsername());
        if(target == null) return;
        if(!target.isOnline()) return;

        DynamicTeleport_TPARequest tpaRequest = api.services().dynamicTeleportService().newRequest(message.getSourceUsername(), target);

        // Attempt to resolve the tpa right away! If the player isn't on the server, this should fail silently.
        try {
            tpaRequest.resolveClient();

            try {
                tpaRequest.teleport();
            } catch (Exception e) {
                tpaRequest.getClient().sendMessage(PaperLang.TPA_FAILED_TELEPORT.build(tpaRequest.getTarget().getPlayerProfile().getName()));
            }
        } catch (NullPointerException ignore) {}
    }
}
