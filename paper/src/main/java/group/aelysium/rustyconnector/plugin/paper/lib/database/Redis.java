package group.aelysium.rustyconnector.plugin.paper.lib.database;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.core.lib.data_messaging.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.data_messaging.MessageStatus;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessage;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.data_messaging.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.lib.message.handling.PingHandler;
import group.aelysium.rustyconnector.plugin.paper.lib.message.handling.ServerRegAllHandler;
import group.aelysium.rustyconnector.plugin.paper.lib.message.handling.ServerRegFamilyHandler;

import javax.naming.AuthenticationException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;

public class Redis extends group.aelysium.rustyconnector.core.lib.database.Redis {
    @Override
    public void onMessage(String rawMessage) {
        PaperRustyConnector plugin = PaperRustyConnector.getInstance();
        CacheableMessage cachedMessage = plugin.getVirtualServer().getMessageCache().cacheMessage(rawMessage, MessageStatus.UNDEFINED);
        try {
            RedisMessage message = RedisMessage.create(rawMessage, MessageOrigin.SERVER, plugin.getVirtualServer().getAddress());
            try {
                if (!(plugin.getVirtualServer().validatePrivateKey(message.getKey())))
                    throw new AuthenticationException("This message has an invalid private key!");

                cachedMessage.sentenceMessage(MessageStatus.ACCEPTED);

                Redis.processParameters(message, cachedMessage);
            } catch (AuthenticationException e) {
                plugin.logger().error("Incoming message from: " + message.getAddress().toString() + " contains an invalid private key! Throwing away...");
                plugin.logger().log("To view the thrown away message use: /rc message get " + cachedMessage.getSnowflake());
            }
        } catch (NullPointerException e) {
            cachedMessage.sentenceMessage(MessageStatus.PARSING_ERROR);

            /* TODO: Uncomment and implement proper logging handling
            PaperRustyConnector plugin = PaperRustyConnector.getInstance();

            plugin.logger().error("There was an issue handling the incoming message! Throwing away...",e);
            plugin.logger().log("To view the thrown away message use: /rc message get "+messageSnowflake.toString());
            */
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
            }

            cachedMessage.sentenceMessage(MessageStatus.EXECUTED);
        } catch (Exception e) {
            cachedMessage.sentenceMessage(MessageStatus.PARSING_ERROR);

            PaperRustyConnector.getInstance().logger().error("Incoming message " + message.getType().toString() + " from " + message.getAddress() + " is not formatted properly. Throwing away...", e);
            PaperRustyConnector.getInstance().logger().log("To view the thrown away message use: /rc message get " + cachedMessage.getSnowflake());
        }
    }

    @Override
    public void sendMessage(String privateKey, RedisMessageType type, InetSocketAddress address, Map<String, String> parameters) throws IllegalArgumentException {
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

        this.publish(gson.toJson(object));
    }

}
