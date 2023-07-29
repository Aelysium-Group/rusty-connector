package group.aelysium.rustyconnector.plugin.paper.lib.database;

import group.aelysium.rustyconnector.core.lib.database.redis.RedisClient;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageStatus;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.handlers.CoordinateRequestHandler;
import group.aelysium.rustyconnector.plugin.paper.lib.magic_link.handlers.MagicLink_PingResponseHandler;

import javax.naming.AuthenticationException;

import static group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType.*;

public class RedisSubscriber extends group.aelysium.rustyconnector.core.lib.database.redis.RedisSubscriber {
    public RedisSubscriber(RedisClient client) {
        super(client);
    }

    @Override
    public void onMessage(String rawMessage) {
        PaperAPI api = PaperAPI.get();
        PluginLogger logger = api.logger();

        CacheableMessage cachedMessage = api.services().messageCacheService().cacheMessage(rawMessage, MessageStatus.UNDEFINED);

        try {
            GenericRedisMessage.Serializer serializer = new GenericRedisMessage.Serializer();
            GenericRedisMessage message = serializer.parseReceived(rawMessage);

            if(message.origin() == MessageOrigin.SERVER) throw new Exception("Message from a sub-server! Ignoring...");


            if(!AddressUtil.addressToString(message.address()).equals(api.services().serverInfoService().address()))
               throw new Exception("Message addressed to another sub-server! Ignoring...");
            try {
                if (!(api.services().redisService().validatePrivateKey(message.privateKey())))
                    throw new AuthenticationException("This message has an invalid private key!");


                cachedMessage.sentenceMessage(MessageStatus.ACCEPTED);

                RedisSubscriber.processParameters(message, cachedMessage);
            } catch (AuthenticationException e) {
                logger.error("Incoming message from: " + message.address().toString() + " contains an invalid private key! Throwing away...");
                logger.log("To view the thrown away message use: /rc message get " + cachedMessage.getSnowflake());
            }
        } catch (Exception e) {
            cachedMessage.sentenceMessage(MessageStatus.TRASHED);
            /* TODO: Uncomment and implement proper logging handling

            logger.error("There was an issue handling the incoming message! Throwing away...",e);
            logger.log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake().toString());
            */
        }
    }

    private static void processParameters(GenericRedisMessage message, CacheableMessage cachedMessage) {
        PluginLogger logger = PaperAPI.get().logger();

        try {
            if(message.type() == PING_RESPONSE)      new MagicLink_PingResponseHandler(message).execute();
            if(message.type() == COORDINATE_REQUEST_QUEUE)   new CoordinateRequestHandler(message).execute();

            cachedMessage.sentenceMessage(MessageStatus.EXECUTED);
        } catch (Exception e) {
            cachedMessage.sentenceMessage(MessageStatus.PARSING_ERROR);

            logger.error("Incoming message " + message.type().name() + " from " + message.address() + " is not formatted properly. Throwing away...", e);
            logger.log("To view the thrown away message use: /rc message get " + cachedMessage.getSnowflake());
        }
    }
}
