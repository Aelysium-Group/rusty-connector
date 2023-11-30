package group.aelysium.rustyconnector.core.lib.packets.variants;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketOrigin;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;
import group.aelysium.rustyconnector.toolkit.mc_loader.connection_intent.ConnectionIntent;
import io.lettuce.core.KeyValue;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RankedGameEndPacket extends GenericPacket {
    private String familyName;
    private String serverName;
    private UUID uuid;

    public String familyName() {
        return familyName;
    }
    public String serverName() {
        return serverName;
    }
    public UUID uuid() {
        return uuid;
    }

    public RankedGameEndPacket(InetSocketAddress address, PacketOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(PacketType.END_RANKED_GAME, address, origin);

        if(!RankedGameEndPacket.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            switch (key) {
                case ValidParameters.FAMILY_NAME -> this.familyName = value.getAsString();
                case ValidParameters.SERVER_NAME -> this.serverName = value.getAsString();
                case ValidParameters.GAME_UUID -> this.uuid = UUID.fromString(value.getAsString());
            }
        });
    }
    public RankedGameEndPacket(int messageVersion, String rawMessage, InetSocketAddress address, PacketOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(messageVersion, rawMessage, PacketType.END_RANKED_GAME, address, origin);

        if(!RankedGameEndPacket.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            switch (key) {
                case ValidParameters.FAMILY_NAME -> this.familyName = value.getAsString();
                case ValidParameters.SERVER_NAME -> this.serverName = value.getAsString();
                case ValidParameters.GAME_UUID -> this.uuid = UUID.fromString(value.getAsString());
            }
        });
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = super.toJSON();
        JsonObject parameters = new JsonObject();

        parameters.add(ValidParameters.FAMILY_NAME, new JsonPrimitive(this.familyName));
        parameters.add(ValidParameters.SERVER_NAME, new JsonPrimitive(this.serverName));
        parameters.add(ValidParameters.GAME_UUID, new JsonPrimitive(this.uuid().toString()));

        object.add(MasterValidParameters.PARAMETERS, parameters);

        return object;
    }

    public interface ValidParameters {
        String FAMILY_NAME = "f";
        String SERVER_NAME = "sn";
        String GAME_UUID = "id";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(FAMILY_NAME);
            list.add(SERVER_NAME);
            list.add(GAME_UUID);

            return list;
        }
    }
}
