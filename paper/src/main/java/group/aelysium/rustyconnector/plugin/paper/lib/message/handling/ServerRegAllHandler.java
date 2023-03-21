package group.aelysium.rustyconnector.plugin.paper.lib.message.handling;


import group.aelysium.rustyconnector.core.lib.data_messaging.MessageHandler;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessage;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;

import java.util.Objects;

public class ServerRegAllHandler implements MessageHandler {
    private final RedisMessage message;

    public ServerRegAllHandler(RedisMessage message) {
        this.message = message;
    }

    @Override
    public void execute() {
        PaperRustyConnector plugin = PaperRustyConnector.getInstance();

        PaperRustyConnector.getInstance().logger().log("Server has been requested to register itself...");
        plugin.registerToProxy();
        PaperRustyConnector.getInstance().logger().log("Server has submitted its registration request.");
    }
}
