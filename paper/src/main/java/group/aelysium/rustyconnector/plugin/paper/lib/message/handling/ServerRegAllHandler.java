package group.aelysium.rustyconnector.plugin.paper.lib.message.handling;


import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageHandler;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.services.RedisMessagerService;

public class ServerRegAllHandler implements MessageHandler {
    private final GenericRedisMessage message;

    public ServerRegAllHandler(GenericRedisMessage message) {
        this.message = message;
    }

    @Override
    public void execute() {
        PaperAPI api = PaperRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        logger.log("Server has been requested to register itself...");
        api.getService(RedisMessagerService.class).registerToProxy();
        logger.log("Server has submitted its registration request.");
    }
}
