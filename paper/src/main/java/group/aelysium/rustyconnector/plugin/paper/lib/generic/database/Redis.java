package group.aelysium.rustyconnector.plugin.paper.lib.generic.database;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.core.lib.generic.database.RedisMessage;
import group.aelysium.rustyconnector.core.lib.generic.database.RedisMessageType;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;

import javax.naming.AuthenticationException;
import java.net.InetSocketAddress;
import java.security.InvalidAlgorithmParameterException;

public class Redis extends group.aelysium.rustyconnector.core.lib.generic.database.Redis {
    @Override
    public void onMessage(String rawMessage, Long messageSnowflake) {
        try {
            PaperRustyConnector plugin = PaperRustyConnector.getInstance();

            Gson gson = new Gson();
            JsonObject object = gson.fromJson(rawMessage, JsonObject.class);
            try {

                JsonElement fromAddress = object.get("from");
                if(fromAddress != null) throw new IllegalArgumentException("`from` is set! This message is from another sub-server! Ignoring...");

                String toAddress = object.get("to").getAsString();
                if(toAddress == null) throw new IllegalArgumentException("`from` should be a string representing a hostname and port number! This message had something else.");
                String[] addressSplit = toAddress.split(":");
                InetSocketAddress address = new InetSocketAddress(addressSplit[0].replace("/",""), Integer.parseInt(addressSplit[1]));

                String typeString = object.get("type").getAsString();
                RedisMessageType type = RedisMessageType.valueOf(typeString);
                if(type == null) throw new IllegalArgumentException("`type` should be a string which resolves to a RedisMessageType");

                String privateKey = object.get("pk").getAsString();
                if(privateKey == null) throw new IllegalArgumentException("`pk` should be a string! This message had something else.");

                RedisMessage message = new RedisMessage(
                        privateKey,
                        type,
                        address,
                        true
                );

                try {
                    if (!(PaperRustyConnector.getInstance().validatePrivateKey(message.getKey())))
                        throw new AuthenticationException("This message has an invalid private key!");

                    Redis.processParameters(message, object, messageSnowflake);
                } catch (AuthenticationException e) {
                    plugin.logger().error("Incoming message from: " + address + " contains an invalid private key! Throwing away...");
                    plugin.logger().log("To view the thrown away message use: /rc retrieveMessage "+messageSnowflake.toString());
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

    private static void processParameters(RedisMessage message, JsonObject object, Long messageSnowflake) {
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
            PaperRustyConnector.getInstance().logger().log("To view the thrown away message use: /rc retrieveMessage "+messageSnowflake.toString());
        } catch (InvalidAlgorithmParameterException e) { // If one of the data processors fails, we get this exception.
            PaperRustyConnector.getInstance().logger().error("There was an issue handling the message. Throwing away...", e);
            PaperRustyConnector.getInstance().logger().log("To view the thrown away message use: /rc retrieveMessage "+messageSnowflake.toString());
        }
    }

}
