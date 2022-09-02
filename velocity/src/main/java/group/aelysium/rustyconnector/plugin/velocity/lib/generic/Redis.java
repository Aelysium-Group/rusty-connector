package group.aelysium.rustyconnector.plugin.velocity.lib.generic;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PaperServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerFamily;
import rustyconnector.RustyConnector;
import rustyconnector.generic.lib.database.RedisMessage;
import rustyconnector.generic.lib.database.RedisMessageType;
import rustyconnector.generic.lib.generic.server.Family;

import javax.naming.AuthenticationException;
import java.security.InvalidAlgorithmParameterException;

public class Redis extends rustyconnector.generic.lib.database.Redis {
    @Override
    public void onMessage(String rawMessage, RustyConnector plugin) {
        try {
            Gson gson = new Gson();
            JsonArray json = gson.fromJson(rawMessage, JsonArray.class);

            RedisMessage message;

            for (JsonElement entry: json) {
                JsonObject object = entry.getAsJsonObject();

                RedisMessageType type = RedisMessageType.valueOf(object.get("type").getAsString());
                if(type == null) throw new IllegalArgumentException("`type` should be a string which resolves to a RedisMessageType");

                String privateKey = object.get("pk").getAsString();
                if(privateKey == null) throw new IllegalArgumentException("`ppk` should be a string! This message had something else.");

                String address = object.get("ip").getAsString();
                if(address == null) throw new IllegalArgumentException("`ip` should be a string representing a hostname and port number! This message had something else.");

                message = new RedisMessage(
                    privateKey,
                    type,
                    address
                );

                try {
                    if (!((VelocityRustyConnector) plugin).getProxy().validatePrivateKey(message.getKey()))
                        throw new AuthenticationException("This message has an invalid private key!");

                    Redis.processParameters(message, object, (VelocityRustyConnector) plugin);
                } catch (AuthenticationException e) {
                    plugin.logger().error("Incoming message from: "+address+" contains an invalid private key! Throwing away...");
                }
            }
        } catch (IllegalArgumentException e) {
            plugin.logger().error("Incoming message is not formatted properly. Throwing away...",e);
        } catch (Exception e) {

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

}
