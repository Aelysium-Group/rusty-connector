package group.aelysium.rustyconnector.core.lib.packets.variants;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.core.lib.packets.PacketOrigin;
import group.aelysium.rustyconnector.core.lib.packets.PacketType;
import io.lettuce.core.KeyValue;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class UnlockServerPacket extends GenericPacket {
    private String serverName;

    public String serverName() { return this.serverName; }

    public UnlockServerPacket(InetSocketAddress address, PacketOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(PacketType.UNLOCK_SERVER, address, origin);

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            if (key.equals(ValidParameters.SERVER_NAME)) {
                this.serverName = value.getAsString();
            }
        });
    }
    public UnlockServerPacket(int messageVersion, String rawMessage, InetSocketAddress address, PacketOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(messageVersion, rawMessage, PacketType.UNLOCK_SERVER, address, origin);

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            if (key.equals(ValidParameters.SERVER_NAME)) {
                this.serverName = value.getAsString();
            }
        });
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = super.toJSON();
        JsonObject parameters = new JsonObject();

        parameters.add(ValidParameters.SERVER_NAME, new JsonPrimitive(this.serverName));

        object.add(MasterValidParameters.PARAMETERS, parameters);

        return object;
    }

    public interface ValidParameters {
        String SERVER_NAME = "s";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(SERVER_NAME);

            return list;
        }
    }
}
