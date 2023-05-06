package group.aelysium.rustyconnector.core.lib.database.redis.messages.variants;

import com.google.gson.JsonElement;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MasterRedisMessageParameters;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import io.lettuce.core.KeyValue;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class RedisMessageServerRegister extends RedisMessage {
    private String familyName;
    private String serverName;
    private Integer softCap;
    private Integer hardCap;
    private Integer weight;

    public RedisMessageServerRegister(String rawMessage, char[] privateKey, RedisMessageType type, InetSocketAddress address, MessageOrigin origin, List<KeyValue<String, JsonElement>> parameters) {
        super(rawMessage, privateKey, type, address, origin);

        if(!RedisMessageServerRegister.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            switch (key) {
                case ValidParameters.FAMILY_NAME -> this.familyName = value.getAsString();
                case ValidParameters.SERVER_NAME -> this.serverName = value.getAsString();
                case ValidParameters.SOFT_CAP -> this.softCap = value.getAsInt();
                case ValidParameters.HARD_CAP -> this.hardCap = value.getAsInt();
                case ValidParameters.WEIGHT -> this.weight = value.getAsInt();
            }
        });
    }

    protected interface ValidParameters {
        String FAMILY_NAME = "family";
        String SERVER_NAME = "name";
        String SOFT_CAP = "scap";
        String HARD_CAP = "hcap";
        String WEIGHT = "w";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(FAMILY_NAME);
            list.add(SERVER_NAME);
            list.add(SOFT_CAP);
            list.add(HARD_CAP);
            list.add(WEIGHT);

            return list;
        }
    }
}
