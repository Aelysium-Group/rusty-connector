package group.aelysium.rustyconnector.plugin.paper.lib.message.handling;

import group.aelysium.rustyconnector.core.lib.data_messaging.MessageHandler;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessage;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.lang_messaging.PaperLang;
import group.aelysium.rustyconnector.plugin.paper.lib.tpa.TPARequest;
import org.bukkit.entity.Player;

public class TPAQueuePlayerHandler implements MessageHandler {
    private final RedisMessage message;

    public TPAQueuePlayerHandler(RedisMessage message) {
        this.message = message;
    }

    @Override
    public void execute() {
        PaperAPI api = PaperRustyConnector.getAPI();

        String targetUsername = message.getParameter("target-username");

        Player target = api.getServer().getPlayer(targetUsername);
        if(target == null) return;
        if(!target.isOnline()) return;

        String sourceUsername = message.getParameter("source-username");

        TPARequest tpaRequest = api.getVirtualProcessor().getTPAQueue().newRequest(sourceUsername, target);

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
