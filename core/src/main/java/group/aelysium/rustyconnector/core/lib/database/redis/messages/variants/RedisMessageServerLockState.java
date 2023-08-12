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

public class RedisMessageServerLockState extends GenericRedisMessage {
    private boolean lockState;

    private String serverName;

    public boolean lockState() {
        return lockState;
    }

    public String serverName() {
        return serverName;
    }

    public RedisMessageServerLockState(InetSocketAddress address, MessageOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(RedisMessageType.SEND_LOCK_STATE, address, origin);

        if(!RedisMessageServerLockState.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            switch (key) {
                case ValidParameters.SERVER_NAME -> this.serverName = value.getAsString();
                case ValidParameters.LOCK_STATE -> this.lockState = value.getAsBoolean();
            }
        });
    }
    public RedisMessageServerLockState(int messageVersion, String rawMessage, char[] privateKey, InetSocketAddress address, MessageOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(messageVersion, rawMessage, privateKey, RedisMessageType.SEND_LOCK_STATE, address, origin);

        if(!RedisMessageServerLockState.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            switch (key) {
                case ValidParameters.SERVER_NAME -> this.serverName = value.getAsString();
                case ValidParameters.LOCK_STATE -> this.lockState = value.getAsBoolean();
            }
        });
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = super.toJSON();
        JsonObject parameters = new JsonObject();

        parameters.add(ValidParameters.SERVER_NAME, new JsonPrimitive(this.serverName));
        parameters.add(ValidParameters.LOCK_STATE, new JsonPrimitive(this.lockState));

        object.add(MasterValidParameters.PARAMETERS, parameters);

        return object;
    }

    public interface ValidParameters {
        String LOCK_STATE = "l";

        String SERVER_NAME = "n";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(LOCK_STATE);
            list.add(SERVER_NAME);

            return list;
        }
    }
}
