package group.aelysium.rustyconnector.plugin.velocity.lib.database;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.core.lib.data_messaging.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.message.handling.*;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessage;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessageType;

import javax.naming.AuthenticationException;
import java.net.InetSocketAddress;
import java.security.InvalidAlgorithmParameterException;
import java.util.Map;
import java.util.Objects;

public class Redis extends group.aelysium.rustyconnector.core.lib.database.Redis {

    @Override
    public void onMessage(String rawMessage, Long messageSnowflake) {
        try {
            VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

            Gson gson = new Gson();
            JsonObject object = gson.fromJson(rawMessage, JsonObject.class);

            RedisMessage message = RedisMessage.create(object, MessageOrigin.PROXY, null);

            try {
                if (!(plugin.getProxy().validatePrivateKey(message.getKey())))
                    throw new AuthenticationException("This message has an invalid private key!");

                Redis.processParameters(message, object, messageSnowflake);
            } catch (AuthenticationException e) {
                if(!plugin.logger().getGate().check(GateKey.MESSAGE_PARSER_TRASH)) return;
                else if(!plugin.logger().getGate().check(GateKey.INVALID_PRIVATE_KEY)) return;

                plugin.logger().error("Incoming message from: "+message.getAddress().toString()+" contains an invalid private key!");
                plugin.logger().log("To view the thrown away message use: /rc message get "+messageSnowflake.toString());
            }
        } catch (Exception e) {
            VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

            if(!plugin.logger().getGate().check(GateKey.MESSAGE_PARSER_TRASH)) return;

            plugin.logger().error("There was an issue handling the incoming message! Throwing away...",e);
            plugin.logger().log("To view the thrown away message use: /rc message get "+messageSnowflake.toString());
        }
    }

    private static void processParameters(RedisMessage message, JsonObject object, Long messageSnowflake) {
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

                    new PongHandler(message).execute();
                }
            }
        } catch (NullPointerException e) { // If a parameter fails to resolve, we get this exception.
            VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

            if(!plugin.logger().getGate().check(GateKey.MESSAGE_PARSER_TRASH)) return;

            plugin.logger().error("Incoming message "+message.getType().toString()+" from "+message.getAddress()+" is not formatted properly. Throwing away...", e);
            plugin.logger().log("To view the thrown away message use: /rc message get "+messageSnowflake.toString());
        } catch (InvalidAlgorithmParameterException e) { // If one of the data processors fails, we get this exception.
            VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

            if(!plugin.logger().getGate().check(GateKey.MESSAGE_PARSER_TRASH)) return;

            plugin.logger().error("There was an issue handling the message. Throwing away...", e);
            plugin.logger().log("To view the thrown away message use: /rc message get "+messageSnowflake.toString());
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
