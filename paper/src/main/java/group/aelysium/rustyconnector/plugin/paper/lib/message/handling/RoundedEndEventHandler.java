package group.aelysium.rustyconnector.plugin.paper.lib.message.handling;

import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageHandler;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;

public class RoundedEndEventHandler implements MessageHandler {
    private final GenericRedisMessage message;

    public RoundedEndEventHandler(GenericRedisMessage message) {
        this.message =  message;
    }

    @Override
    public void execute() {
        PaperAPI api = PaperRustyConnector.getAPI();
    }
}
