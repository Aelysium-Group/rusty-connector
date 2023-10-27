package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.core.lib.Version;
import group.aelysium.rustyconnector.core.lib.packets.PacketOrigin;
import group.aelysium.rustyconnector.core.lib.packets.PacketType;
import group.aelysium.rustyconnector.core.lib.packets.variants.CoordinateRequestQueuePacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.SendPlayerPacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.ServerPingPacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.ServerPingResponsePacket;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import io.lettuce.core.KeyValue;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class WebSocketTicket {

    private String rawMessage;
    private Version version;
    private String auth;

    /*
     * Constructs a received RedisMessage.
     */
    protected WebSocketTicket(String rawMessage, String version, String auth) {
        this.rawMessage = rawMessage;
        this.version = new Version(version);
        this.auth = auth;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public Version getVersion() {
        return version;
    }

    public String getAuth() {
        return auth;
    }

    public static WebSocketTicket serialize(String rawMessage) {
        Gson gson = new Gson();
        JsonObject messageObject = gson.fromJson(rawMessage, JsonObject.class);

        return new WebSocketTicket(
                rawMessage,
                messageObject.get("version").getAsString(),
                messageObject.get("auth").getAsString()
        );
    }
}

