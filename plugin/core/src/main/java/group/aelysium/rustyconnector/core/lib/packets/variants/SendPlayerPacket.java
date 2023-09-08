package group.aelysium.rustyconnector.core.lib.packets.variants;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.core.lib.packets.PacketOrigin;
import group.aelysium.rustyconnector.core.lib.packets.PacketType;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import io.lettuce.core.KeyValue;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class SendPlayerPacket extends GenericPacket {
    private String targetFamilyName;
    private String uuid;

    public String targetFamilyName() {
        return targetFamilyName;
    }

    public String uuid() {
        return uuid;
    }

    public SendPlayerPacket(InetSocketAddress address, PacketOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(PacketType.SEND_PLAYER, address, origin);

        if(!SendPlayerPacket.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            switch (key) {
                case ValidParameters.TARGET_FAMILY_NAME -> this.targetFamilyName = value.getAsString();
                case ValidParameters.PLAYER_UUID -> this.uuid = value.getAsString();
            }
        });
    }
    public SendPlayerPacket(int messageVersion, String rawMessage, char[] privateKey, InetSocketAddress address, PacketOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(messageVersion, rawMessage, privateKey, PacketType.SEND_PLAYER, address, origin);

        if(!SendPlayerPacket.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            switch (key) {
                case ValidParameters.TARGET_FAMILY_NAME -> this.targetFamilyName = value.getAsString();
                case ValidParameters.PLAYER_UUID -> this.uuid = value.getAsString();
            }
        });
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = super.toJSON();
        JsonObject parameters = new JsonObject();

        parameters.add(ValidParameters.TARGET_FAMILY_NAME, new JsonPrimitive(this.targetFamilyName));
        parameters.add(ValidParameters.PLAYER_UUID, new JsonPrimitive(this.uuid));

        object.add(MasterValidParameters.PARAMETERS, parameters);

        return object;
    }

    public interface ValidParameters {
        String TARGET_FAMILY_NAME = "f";
        String PLAYER_UUID = "p";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(TARGET_FAMILY_NAME);
            list.add(PLAYER_UUID);

            return list;
        }
    }
}
