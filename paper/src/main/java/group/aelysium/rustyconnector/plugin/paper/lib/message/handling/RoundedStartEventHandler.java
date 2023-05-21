package group.aelysium.rustyconnector.plugin.paper.lib.message.handling;

import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageHandler;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageRoundedSessionStartEvent;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.rounded.RoundedSessionLifecycle;

public class RoundedStartEventHandler implements MessageHandler {
    private final RedisMessageRoundedSessionStartEvent message;

    public RoundedStartEventHandler(GenericRedisMessage message) {
        this.message = (RedisMessageRoundedSessionStartEvent) message;
    }

    @Override
    public void execute() {
        PaperAPI api = PaperRustyConnector.getAPI();

        RoundedSessionLifecycle sessionLifecycle = api.getVirtualProcessor().getRoundedSessionLifecycle();
        if(sessionLifecycle == null) throw new IllegalStateException("This server doesn't have a rounded session lifecycle manager!");

        sessionLifecycle.start(message.getSessionID());
    }
}
