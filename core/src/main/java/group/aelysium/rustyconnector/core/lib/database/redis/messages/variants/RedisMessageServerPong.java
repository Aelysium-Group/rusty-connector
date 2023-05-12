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

public class RedisMessageServerPong extends GenericRedisMessage {
    private String serverName;
    private int playerCount;

    public String getServerName() {
        return serverName;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public RedisMessageServerPong(InetSocketAddress address, MessageOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(RedisMessageType.PONG, address, origin);

        if(!RedisMessageServerPong.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            switch (key) {
                case ValidParameters.SERVER_NAME -> this.serverName = value.getAsString();
                case ValidParameters.PLAYER_COUNT -> this.playerCount = value.getAsInt();
            }
        });
    }
    public RedisMessageServerPong(int messageVersion, String rawMessage, char[] privateKey, InetSocketAddress address, MessageOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(messageVersion, rawMessage, privateKey, RedisMessageType.PONG, address, origin);

        if(!RedisMessageServerPong.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            switch (key) {
                case ValidParameters.SERVER_NAME -> this.serverName = value.getAsString();
                case ValidParameters.PLAYER_COUNT -> this.playerCount = value.getAsInt();
            }
        });
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = super.toJSON();
        JsonObject parameters = new JsonObject();

        parameters.add(ValidParameters.SERVER_NAME, new JsonPrimitive(this.serverName));
        parameters.add(ValidParameters.PLAYER_COUNT, new JsonPrimitive(this.playerCount));

        object.add(MasterValidParameters.PARAMETERS, parameters);

        return object;
    }

    public interface ValidParameters {
        String SERVER_NAME = "n";
        String PLAYER_COUNT = "pc";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(SERVER_NAME);
            list.add(PLAYER_COUNT);

            return list;
        }
    }
}
