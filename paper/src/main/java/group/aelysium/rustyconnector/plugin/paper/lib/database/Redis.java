package group.aelysium.rustyconnector.plugin.paper.lib.database;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.core.lib.message.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.message.RedisMessage;
import group.aelysium.rustyconnector.core.lib.message.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.lib.message.handling.PingHandler;
import group.aelysium.rustyconnector.plugin.paper.lib.message.handling.ServerRegHandler;

import javax.naming.AuthenticationException;
import java.net.InetSocketAddress;
import java.security.InvalidAlgorithmParameterException;
import java.util.Map;
import java.util.Objects;

public class Redis extends group.aelysium.rustyconnector.core.lib.database.Redis {
    @Override
    public void onMessage(String rawMessage, Long messageSnowflake) {
        try {
            PaperRustyConnector plugin = PaperRustyConnector.getInstance();

            Gson gson = new Gson();
            JsonObject object = gson.fromJson(rawMessage, JsonObject.class);

            RedisMessage message = RedisMessage.create(object, MessageOrigin.SERVER, plugin.getVirtualServer().getAddress());

            try {
                if (!(plugin.getVirtualServer().validatePrivateKey(message.getKey())))
                    throw new AuthenticationException("This message has an invalid private key!");

                Redis.processParameters(message, object, messageSnowflake);
            } catch (AuthenticationException e) {
                plugin.logger().error("Incoming message from: " + message.getAddress().toString() + " contains an invalid private key! Throwing away...");
                plugin.logger().log("To view the thrown away message use: /rc message get " + messageSnowflake.toString());
            }
        } catch (Exception e) {
            /* TODO: Uncomment and implement proper logging handling
            PaperRustyConnector plugin = PaperRustyConnector.getInstance();

            plugin.logger().error("There was an issue handling the incoming message! Throwing away...",e);
            plugin.logger().log("To view the thrown away message use: /rc message get "+messageSnowflake.toString());
            */
        }
    }

    private static void processParameters(RedisMessage message, JsonObject object, Long messageSnowflake) {
        try {
            switch (message.getType()) {
                case REG_ALL -> {
                    new ServerRegHandler(message).execute();
                }
                case PING -> {
                    new PingHandler(message).execute();
                }
            }
        } catch (NullPointerException e) { // If a parameter fails to resolve, we get this exception.
            PaperRustyConnector.getInstance().logger().error("Incoming message "+message.getType().toString()+" from "+message.getAddress()+" is not formatted properly. Throwing away...", e);
            PaperRustyConnector.getInstance().logger().log("To view the thrown away message use: /rc message get "+messageSnowflake.toString());
        } catch (InvalidAlgorithmParameterException e) { // If one of the data processors fails, we get this exception.
            PaperRustyConnector.getInstance().logger().error("There was an issue handling the message. Throwing away...", e);
            PaperRustyConnector.getInstance().logger().log("To view the thrown away message use: /rc message get "+messageSnowflake.toString());
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
