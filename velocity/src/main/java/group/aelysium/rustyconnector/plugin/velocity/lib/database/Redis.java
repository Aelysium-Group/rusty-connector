package group.aelysium.rustyconnector.plugin.velocity.lib.database;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.core.lib.data_messaging.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.data_messaging.MessageStatus;
import group.aelysium.rustyconnector.core.lib.data_messaging.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.data_messaging.cache.MessageCache;
import group.aelysium.rustyconnector.core.lib.database.RedisIO;
import group.aelysium.rustyconnector.core.lib.exception.BlockedMessageException;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.VirtualProxyProcessor;
import group.aelysium.rustyconnector.plugin.velocity.lib.message.handling.*;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessage;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessageType;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.resource.ClientResources;

import javax.naming.AuthenticationException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public class Redis extends RedisIO {
    protected Redis(RedisClient client) {
        super(client);
    }

    @Override
    public void onMessage(String rawMessage) {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();
        VirtualProxyProcessor virtualProcessor = api.getVirtualProcessor();
        MessageCache messageCache = virtualProcessor.getMessageCache();

        // If the proxy doesn't have a message cache (maybe it's in the middle of a reload.
        // Send a temporary, worthless, message cache so that the system can still "cache" messages into the worthless cache if needed.
        if(messageCache == null) messageCache = new MessageCache(1);

        CacheableMessage cachedMessage = messageCache.cacheMessage(rawMessage, MessageStatus.UNDEFINED);
        try {
            RedisMessage message = RedisMessage.create(rawMessage, MessageOrigin.PROXY, null);
            try {
                virtualProcessor.validateMessage(message);

                if (!(virtualProcessor.validatePrivateKey(message.getKey())))
                    throw new AuthenticationException("This message has an invalid private key!");

                cachedMessage.sentenceMessage(MessageStatus.ACCEPTED);
                Redis.processParameters(message, cachedMessage);
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
                virtualProcessor.getMessageCache().removeMessage(cachedMessage.getSnowflake());

            if(!logger.getGate().check(GateKey.MESSAGE_PARSER_TRASH)) return;

            logger.error("An incoming message was thrown away!");
            logger.log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake());
        }
    }

    private static void processParameters(RedisMessage message, CacheableMessage cachedMessage) {
        PluginLogger logger = VelocityRustyConnector.getAPI().getLogger();

        Gson gson = new Gson();
        JsonObject object = gson.fromJson(message.toString(), JsonObject.class);
        try {
            switch (message.getType()) {
                case REG -> {
                    message.setToParameter(object, "family"); // The family that holds the server
                    message.setToParameter(object, "name"); // The server's identifier
                    message.setToParameter(object, "soft-cap"); // The server's soft cap
                    message.setToParameter(object, "hard-cap"); // The server's hard cap
                    message.setToParameter(object, "weight"); // The server's current player count

                    new ServerRegHandler(message).execute();
                }
                case UNREG -> {
                    message.setToParameter(object, "family"); // The family that holds the server
                    message.setToParameter(object, "name"); // The server's identifier

                    new ServerUnRegHandler(message).execute();
                }
                case SEND -> {
                    message.setToParameter(object, "family"); // The family to send the player to
                    message.setToParameter(object, "uuid"); // The uuid of the player to move

                    new SendPlayerHandler(message).execute();
                }
                case PONG -> {
                    message.setToParameter(object, "name"); // The server's identifier
                    message.setToParameter(object, "player-count"); // The active player count on the sub-server

                    new PongHandler(message).execute();
                }
            }

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

    @Override
    public void sendPluginMessage(String privateKey, RedisMessageType type, InetSocketAddress address, Map<String, String> parameters) throws IllegalArgumentException {
        Gson gson = new Gson();

        JsonObject object = new JsonObject();
        object.addProperty("pk",privateKey);
        object.addProperty("type",type.toString());
        object.addProperty("to", AddressUtil.addressToString(address)); // We tell the servers who we are sending our message to

        JsonObject parameterObject = new JsonObject();
        parameters.forEach(object::addProperty);

        parameterObject.entrySet().forEach(entry -> {
            // List of illegal parameters which can't be used
            if(Objects.equals(entry.getKey(), "pk")) throw new IllegalArgumentException("You can't send this parameters in a message!");
            if(Objects.equals(entry.getKey(), "type")) throw new IllegalArgumentException("You can't send this parameters in a message!");
            if(Objects.equals(entry.getKey(), "ip")) throw new IllegalArgumentException("You can't send this parameters in a message!");
            if(Objects.equals(entry.getKey(), "to")) throw new IllegalArgumentException("You can't send this parameters in a message!");
            if(Objects.equals(entry.getKey(), "from")) throw new IllegalArgumentException("You can't send this parameters in a message!");

            object.addProperty(entry.getKey(),entry.getValue().getAsString());
        });

        this.publish(VelocityRustyConnector.getAPI().getVirtualProcessor().getRedisDataChannel(), gson.toJson(object));
    }

    public static class RedisConnector {
        private String host = "localhost";
        private int port = 3306;
        private String user = "default";
        private char[] password = null;

        public RedisConnector() {}

        public RedisConnector setHost(String host) {
            this.host = host;
            return this;
        }

        public RedisConnector setPort(int port) {
            this.port = port;
            return this;
        }

        public RedisConnector setUser(String user) {
            this.user = user;
            return this;
        }

        public RedisConnector setPassword(String password) {
            this.password = password.toCharArray();
            return this;
        }

        public Redis build() {
            RedisURI uri;
            if(this.password == null)
                uri = RedisURI.builder()
                        .withHost(this.host)
                        .withPort(this.port)
                        .withAuthentication(this.user, "")
                        .build();
            else
                uri = RedisURI.builder()
                        .withHost(this.host)
                        .withPort(this.port)
                        .withAuthentication(this.user, this.password)
                        .build();

            ClientResources resources = ClientResources.create();

            return new Redis(RedisClient.create(resources, uri));
        }
    }
}
