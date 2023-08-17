package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.websocket.event_factory.events;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.websocket.event_factory.ViewportEvent;

import java.util.ArrayList;
import java.util.List;

public class ServerChatEvent extends ViewportEvent {
    private final Player player;
    private final String message;

    public ServerChatEvent(Player player, String message) {
        this.player = player;
        this.message = message;
    }

    @Override
    public String toJsonPacket() {
        JsonObject object = new JsonObject();

        object.add(ValidParameters.PLAYER_UUID, new JsonPrimitive(player.getUniqueId().toString()));
        object.add(ValidParameters.PLAYER_USERNAME, new JsonPrimitive(player.getUsername()));
        object.add(ValidParameters.PLAYER_UUID, new JsonPrimitive(message));

        return object.toString();
    }

    public interface ValidParameters {
        String PLAYER_UUID = "u";
        String PLAYER_USERNAME = "n";
        String MESSAGE = "m";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(PLAYER_UUID);
            list.add(PLAYER_USERNAME);
            list.add(MESSAGE);

            return list;
        }
    }
}
