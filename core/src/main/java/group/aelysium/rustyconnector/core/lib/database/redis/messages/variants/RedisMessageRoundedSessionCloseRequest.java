package group.aelysium.rustyconnector.core.lib.database.redis.messages.variants;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import io.lettuce.core.KeyValue;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class RedisMessageRoundedSessionCloseRequest extends GenericRedisMessage {
    private String familyName;
    private String serverName;

    public String getFamilyName() {
        return familyName;
    }
    public String getServerName() {
        return serverName;
    }

    public RedisMessageRoundedSessionCloseRequest(InetSocketAddress address, MessageOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(RedisMessageType.ROUNDED_SESSION_CLOSE_REQUEST, address, origin);

        if(!RedisMessageRoundedSessionCloseRequest.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            switch (key) {
                case ValidParameters.FAMILY_NAME -> this.familyName = value.getAsString();
                case ValidParameters.SERVER_NAME -> this.serverName = value.getAsString();
            }
        });
    }
    public RedisMessageRoundedSessionCloseRequest(int messageVersion, String rawMessage, char[] privateKey, InetSocketAddress address, MessageOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(messageVersion, rawMessage, privateKey, RedisMessageType.ROUNDED_SESSION_CLOSE_REQUEST, address, origin);

        if(!RedisMessageRoundedSessionCloseRequest.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            switch (key) {
                case ValidParameters.FAMILY_NAME -> this.familyName = value.getAsString();
                case ValidParameters.SERVER_NAME -> this.serverName = value.getAsString();
            }
        });
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = super.toJSON();
        JsonObject parameters = new JsonObject();

        parameters.add(ValidParameters.FAMILY_NAME, new JsonPrimitive(this.familyName));
        parameters.add(ValidParameters.SERVER_NAME, new JsonPrimitive(this.serverName));

        object.add(MasterValidParameters.PARAMETERS, parameters);

        return object;
    }

    public interface ValidParameters {
        String FAMILY_NAME = "f";
        String SERVER_NAME = "s";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(FAMILY_NAME);
            list.add(SERVER_NAME);

            return list;
        }
    }
}
