package group.aelysium.rustyconnector.core.lib.packets.variants;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.api.core.packet.PacketOrigin;
import group.aelysium.rustyconnector.api.core.packet.PacketType;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import io.lettuce.core.KeyValue;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class CoordinateRequestQueuePacket extends GenericPacket {
    private String targetUsername;
    private String targetServer;
    private String sourceUsername;

    public String targetUsername() {
        return targetUsername;
    }

    public String sourceUsername() {
        return sourceUsername;
    }

    public String targetServer() {
        return targetServer;
    }

    public CoordinateRequestQueuePacket(InetSocketAddress address, PacketOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(PacketType.COORDINATE_REQUEST_QUEUE, address, origin);

        if(!CoordinateRequestQueuePacket.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            switch (key) {
                case ValidParameters.TARGET_SERVER -> this.targetServer = value.getAsString();
                case ValidParameters.TARGET_USERNAME -> this.targetUsername = value.getAsString();
                case ValidParameters.SOURCE_USERNAME -> this.sourceUsername = value.getAsString();
            }
        });
    }
    public CoordinateRequestQueuePacket(int messageVersion, String rawMessage, InetSocketAddress address, PacketOrigin origin, List<KeyValue<String, JsonPrimitive>> parameters) {
        super(messageVersion, rawMessage, PacketType.COORDINATE_REQUEST_QUEUE, address, origin);

        if(!CoordinateRequestQueuePacket.validateParameters(ValidParameters.toList(), parameters))
            throw new IllegalStateException("Unable to construct Redis message! There are missing parameters!");

        parameters.forEach(entry -> {
            String key = entry.getKey();
            JsonPrimitive value = entry.getValue();

            switch (key) {
                case ValidParameters.TARGET_SERVER -> this.targetServer = value.getAsString();
                case ValidParameters.TARGET_USERNAME -> this.targetUsername = value.getAsString();
                case ValidParameters.SOURCE_USERNAME -> this.sourceUsername = value.getAsString();
            }
        });
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = super.toJSON();
        JsonObject parameters = new JsonObject();

        parameters.add(ValidParameters.TARGET_SERVER, new JsonPrimitive(this.targetServer));
        parameters.add(ValidParameters.TARGET_USERNAME, new JsonPrimitive(this.targetUsername));
        parameters.add(ValidParameters.SOURCE_USERNAME, new JsonPrimitive(this.sourceUsername));

        object.add(MasterValidParameters.PARAMETERS, parameters);

        return object;
    }

    public interface ValidParameters {
        String TARGET_SERVER = "ts";
        String TARGET_USERNAME = "tp";
        String SOURCE_USERNAME = "sp";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(TARGET_SERVER);
            list.add(TARGET_USERNAME);
            list.add(SOURCE_USERNAME);

            return list;
        }
    }
}
