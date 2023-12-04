package group.aelysium.rustyconnector.core.lib.packets.variants;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketOrigin;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;
import io.lettuce.core.KeyValue;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class RankedGameAssociatePacket extends GenericPacket {
    private UUID uuid;

    public UUID uuid() {
        return uuid;
    }

    public RankedGameAssociatePacket(InetSocketAddress address, PacketOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(PacketType.ASSOCIATE_RANKED_GAME, address, origin);

        if(!RankedGameAssociatePacket.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            switch (key) {
                case ValidParameters.GAME_UUID -> {
                    if(value.getAsString().equals("null")) this.uuid = null;
                    this.uuid = UUID.fromString(value.getAsString());
                }
            }
        });
    }
    public RankedGameAssociatePacket(int messageVersion, String rawMessage, InetSocketAddress address, PacketOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(messageVersion, rawMessage, PacketType.ASSOCIATE_RANKED_GAME, address, origin);

        if(!RankedGameAssociatePacket.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            switch (key) {
                case ValidParameters.GAME_UUID -> {
                    if(value.getAsString().equals("null")) this.uuid = null;
                    this.uuid = UUID.fromString(value.getAsString());
                }
            }
        });
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = super.toJSON();
        JsonObject parameters = new JsonObject();

        parameters.add(ValidParameters.GAME_UUID, new JsonPrimitive(this.uuid().toString()));

        object.add(MasterValidParameters.PARAMETERS, parameters);

        return object;
    }

    public interface ValidParameters {
        String GAME_UUID = "id";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(GAME_UUID);

            return list;
        }
    }
}
