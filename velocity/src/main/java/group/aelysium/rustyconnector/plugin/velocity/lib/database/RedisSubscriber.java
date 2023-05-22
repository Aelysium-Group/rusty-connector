package group.aelysium.rustyconnector.plugin.velocity.lib.database;

import group.aelysium.rustyconnector.core.lib.database.redis.RedisClient;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisService;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageStatus;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.exception.BlockedMessageException;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.central.Processor;
import group.aelysium.rustyconnector.plugin.velocity.lib.message.handling.*;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;

import javax.naming.AuthenticationException;

import static group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType.*;

public class RedisSubscriber extends group.aelysium.rustyconnector.core.lib.database.redis.RedisSubscriber {
    public RedisSubscriber(RedisClient client) {
        super(client);
    }

    @Override
    public void onMessage(String rawMessage) {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();
        Processor virtualProcessor = api.getProcessor();
        MessageCacheService messageCacheService = virtualProcessor.getService(MessageCacheService.class);

        // If the proxy doesn't have a message cache (maybe it's in the middle of a reload)
        // Send a temporary, worthless, message cache so that the system can still "cache" messages into the worthless cache if needed.
        if(messageCacheService == null) messageCacheService = new MessageCacheService(1);

        CacheableMessage cachedMessage = messageCacheService.cacheMessage(rawMessage, MessageStatus.UNDEFINED);
        try {
            GenericRedisMessage.Serializer serializer = new GenericRedisMessage.Serializer();
            GenericRedisMessage message = serializer.parseReceived(rawMessage);

            if(message.getOrigin() == MessageOrigin.PROXY) throw new Exception("Message from the proxy! Ignoring...");
            try {
                virtualProcessor.getService(RedisService.class).validatePrivateKey(message.getPrivateKey());

                if (!(virtualProcessor.getService(RedisService.class).validatePrivateKey(message.getPrivateKey())))
                    throw new AuthenticationException("This message has an invalid private key!");

                cachedMessage.sentenceMessage(MessageStatus.ACCEPTED);
                RedisSubscriber.processParameters(message, cachedMessage);
            } catch (AuthenticationException e) {
                cachedMessage.sentenceMessage(MessageStatus.AUTH_DENIAL, e.getMessage());

                logger.error("An incoming message from: "+message.getAddress().toString()+" had an invalid private-key!");
                logger.log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake());
            } catch (BlockedMessageException e) {
                cachedMessage.sentenceMessage(MessageStatus.AUTH_DENIAL, e.getMessage());

                if(!logger.getGate().check(GateKey.MESSAGE_TUNNEL_FAILED_MESSAGE)) return;

                logger.error("An incoming message from: "+message.getAddress().toString()+" was blocked by the message tunnel!");
                logger.log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake());
            } catch (NoOutputException e) {
                cachedMessage.sentenceMessage(MessageStatus.AUTH_DENIAL, e.getMessage());
            }
        } catch (Exception e) {
            if(logger.getGate().check(GateKey.SAVE_TRASH_MESSAGES))
                cachedMessage.sentenceMessage(MessageStatus.TRASHED, e.getMessage());
            else
                virtualProcessor.getService(MessageCacheService.class).removeMessage(cachedMessage.getSnowflake());

            if(!logger.getGate().check(GateKey.MESSAGE_PARSER_TRASH)) return;

            logger.error("An incoming message was thrown away!");
            logger.log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake());
        }
    }

    private static void processParameters(GenericRedisMessage message, CacheableMessage cachedMessage) {
        PluginLogger logger = VelocityRustyConnector.getAPI().getLogger();

        try {
            if(message.getType() == REGISTER_SERVER)                    new ServerRegHandler(message).execute();
            if(message.getType() == UNREGISTER_SERVER)                  new ServerUnRegHandler(message).execute();
            if(message.getType() == SEND_PLAYER)                        new SendPlayerHandler(message).execute();
            if(message.getType() == PONG)                               new PongHandler(message).execute();
            if(message.getType() == ROUNDED_PRECONNECT_PLAYER)          new RoundedFamilyPreConnectHandler(message).execute();
            if(message.getType() == ROUNDED_CANCEL_PRECONNECT_PLAYER)   new RoundedFamilyCancelPreConnectHandler(message).execute();
            if(message.getType() == ROUNDED_SESSION_CLOSE_REQUEST)        new RoundedSessionCloseRequestHandler(message).execute();

            cachedMessage.sentenceMessage(MessageStatus.EXECUTED);
        } catch (NullPointerException e) {
            cachedMessage.sentenceMessage(MessageStatus.PARSING_ERROR);

            if(!logger.getGate().check(GateKey.MESSAGE_PARSER_TRASH)) return;

            logger.error("There was an issue handling the message. Throwing away...", e);
            logger.log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake());
        } catch (Exception e) {
            cachedMessage.sentenceMessage(MessageStatus.EXECUTING_ERROR, e.getMessage());

            if(!logger.getGate().check(GateKey.MESSAGE_PARSER_TRASH)) return;

            logger.error("There was an issue handling the message. Throwing away...", e);
            logger.log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake());
        }
    }
}
