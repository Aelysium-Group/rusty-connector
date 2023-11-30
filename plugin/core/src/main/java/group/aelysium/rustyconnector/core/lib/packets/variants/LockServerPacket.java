package group.aelysium.rustyconnector.core.lib.packets.variants;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketOrigin;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;
import io.lettuce.core.KeyValue;

import java.net.InetSocketAddress;
import java.util.List;

public class LockServerPacket extends GenericPacket {
    private String serverName;

    public String serverName() { return this.serverName; }

    public LockServerPacket(InetSocketAddress address, PacketOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(PacketType.LOCK_SERVER, address, origin);

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            if (key.equals(UnlockServerPacket.ValidParameters.SERVER_NAME)) {
                this.serverName = value.getAsString();
            }
        });
    }
    public LockServerPacket(int messageVersion, String rawMessage, InetSocketAddress address, PacketOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(messageVersion, rawMessage, PacketType.LOCK_SERVER, address, origin);

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            if (key.equals(UnlockServerPacket.ValidParameters.SERVER_NAME)) {
                this.serverName = value.getAsString();
            }
        });
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = super.toJSON();
        JsonObject parameters = new JsonObject();

        parameters.add(UnlockServerPacket.ValidParameters.SERVER_NAME, new JsonPrimitive(this.serverName));

        object.add(MasterValidParameters.PARAMETERS, parameters);

        return object;
    }
}
