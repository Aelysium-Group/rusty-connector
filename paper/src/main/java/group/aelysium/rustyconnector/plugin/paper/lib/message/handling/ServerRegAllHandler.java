package group.aelysium.rustyconnector.plugin.paper.lib.message.handling;


import group.aelysium.rustyconnector.core.lib.data_messaging.MessageHandler;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessage;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;

import java.util.Objects;

public class ServerRegAllHandler implements MessageHandler {
    private final RedisMessage message;

    public ServerRegAllHandler(RedisMessage message) {
        this.message = message;
    }

    @Override
    public void execute() {
        PaperAPI api = PaperRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        logger.log("Server has been requested to register itself...");
        api.getVirtualProcessor().registerToProxy();
        logger.log("Server has submitted its registration request.");
    }
}
