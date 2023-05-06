package group.aelysium.rustyconnector.core.lib.database.redis.messages.variants;

import com.google.gson.JsonElement;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import io.lettuce.core.KeyValue;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class RedisMessageFamilyRegister extends RedisMessage {
    private String familyName;

    public String getFamilyName() {
        return familyName;
    }

    public RedisMessageFamilyRegister(InetSocketAddress address, MessageOrigin origin, List<KeyValue<String, JsonElement>> parameters) {
        super(RedisMessageType.REG_FAMILY, address, origin);

        if(!RedisMessageFamilyRegister.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            switch (key) {
                case ValidParameters.FAMILY_NAME -> this.familyName = value.getAsString();
            }
        });
    }
    public RedisMessageFamilyRegister(String rawMessage, char[] privateKey, InetSocketAddress address, MessageOrigin origin, List<KeyValue<String, JsonElement>> parameters) {
        super(rawMessage, privateKey, RedisMessageType.REG_FAMILY, address, origin);

        if(!RedisMessageFamilyRegister.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            switch (key) {
                case ValidParameters.FAMILY_NAME -> this.familyName = value.getAsString();
            }
        });
    }

    public interface ValidParameters {
        String FAMILY_NAME = "family";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(FAMILY_NAME);

            return list;
        }
    }
}
