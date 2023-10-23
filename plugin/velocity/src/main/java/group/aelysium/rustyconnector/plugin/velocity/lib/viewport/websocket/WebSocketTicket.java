package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.api.velocity.lib.Version;

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

