package group.aelysium.rustyconnector.plugin.paper.lib.database;

import group.aelysium.rustyconnector.core.lib.database.redis.RedisClient;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisService;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageStatus;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.cache.MessageCacheService;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.message.handling.*;

import javax.naming.AuthenticationException;

import static group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType.*;

public class RedisSubscriber extends group.aelysium.rustyconnector.core.lib.database.redis.RedisSubscriber {
    public RedisSubscriber(RedisClient client) {
        super(client);
    }

    @Override
    public void onMessage(String rawMessage) {
        PaperAPI api = PaperRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        CacheableMessage cachedMessage = api.getService(MessageCacheService.class).cacheMessage(rawMessage, MessageStatus.UNDEFINED);

        try {
            GenericRedisMessage.Serializer serializer = new GenericRedisMessage.Serializer();
            GenericRedisMessage message = serializer.parseReceived(rawMessage);

            if(message.getOrigin() == MessageOrigin.SERVER) throw new Exception("Message from a sub-server! Ignoring...");
            try {
                if (!(api.getService(RedisService.class).validatePrivateKey(message.getPrivateKey())))
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

    private static void processParameters(GenericRedisMessage message, CacheableMessage cachedMessage) {
        PluginLogger logger = PaperRustyConnector.getAPI().getLogger();

        try {
            if(message.getType() == REGISTER_ALL_SERVERS_TO_PROXY)      new ServerRegAllHandler(message).execute();
            if(message.getType() == REGISTER_ALL_SERVERS_TO_FAMILY)     new ServerRegFamilyHandler(message).execute();
            if(message.getType() == PING)                               new PingHandler(message).execute();
            if(message.getType() == TPA_QUEUE_PLAYER)                   new TPAQueuePlayerHandler(message).execute();

            cachedMessage.sentenceMessage(MessageStatus.EXECUTED);
        } catch (Exception e) {
            cachedMessage.sentenceMessage(MessageStatus.PARSING_ERROR);

            logger.error("Incoming message " + message.getType().name() + " from " + message.getAddress() + " is not formatted properly. Throwing away...", e);
            logger.log("To view the thrown away message use: /rc message get " + cachedMessage.getSnowflake());
        }
    }
}
