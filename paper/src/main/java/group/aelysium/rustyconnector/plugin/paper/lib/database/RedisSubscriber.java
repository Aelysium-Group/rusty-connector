package group.aelysium.rustyconnector.plugin.paper.lib.database;

import group.aelysium.rustyconnector.core.lib.database.redis.RedisClient;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageStatus;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.cache.CacheableMessage;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.message.handling.PingHandler;
import group.aelysium.rustyconnector.plugin.paper.lib.message.handling.ServerRegAllHandler;
import group.aelysium.rustyconnector.plugin.paper.lib.message.handling.ServerRegFamilyHandler;
import group.aelysium.rustyconnector.plugin.paper.lib.message.handling.TPAQueuePlayerHandler;

import javax.naming.AuthenticationException;

public class RedisSubscriber extends group.aelysium.rustyconnector.core.lib.database.redis.RedisSubscriber {
    protected RedisSubscriber(RedisClient client) {
        super(client);
    }

    @Override
    public void onMessage(String rawMessage) {
        PaperAPI api = PaperRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        CacheableMessage cachedMessage = api.getVirtualProcessor().getMessageCache().cacheMessage(rawMessage, MessageStatus.UNDEFINED);

        try {
            RedisMessage.Serializer serializer = new RedisMessage.Serializer();
            RedisMessage message = serializer.parseReceived(rawMessage);

            if(message.getOrigin() == MessageOrigin.SERVER) throw new Exception("Message from a sub-server! Ignoring...");
            try {
                if (!(api.getVirtualProcessor().getRedisService().validatePrivateKey(message.getPrivateKey())))
                    throw new AuthenticationException("This message has an invalid private key!");

                cachedMessage.sentenceMessage(MessageStatus.ACCEPTED);

                RedisSubscriber.processParameters(message, cachedMessage);
            } catch (AuthenticationException e) {
                logger.error("Incoming message from: " + message.getAddress().toString() + " contains an invalid private key! Throwing away...");
                logger.log("To view the thrown away message use: /rc message get " + cachedMessage.getSnowflake());
            }
        } catch (Exception e) {
            cachedMessage.sentenceMessage(MessageStatus.TRASHED);
            /* TODO: Uncomment and implement proper logging handling
            PaperRustyConnector plugin = PaperRustyConnector.getInstance();

            plugin.logger().error("There was an issue handling the incoming message! Throwing away...",e);
            plugin.logger().log("To view the thrown away message use: /rc message get "+messageSnowflake.toString());
            */
        }
    }

    private static void processParameters(RedisMessage message, CacheableMessage cachedMessage) {
        PluginLogger logger = PaperRustyConnector.getAPI().getLogger();

        try {
            switch (message.getType()) {
                case REG_ALL -> new ServerRegAllHandler(message).execute();
                case REG_FAMILY -> new ServerRegFamilyHandler(message).execute();
                case PING -> new PingHandler(message).execute();
                case TPA_QUEUE_PLAYER -> new TPAQueuePlayerHandler(message).execute();
            }

            cachedMessage.sentenceMessage(MessageStatus.EXECUTED);
        } catch (Exception e) {
            cachedMessage.sentenceMessage(MessageStatus.PARSING_ERROR);

            logger.error("Incoming message " + message.getType().toString() + " from " + message.getAddress() + " is not formatted properly. Throwing away...", e);
            logger.log("To view the thrown away message use: /rc message get " + cachedMessage.getSnowflake());
        }
    }
}
