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
    private ConnectionIntent intent;
    private String familyName;
    private String serverName;
    private Integer softCap;
    private Integer hardCap;
    private Integer weight;
    private Integer playerCount;

    public ConnectionIntent intent() {
        return intent;
    }
    public String familyName() {
        return familyName;
    }

    public String serverName() {
        return serverName;
    }

    public Integer softCap() {
        return softCap;
    }

    public Integer hardCap() {
        return hardCap;
    }

    public Integer weight() {
        return weight;
    }

    public Integer playerCount() {
        return playerCount;
    }

    public RedisMessageServerPing(InetSocketAddress address, MessageOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(RedisMessageType.PING, address, origin);

        if(!RedisMessageServerPing.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            switch (key) {
                case ValidParameters.INTENT -> this.intent = ConnectionIntent.valueOf(value.getAsString());
                case ValidParameters.FAMILY_NAME -> this.familyName = value.getAsString();
                case ValidParameters.SERVER_NAME -> this.serverName = value.getAsString();
                case ValidParameters.SOFT_CAP -> this.softCap = value.getAsInt();
                case ValidParameters.HARD_CAP -> this.hardCap = value.getAsInt();
                case ValidParameters.WEIGHT -> this.weight = value.getAsInt();
                case ValidParameters.PLAYER_COUNT -> this.playerCount = value.getAsInt();
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
                case ValidParameters.INTENT -> this.intent = ConnectionIntent.valueOf(value.getAsString());
                case ValidParameters.FAMILY_NAME -> this.familyName = value.getAsString();
                case ValidParameters.SERVER_NAME -> this.serverName = value.getAsString();
                case ValidParameters.SOFT_CAP -> this.softCap = value.getAsInt();
                case ValidParameters.HARD_CAP -> this.hardCap = value.getAsInt();
                case ValidParameters.WEIGHT -> this.weight = value.getAsInt();
                case ValidParameters.PLAYER_COUNT -> this.playerCount = value.getAsInt();
            }
        });
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = super.toJSON();
        JsonObject parameters = new JsonObject();

        parameters.add(ValidParameters.INTENT, new JsonPrimitive(this.intent.toString()));
        parameters.add(ValidParameters.FAMILY_NAME, new JsonPrimitive(this.familyName));
        parameters.add(ValidParameters.SERVER_NAME, new JsonPrimitive(this.serverName));
        parameters.add(ValidParameters.SOFT_CAP, new JsonPrimitive(this.softCap));
        parameters.add(ValidParameters.HARD_CAP, new JsonPrimitive(this.hardCap));
        parameters.add(ValidParameters.WEIGHT, new JsonPrimitive(this.weight));
        parameters.add(ValidParameters.PLAYER_COUNT, new JsonPrimitive(this.playerCount));

        object.add(MasterValidParameters.PARAMETERS, parameters);

        return object;
    }

    public interface ValidParameters {
        String FAMILY_NAME = "f";
        String SERVER_NAME = "n";
        String SOFT_CAP = "sc";
        String HARD_CAP = "hc";
        String WEIGHT = "w";
        String INTENT = "i";
        String PLAYER_COUNT = "pc";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(FAMILY_NAME);
            list.add(SERVER_NAME);
            list.add(SOFT_CAP);
            list.add(HARD_CAP);
            list.add(WEIGHT);
            list.add(INTENT);
            list.add(PLAYER_COUNT);

            return list;
        }
    }

    public enum ConnectionIntent {
        CONNECT,
        DISCONNECT
    }
}
