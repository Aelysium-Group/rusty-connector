package group.aelysium.rustyconnector.api.velocity.lib.server.viewport.events;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.websocket.ViewportEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServerPlayerCountEvent extends ViewportEvent {
    private final PlayerServer server;
    private final int playerCount; // We specifically use a separate param here because we don't want the reference to change before the event can be sent.

    public ServerPlayerCountEvent(PlayerServer server, int playerCount) {
        this.playerCount = playerCount;
        this.server = server;
    }

    @Override
    public String toJsonPacket() {
        JsonObject object = new JsonObject();

        object.add("id", new JsonPrimitive(server.id().toString()));
        object.add("name", new JsonPrimitive(server.serverInfo().getName()));
        object.add("player_count", new JsonPrimitive(playerCount));

        return object.toString();
    }

    public PlayerServer server() {
        return this.server;
    }

    public int playerCount() {
        return this.playerCount;
    }
}
