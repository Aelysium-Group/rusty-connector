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
        if(plugin.hasRegistered) plugin.logger().warn("This server has already registered itself! Re-registering anyways...");
        plugin.registerToProxy();
        PaperRustyConnector.getInstance().logger().log("Server has submitted it's registration request.");
    }
}
