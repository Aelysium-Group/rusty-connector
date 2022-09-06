package group.aelysium.rustyconnector.plugin.velocity.lib.generic.database;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.core.lib.generic.firewall.MessageTunnel;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PaperServer;
import group.aelysium.rustyconnector.core.lib.generic.database.RedisMessage;
import group.aelysium.rustyconnector.core.lib.generic.database.RedisMessageType;

import javax.naming.AuthenticationException;
import java.net.InetSocketAddress;
import java.security.InvalidAlgorithmParameterException;
import java.util.Map;
import java.util.Objects;

public class Redis extends group.aelysium.rustyconnector.core.lib.generic.database.Redis {

    @Override
    public void onMessage(String rawMessage, Long messageSnowflake) {
        try {
            VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

            Gson gson = new Gson();
            JsonObject object = gson.fromJson(rawMessage, JsonObject.class);

            RedisMessageType type = RedisMessageType.valueOf(object.get("type").getAsString());
            if(type == null) throw new IllegalArgumentException("`type` should be a string which resolves to a RedisMessageType");

            String privateKey = object.get("pk").getAsString();
            if(privateKey == null) throw new IllegalArgumentException("`ppk` should be a string! This message had something else.");

            String fromAddress = object.get("from").getAsString();
            if(fromAddress == null) throw new IllegalArgumentException("`from` should be a string representing a hostname and port number! This message had something else.");
            String[] addressSplit = fromAddress.split(":");
            InetSocketAddress address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));

            String to = object.get("to").getAsString();
            if(to != null) throw new IllegalArgumentException("`to` is set! This message is from the proxy! Ignoring...");

            RedisMessage message = new RedisMessage(
                privateKey,
                type,
                fromAddress,
                true
            );

            try {
                if (!(plugin.getProxy().validatePrivateKey(message.getKey())))
                    throw new AuthenticationException("This message has an invalid private key!");

                Redis.processParameters(message, object, plugin);
            } catch (AuthenticationException e) {
                plugin.logger().error("Incoming message from: "+address+" contains an invalid private key! Throwing away...");
            }
        } catch (IllegalArgumentException e) {
            VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

            plugin.logger().error("Incoming message is not formatted properly. Throwing away...",e);
            plugin.logger().log("To view the thrown away message use: /rc retrieveMessage "+messageSnowflake.toString());
        } catch (Exception e) {
            VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

            plugin.logger().error("There was an issue handling the incoming message! Throwing away...",e);
            plugin.logger().log("To view the thrown away message use: /rc retrieveMessage "+messageSnowflake.toString());
        }
    }

    private static void processParameters(RedisMessage message, JsonObject object, VelocityRustyConnector plugin) {
        try {
            switch (message.getType()) {
                case REG -> {
                    message.setToParameter(object, "family"); // The family that holds the server
                    message.setToParameter(object, "name"); // The server's identifier
                    message.setToParameter(object, "soft-cap"); // The server's soft cap
                    message.setToParameter(object, "hard-cap"); // The server's hard cap
                    message.setToParameter(object, "player-count"); // The server's current player count

                    PaperServer.getProcessor(message.getType()).execute(message);
                }
                case UNREG, PLAYER_DISCON -> {
                    message.setToParameter(object, "family"); // The family that holds the server
                    message.setToParameter(object, "name"); // The server's identifier

                    PaperServer.getProcessor(message.getType()).execute(message);
                }
                case PLAYER_CNT -> {
                    message.setToParameter(object, "family"); // The family that holds the server
                    message.setToParameter(object, "name"); // The server's identifier
                    message.setToParameter(object, "player-count"); // The server's current player count

                    PaperServer.getProcessor(message.getType()).execute(message);
                }
            }
        } catch (NullPointerException e) { // If a parameter fails to resolve, we get this exception.
            plugin.logger().error("Incoming message "+message.getType().toString()+" from "+message.getAddress()+" is not formatted properly. Throwing away...", e);
        } catch (InvalidAlgorithmParameterException e) { // If one of the data processors fails, we get this exception.
            plugin.logger().error("There was an issue handling the message. Throwing away...", e);
        }
    }


    /**
     * Send a message over the data channel on this Redis instance
     * @param privateKey The private key to send
     * @param type The type of message being sent
     * @param to The IP Address of the server we're sending to.
     * @param parameters Additional parameters
     * @throws IllegalArgumentException If message parameters contains parameters: `pk`, `type`, or `ip`
     */
    @Override
    public void sendMessage(String privateKey, RedisMessageType type, InetSocketAddress to, Map<String, String> parameters) throws IllegalArgumentException {
        Gson gson = new Gson();

        JsonObject object = new JsonObject();
        object.addProperty("pk",privateKey);
        object.addProperty("type",type.toString());
        object.addProperty("to",to.toString());

        JsonObject parameterObject = new JsonObject();
        parameters.forEach(object::addProperty);

        parameterObject.entrySet().forEach(entry -> {
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
