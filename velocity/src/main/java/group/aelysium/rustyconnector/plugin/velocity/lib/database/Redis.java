package group.aelysium.rustyconnector.plugin.velocity.lib.database;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.core.lib.data_messaging.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.data_messaging.MessageStatus;
import group.aelysium.rustyconnector.core.lib.data_messaging.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.message.handling.*;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessage;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessageType;

import javax.naming.AuthenticationException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;

public class Redis extends group.aelysium.rustyconnector.core.lib.database.Redis {

    @Override
    public void onMessage(String rawMessage) {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();
        CacheableMessage cachedMessage = plugin.getProxy().getMessageCache().cacheMessage(rawMessage, MessageStatus.UNDEFINED);
        try {
            RedisMessage message = RedisMessage.create(rawMessage, MessageOrigin.PROXY, null);
            try {
                if(!plugin.getProxy().validateMessage(message))
                    throw new AuthenticationException("The message was to long!");
                if (!(plugin.getProxy().validatePrivateKey(message.getKey())))
                    throw new AuthenticationException("This message has an invalid private key!");

                cachedMessage.sentenceMessage(MessageStatus.ACCEPTED);
                Redis.processParameters(message, cachedMessage);
            } catch (AuthenticationException e) {
                cachedMessage.sentenceMessage(MessageStatus.AUTH_DENIAL, e.getMessage());

                if(!plugin.logger().getGate().check(GateKey.MESSAGE_PARSER_TRASH)) return;
                else if(!plugin.logger().getGate().check(GateKey.INVALID_PRIVATE_KEY)) return;

                plugin.logger().error("An incoming message from: "+message.getAddress().toString()+" was thrown away!");
                plugin.logger().log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake());
            }
        } catch (Exception e) {
            if(plugin.logger().getGate().check(GateKey.SAVE_TRASH_MESSAGES))
                cachedMessage.sentenceMessage(MessageStatus.TRASHED, e.getMessage());
            else
                plugin.getProxy().getMessageCache().removeMessage(cachedMessage.getSnowflake());

            if(!plugin.logger().getGate().check(GateKey.MESSAGE_PARSER_TRASH)) return;

            plugin.logger().error("An incoming message was thrown away!");
            plugin.logger().log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake());
        }
    }

    private static void processParameters(RedisMessage message, CacheableMessage cachedMessage) {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

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

            if(!plugin.logger().getGate().check(GateKey.MESSAGE_PARSER_TRASH)) return;

            plugin.logger().error("There was an issue handling the message. Throwing away...", e);
            plugin.logger().log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake());
        } catch (Exception e) {
            cachedMessage.sentenceMessage(MessageStatus.EXECUTING_ERROR, e.getMessage());

            if(!plugin.logger().getGate().check(GateKey.MESSAGE_PARSER_TRASH)) return;

            plugin.logger().error("There was an issue handling the message. Throwing away...", e);
            plugin.logger().log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake());
        }
    }

    @Override
    public void sendMessage(String privateKey, RedisMessageType type, InetSocketAddress address, Map<String, String> parameters) throws IllegalArgumentException {
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

        this.publish(gson.toJson(object));
    }
}
