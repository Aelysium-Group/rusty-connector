package group.aelysium.rustyconnector.plugin.paper.lib.database;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisClient;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisSubscriber;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageStatus;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.message.handling.PingHandler;
import group.aelysium.rustyconnector.plugin.paper.lib.message.handling.ServerRegAllHandler;
import group.aelysium.rustyconnector.plugin.paper.lib.message.handling.ServerRegFamilyHandler;
import group.aelysium.rustyconnector.plugin.paper.lib.message.handling.TPAQueuePlayerHandler;
import io.lettuce.core.RedisURI;
import io.lettuce.core.resource.ClientResources;

import javax.naming.AuthenticationException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;

public class RedisIO extends RedisSubscriber {
    protected RedisIO(RedisClient client) {
        super(client);
    }

    @Override
    public void onMessage(String rawMessage) {
        PaperAPI api = PaperRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        CacheableMessage cachedMessage = api.getVirtualProcessor().getMessageCache().cacheMessage(rawMessage, MessageStatus.UNDEFINED);

        try {
            RedisMessage message = RedisMessage.create(rawMessage, MessageOrigin.SERVER, api.getVirtualProcessor().getAddress());
            try {
                if (!(api.getVirtualProcessor().validatePrivateKey(message.getKey())))
                    throw new AuthenticationException("This message has an invalid private key!");

                cachedMessage.sentenceMessage(MessageStatus.ACCEPTED);

                RedisIO.processParameters(message, cachedMessage);
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

        Gson gson = new Gson();
        JsonObject object = gson.fromJson(message.toString(), JsonObject.class);

        try {
            switch (message.getType()) {
                case REG_ALL -> new ServerRegAllHandler(message).execute();
                case REG_FAMILY -> {
                    message.setToParameter(object, "family"); // The family of this server

                    new ServerRegFamilyHandler(message).execute();
                }
                case PING -> new PingHandler(message).execute();
                case TPA_QUEUE_PLAYER -> {
                    message.setToParameter(object, "target-username");
                    message.setToParameter(object, "source-username");

                    new TPAQueuePlayerHandler(message).execute();
                }
            }

            cachedMessage.sentenceMessage(MessageStatus.EXECUTED);
        } catch (Exception e) {
            cachedMessage.sentenceMessage(MessageStatus.PARSING_ERROR);

            logger.error("Incoming message " + message.getType().toString() + " from " + message.getAddress() + " is not formatted properly. Throwing away...", e);
            logger.log("To view the thrown away message use: /rc message get " + cachedMessage.getSnowflake());
        }
    }

    @Override
    public void sendPluginMessage(String privateKey, RedisMessageType type, InetSocketAddress address, Map<String, String> parameters) throws IllegalArgumentException {
        Gson gson = new Gson();

        JsonObject object = new JsonObject();
        object.addProperty("pk",privateKey);
        object.addProperty("type",type.toString());
        object.addProperty("from",AddressUtil.addressToString(address)); // We tell the proxy who is sending the message to it

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

        super.publish(gson.toJson(object));
    }
}
