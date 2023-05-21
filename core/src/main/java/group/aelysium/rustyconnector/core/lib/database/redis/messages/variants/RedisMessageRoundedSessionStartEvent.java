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
import java.util.UUID;

public class RedisMessageRoundedSessionStartEvent extends GenericRedisMessage {
    private UUID sessionID;

    public UUID getSessionID() {
        return sessionID;
    }

    public RedisMessageRoundedSessionStartEvent(InetSocketAddress address, MessageOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(RedisMessageType.ROUNDED_SESSION_START_EVENT, address, origin);

        if(!RedisMessageRoundedSessionStartEvent.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            switch (key) {
                case ValidParameters.SESSION_ID -> this.sessionID = UUID.fromString(value.getAsString());
            }
        });
    }
    public RedisMessageRoundedSessionStartEvent(int messageVersion, String rawMessage, char[] privateKey, InetSocketAddress address, MessageOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(messageVersion, rawMessage, privateKey, RedisMessageType.ROUNDED_SESSION_START_EVENT, address, origin);

        if(!RedisMessageRoundedSessionStartEvent.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            switch (key) {
                case ValidParameters.SESSION_ID -> this.sessionID = UUID.fromString(value.getAsString());
            }
        });
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = super.toJSON();
        JsonObject parameters = new JsonObject();

        parameters.add(ValidParameters.SESSION_ID, new JsonPrimitive(this.sessionID.toString()));

        object.add(MasterValidParameters.PARAMETERS, parameters);

        return object;
    }

    public interface ValidParameters {
        String SESSION_ID = "id";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(SESSION_ID);

            return list;
        }
    }
}
