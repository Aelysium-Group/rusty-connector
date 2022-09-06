package group.aelysium.rustyconnector.plugin.paper.lib.generic.database;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.core.lib.generic.database.RedisMessage;
import group.aelysium.rustyconnector.core.lib.generic.database.RedisMessageType;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;

import javax.naming.AuthenticationException;
import java.security.InvalidAlgorithmParameterException;

public class Redis extends group.aelysium.rustyconnector.core.lib.generic.database.Redis {
    @Override
    public void onMessage(String rawMessage, Long messageSnowflake) {
        try {
            PaperRustyConnector plugin = PaperRustyConnector.getInstance();

            Gson gson = new Gson();
            JsonObject object = gson.fromJson(rawMessage, JsonObject.class);
            try {
                RedisMessageType type = RedisMessageType.valueOf(object.get("type").getAsString());
                if (type == null)
                    throw new IllegalArgumentException("`type` should be a string which resolves to a RedisMessageType");

                String privateKey = object.get("pk").getAsString();
                if (privateKey == null)
                    throw new IllegalArgumentException("`pk` should be a string! This message had something else.");

                String address = object.get("to").getAsString();
                if (address == null) throw new IllegalArgumentException("`to` should be set! Ignoring...");
                if (!address.equals(plugin.getVirtualServer().getAddress().toString()))
                    throw new IllegalArgumentException("`to` is not directed to this server! Ignoring...");

                String to = object.get("from").getAsString();
                if (to != null)
                    throw new IllegalArgumentException("`from` is set! This message is from another sub-server! Ignoring...");

                RedisMessage message = new RedisMessage(
                        privateKey,
                        type,
                        address,
                        true
                );

                try {
                    if (!(PaperRustyConnector.getInstance().validatePrivateKey(message.getKey())))
                        throw new AuthenticationException("This message has an invalid private key!");

                    Redis.processParameters(message, object);
                } catch (AuthenticationException e) {
                    plugin.logger().error("Incoming message from: " + address + " contains an invalid private key! Throwing away...");
                }
            } catch (IllegalArgumentException e) {
                return;
            }
        } catch (IllegalArgumentException e) {
            PaperRustyConnector plugin = PaperRustyConnector.getInstance();

            plugin.logger().error("Incoming message is not formatted properly. Throwing away...",e);
            plugin.logger().log("To view the thrown away message use: /rc retrieveMessage "+messageSnowflake.toString());
        } catch (Exception e) {
            PaperRustyConnector plugin = PaperRustyConnector.getInstance();

            plugin.logger().error("There was an issue handling the incoming message! Throwing away...",e);
            plugin.logger().log("To view the thrown away message use: /rc retrieveMessage "+messageSnowflake.toString());
        }
    }

    private static void processParameters(RedisMessage message, JsonObject object) {
        try {
            switch (message.getType()) {
                case REQ_REG -> {
                }
                case PING -> {
                    throw new InvalidAlgorithmParameterException();
                }
            }
        } catch (NullPointerException e) { // If a parameter fails to resolve, we get this exception.
            PaperRustyConnector.getInstance().logger().error("Incoming message "+message.getType().toString()+" from "+message.getAddress()+" is not formatted properly. Throwing away...", e);
        } catch (InvalidAlgorithmParameterException e) { // If one of the data processors fails, we get this exception.
            PaperRustyConnector.getInstance().logger().error("There was an issue handling the message. Throwing away...", e);
        }
    }

}
