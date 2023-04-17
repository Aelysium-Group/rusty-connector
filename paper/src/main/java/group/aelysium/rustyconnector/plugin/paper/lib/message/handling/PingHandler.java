package group.aelysium.rustyconnector.plugin.paper.lib.message.handling;

import group.aelysium.rustyconnector.core.lib.data_messaging.MessageHandler;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessage;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;

public class PingHandler implements MessageHandler {
    private final RedisMessage message;

    public PingHandler(RedisMessage message) {
        this.message = message;
    }

    @Override
    public void execute() {
        PaperAPI api = PaperRustyConnector.getAPI();

        api.getVirtualProcessor().pong();
    }
}
