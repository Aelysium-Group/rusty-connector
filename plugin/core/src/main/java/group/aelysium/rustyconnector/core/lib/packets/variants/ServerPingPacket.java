package group.aelysium.rustyconnector.core.lib.packets.variants;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.toolkit.mc_loader.connection_intent.ConnectionIntent;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketOrigin;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import io.lettuce.core.KeyValue;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class ServerPingPacket extends GenericPacket {
    private String serverName;
    private ConnectionIntent intent;
    private String magicConfigName;
    private Integer playerCount;

    public String serverName() {
        return serverName;
    }
    public ConnectionIntent intent() {
        return intent;
    }
    public String magicConfigName() {
        return magicConfigName;
    }
    public Integer playerCount() {
        return playerCount;
    }

    public ServerPingPacket(InetSocketAddress address, PacketOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(PacketType.PING, address, origin);

        if(!ServerPingPacket.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            switch (key) {
                case ValidParameters.SERVER_NAME -> this.serverName = value.getAsString();
                case ValidParameters.INTENT -> this.intent = ConnectionIntent.valueOf(value.getAsString());
                case ValidParameters.MAGIC_CONFIG_NAME -> this.magicConfigName = value.getAsString();
                case ValidParameters.PLAYER_COUNT -> this.playerCount = value.getAsInt();
            }
        });
    }
    public ServerPingPacket(int messageVersion, String rawMessage, InetSocketAddress address, PacketOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(messageVersion, rawMessage, PacketType.PING, address, origin);

        if(!ServerPingPacket.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            switch (key) {
                case ValidParameters.SERVER_NAME -> this.serverName = value.getAsString();
                case ValidParameters.INTENT -> this.intent = ConnectionIntent.valueOf(value.getAsString());
                case ValidParameters.MAGIC_CONFIG_NAME -> this.magicConfigName = value.getAsString();
                case ValidParameters.PLAYER_COUNT -> this.playerCount = value.getAsInt();
            }
        });
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = super.toJSON();
        JsonObject parameters = new JsonObject();

        parameters.add(ValidParameters.SERVER_NAME, new JsonPrimitive(this.serverName));
        parameters.add(ValidParameters.INTENT, new JsonPrimitive(this.intent.toString()));
        parameters.add(ValidParameters.MAGIC_CONFIG_NAME, new JsonPrimitive(this.magicConfigName));
        parameters.add(ValidParameters.PLAYER_COUNT, new JsonPrimitive(this.playerCount));

        object.add(MasterValidParameters.PARAMETERS, parameters);

        return object;
    }

    public interface ValidParameters {
        String SERVER_NAME = "s";
        String MAGIC_CONFIG_NAME = "c";
        String INTENT = "i";
        String PLAYER_COUNT = "pc";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(MAGIC_CONFIG_NAME);
            list.add(INTENT);
            list.add(PLAYER_COUNT);

            return list;
        }
    }
}
