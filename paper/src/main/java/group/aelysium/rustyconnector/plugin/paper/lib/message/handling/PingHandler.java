package group.aelysium.rustyconnector.plugin.paper.lib.message.handling;

import group.aelysium.rustyconnector.core.lib.data_messaging.MessageHandler;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessage;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;

public class PingHandler implements MessageHandler {
    private final RedisMessage message;

    public PingHandler(RedisMessage message) {
        this.message = message;
    }

    @Override
    public void execute() {
        PaperRustyConnector plugin = PaperRustyConnector.getInstance();

        plugin.getVirtualServer().pong();
    }
}
