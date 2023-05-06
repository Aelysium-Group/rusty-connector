package group.aelysium.rustyconnector.core.lib.database.redis.messages.variants;

import com.google.gson.JsonElement;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import io.lettuce.core.KeyValue;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class RedisMessageServerPong extends RedisMessage {
    private String serverName;
    private int playerCount;

    public String getServerName() {
        return serverName;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public RedisMessageServerPong(InetSocketAddress address, MessageOrigin origin, List<KeyValue<String, JsonElement>> parameters) {
        super(RedisMessageType.PONG, address, origin);

        if(!RedisMessageServerPong.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            switch (key) {
                case ValidParameters.SERVER_NAME -> this.serverName = value.getAsString();
                case ValidParameters.PLAYER_COUNT -> this.playerCount = value.getAsInt();
            }
        });
    }
    public RedisMessageServerPong(int messageVersion, String rawMessage, char[] privateKey, InetSocketAddress address, MessageOrigin origin, List<KeyValue<String, JsonElement>> parameters) {
        super(messageVersion, rawMessage, privateKey, RedisMessageType.PONG, address, origin);

        if(!RedisMessageServerPong.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            switch (key) {
                case ValidParameters.SERVER_NAME -> this.serverName = value.getAsString();
                case ValidParameters.PLAYER_COUNT -> this.playerCount = value.getAsInt();
            }
        });
    }

    public interface ValidParameters {
        String SERVER_NAME = "name";
        String PLAYER_COUNT = "player-count";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(SERVER_NAME);
            list.add(PLAYER_COUNT);

            return list;
        }
    }
}
