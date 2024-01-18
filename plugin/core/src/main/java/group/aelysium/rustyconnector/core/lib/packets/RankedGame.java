package group.aelysium.rustyconnector.core.lib.packets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketParameter;

import java.util.*;

public interface RankedGame {
    public record Session(UUID uuid, Map<UUID, String> players) {}
    public record EndedSession(UUID uuid, List<UUID> winners, List<UUID> losers) {}

    class Ready extends Packet.Wrapper {
        public Session session() {
            Gson gson = new Gson();
            JsonObject object = gson.fromJson(this.parameter(Parameters.SESSION).getAsString(), JsonObject.class);
            UUID uuid = UUID.fromString(object.get("uuid").getAsString());

            Map<UUID, String> players = new HashMap<>();

            JsonArray array = object.getAsJsonArray("players");
            array.forEach(entry -> {
                players.put(
                        UUID.fromString(entry.getAsJsonObject().get("uuid").getAsString()),
                        entry.getAsJsonObject().get("rank").getAsString()
                );
            });

            return new Session(uuid, players);
        }

        public Ready(Packet packet) {
            super(packet);
        }

        public interface Parameters {
            String SESSION = "s";
        }
    }

    class End extends Packet.Wrapper {
        public EndedSession session() {
            Gson gson = new Gson();
            JsonObject object = gson.fromJson(this.parameter(Ready.Parameters.SESSION).getAsString(), JsonObject.class);
            UUID uuid = UUID.fromString(object.get("uuid").getAsString());

            List<UUID> winners = new ArrayList<>();
            object.getAsJsonArray("winners").forEach(entry -> winners.add(UUID.fromString(entry.getAsJsonObject().get("uuid").getAsString())));

            List<UUID> losers = new ArrayList<>();
            object.getAsJsonArray("losers").forEach(entry -> losers.add(UUID.fromString(entry.getAsJsonObject().get("uuid").getAsString())));

            return new EndedSession(uuid, winners, losers);
        }

        public End(Packet packet) {
            super(packet);
        }

        public interface Parameters {
            String SESSION = "s";
        }
    }

    class Imploded extends Packet.Wrapper {
        public String reason() {
            return this.parameters().get(Parameters.REASON).getAsString();
        }
        public UUID sessionUUID() {
            return UUID.fromString(this.parameters().get(Parameters.SESSION_UUID).getAsString());
        }

        public Imploded(Packet packet) {
            super(packet);
        }

        public interface Parameters {
            String REASON = "r";
            String SESSION_UUID = "u";
        }
    }
}
