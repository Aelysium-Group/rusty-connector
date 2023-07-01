package group.aelysium.rustyconnector.core.lib.database.redis.messages.variants;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import io.lettuce.core.KeyValue;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class RedisMessageServerPing extends GenericRedisMessage {
    private String familyName;
    private String serverName;

    private String parentFamilyName;
    private Integer softCap;
    private Integer hardCap;
    private Integer weight;

    public String getFamilyName() {
        return familyName;
    }

    public String getServerName() {
        return serverName;
    }

    public String getParentFamilyName() {
        return parentFamilyName;
    }

    public Integer getSoftCap() {
        return softCap;
    }

    public Integer getHardCap() {
        return hardCap;
    }

    public Integer getWeight() {
        return weight;
    }

    public RedisMessageServerPing(InetSocketAddress address, MessageOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(RedisMessageType.PING, address, origin);

        if(!RedisMessageServerPing.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            switch (key) {
                case ValidParameters.FAMILY_NAME -> this.familyName = value.getAsString();
                case ValidParameters.SERVER_NAME -> this.serverName = value.getAsString();
                case ValidParameters.PARENT_FAMILY_NAME -> this.parentFamilyName = value.getAsString();
                case ValidParameters.SOFT_CAP -> this.softCap = value.getAsInt();
                case ValidParameters.HARD_CAP -> this.hardCap = value.getAsInt();
                case ValidParameters.WEIGHT -> this.weight = value.getAsInt();
            }
        });
    }
    public RedisMessageServerPing(int messageVersion, String rawMessage, char[] privateKey, InetSocketAddress address, MessageOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(messageVersion, rawMessage, privateKey, RedisMessageType.PING, address, origin);

        if(!RedisMessageServerPing.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            switch (key) {
                case ValidParameters.FAMILY_NAME -> this.familyName = value.getAsString();
                case ValidParameters.SERVER_NAME -> this.serverName = value.getAsString();
                case ValidParameters.PARENT_FAMILY_NAME -> this.parentFamilyName = value.getAsString();
                case ValidParameters.SOFT_CAP -> this.softCap = value.getAsInt();
                case ValidParameters.HARD_CAP -> this.hardCap = value.getAsInt();
                case ValidParameters.WEIGHT -> this.weight = value.getAsInt();
            }
        });
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = super.toJSON();
        JsonObject parameters = new JsonObject();

        parameters.add(ValidParameters.FAMILY_NAME, new JsonPrimitive(this.familyName));
        parameters.add(ValidParameters.SERVER_NAME, new JsonPrimitive(this.serverName));
        parameters.add(ValidParameters.PARENT_FAMILY_NAME, new JsonPrimitive(this.parentFamilyName));
        parameters.add(ValidParameters.SOFT_CAP, new JsonPrimitive(this.softCap));
        parameters.add(ValidParameters.HARD_CAP, new JsonPrimitive(this.hardCap));
        parameters.add(ValidParameters.WEIGHT, new JsonPrimitive(this.weight));

        object.add(MasterValidParameters.PARAMETERS, parameters);

        return object;
    }

    public interface ValidParameters {
        String FAMILY_NAME = "f";
        String SERVER_NAME = "n";

        String PARENT_FAMILY_NAME = "pf";
        String SOFT_CAP = "sc";
        String HARD_CAP = "hc";
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
