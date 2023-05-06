package group.aelysium.rustyconnector.core.lib.database.redis.messages.variants;

import com.google.gson.JsonElement;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import io.lettuce.core.KeyValue;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class RedisMessageSendPlayer extends RedisMessage {
    private String familyName;
    private String uuid;

    public String getFamilyName() {
        return familyName;
    }

    public String getUUID() {
        return uuid;
    }

    public RedisMessageSendPlayer(InetSocketAddress address, MessageOrigin origin, List<KeyValue<String, JsonElement>> parameters) {
        super(RedisMessageType.SEND, address, origin);

        if(!RedisMessageSendPlayer.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            switch (key) {
                case ValidParameters.TARGET_FAMILY_NAME -> this.familyName = value.getAsString();
                case ValidParameters.PLAYER_UUID -> this.uuid = value.getAsString();
            }
        });
    }
    public RedisMessageSendPlayer(int messageVersion, String rawMessage, char[] privateKey, InetSocketAddress address, MessageOrigin origin, List<KeyValue<String, JsonElement>> parameters) {
        super(messageVersion, rawMessage, privateKey, RedisMessageType.SEND, address, origin);

        if(!RedisMessageSendPlayer.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            switch (key) {
                case ValidParameters.TARGET_FAMILY_NAME -> this.familyName = value.getAsString();
                case ValidParameters.PLAYER_UUID -> this.uuid = value.getAsString();
            }
        });
    }

    public interface ValidParameters {
        String TARGET_FAMILY_NAME = "family";
        String PLAYER_UUID = "uuid";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(TARGET_FAMILY_NAME);
            list.add(PLAYER_UUID);

            return list;
        }
    }
}
