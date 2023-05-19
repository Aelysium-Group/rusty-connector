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

public class RedisMessageRoundedFamilyPreConnect extends GenericRedisMessage {
    private String familyName;
    private UUID uuid;

    public String getFamilyName() {
        return familyName;
    }

    public UUID getUUID() {
        return uuid;
    }

    public RedisMessageRoundedFamilyPreConnect(InetSocketAddress address, MessageOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(RedisMessageType.RND_PRE, address, origin);

        if(!RedisMessageRoundedFamilyPreConnect.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            switch (key) {
                case ValidParameters.FAMILY_NAME -> this.familyName = value.getAsString();
                case ValidParameters.UUID -> this.uuid = UUID.fromString(value.getAsString());
            }
        });
    }
    public RedisMessageRoundedFamilyPreConnect(int messageVersion, String rawMessage, char[] privateKey, InetSocketAddress address, MessageOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(messageVersion, rawMessage, privateKey, RedisMessageType.UNREGISTER_SERVER, address, origin);

        if(!RedisMessageRoundedFamilyPreConnect.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            switch (key) {
                case ValidParameters.FAMILY_NAME -> this.familyName = value.getAsString();
                case ValidParameters.UUID -> this.uuid = UUID.fromString(value.getAsString());
            }
        });
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = super.toJSON();
        JsonObject parameters = new JsonObject();

        parameters.add(ValidParameters.FAMILY_NAME, new JsonPrimitive(this.familyName));
        parameters.add(ValidParameters.UUID, new JsonPrimitive(this.uuid.toString()));

        object.add(MasterValidParameters.PARAMETERS, parameters);

        return object;
    }

    public interface ValidParameters {
        String FAMILY_NAME = "f";
        String UUID = "u";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(FAMILY_NAME);
            list.add(UUID);

            return list;
        }
    }
}
