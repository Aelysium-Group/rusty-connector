package group.aelysium.rustyconnector.plugin.paper.lib.message.handling;

import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageHandler;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.GenericRedisMessage;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;

public class PingHandler implements MessageHandler {
    private final GenericRedisMessage message;

    public PingHandler(RedisMessage message) {
        this.message = (GenericRedisMessage) message;
    }

    @Override
    public void execute() {
        PaperAPI api = PaperRustyConnector.getAPI();

        api.getVirtualProcessor().pong();
    }
}
