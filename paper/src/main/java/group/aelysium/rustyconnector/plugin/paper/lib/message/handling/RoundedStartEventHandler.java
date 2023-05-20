package group.aelysium.rustyconnector.plugin.paper.lib.message.handling;

import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageHandler;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageTPAQueuePlayer;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.lang_messaging.PaperLang;
import group.aelysium.rustyconnector.plugin.paper.lib.tpa.TPARequest;
import org.bukkit.entity.Player;

import java.util.Objects;

public class RoundedStartEventHandler implements MessageHandler {
    private final GenericRedisMessage message;

    public RoundedStartEventHandler(GenericRedisMessage message) {
        this.message =  message;
    }

    @Override
    public void execute() {
        PaperAPI api = PaperRustyConnector.getAPI();
    }
}
