package group.aelysium.rustyconnector.plugin.paper.lib.message.handling;


import group.aelysium.rustyconnector.core.lib.message.MessageHandler;
import group.aelysium.rustyconnector.core.lib.message.RedisMessage;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;

import java.security.InvalidAlgorithmParameterException;

public class ServerRegHandler implements MessageHandler {
    private final RedisMessage message;

    public ServerRegHandler(RedisMessage message) {
        this.message = message;
    }

    @Override
    public void execute() throws InvalidAlgorithmParameterException {
        PaperRustyConnector plugin = PaperRustyConnector.getInstance();

        PaperRustyConnector.getInstance().logger().log("Server has been requested to register itself...");
        plugin.registerToProxy();
        PaperRustyConnector.getInstance().logger().log("Server has submitted its registration request.");
    }
}
