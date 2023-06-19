package group.aelysium.rustyconnector.plugin.paper.lib.message.handling;


import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageHandler;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageFamilyRegister;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.services.RedisMessagerService;
import group.aelysium.rustyconnector.plugin.paper.lib.services.ServerInfoService;

import java.util.Objects;

public class ServerRegFamilyHandler implements MessageHandler {
    private final RedisMessageFamilyRegister message;

    public ServerRegFamilyHandler(GenericRedisMessage message) {
        this.message = (RedisMessageFamilyRegister) message;
    }

    @Override
    public void execute() {
        PaperAPI api = PaperRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        if(Objects.equals(this.message.getFamilyName(), api.getService(ServerInfoService.class).getFamily())) {
            logger.log("Server has been requested to register itself...");
            api.getService(RedisMessagerService.class).registerToProxy();
            logger.log("Server has submitted its registration request.");
        }
    }
}
