package group.aelysium.rustyconnector.core.lib.packets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.core.mcloader.lib.ranked_game_interface.ranks.DefaultRankResolver;
import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.toolkit.mc_loader.ranked_game_interface.MCLoaderMatchPlayer;

import java.util.*;

public interface RankedGame {
    record Session(UUID uuid, Map<UUID, MCLoaderMatchPlayer> players) {}
    record EndedSession(UUID uuid, List<UUID> winners, List<UUID> losers, boolean tied) {}

    class Ready extends Packet.Wrapper {
        public Session session() {
            JsonObject object = this.parameter(Parameters.SESSION).getAsJsonObject();
            UUID uuid = UUID.fromString(object.get("uuid").getAsString());

            Map<UUID, MCLoaderMatchPlayer> players = new HashMap<>();

            JsonArray array = object.getAsJsonArray("players");
            array.forEach(entry -> {
                JsonObject entryObject = entry.getAsJsonObject();

                UUID player_uuid = UUID.fromString(entryObject.get("uuid").getAsString());
                players.put(
                    player_uuid,
                    new MCLoaderMatchPlayer(
                        player_uuid,
                        entryObject.get("username").getAsString(),
                        entryObject.get("schema").getAsString(),
                        DefaultRankResolver.New().resolve(entryObject.get("rank").getAsJsonObject())
                    )
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
            JsonObject object = this.parameter(Ready.Parameters.SESSION).getAsJsonObject();
            UUID uuid = UUID.fromString(object.get("uuid").getAsString());

            List<UUID> winners = new ArrayList<>();
            object.getAsJsonArray("winners").forEach(entry -> winners.add(UUID.fromString(entry.getAsString())));

            List<UUID> losers = new ArrayList<>();
            object.getAsJsonArray("losers").forEach(entry -> losers.add(UUID.fromString(entry.getAsString())));

            return new EndedSession(uuid, winners, losers, false);
        }

        public boolean unlock() {
            return this.parameter(Parameters.UNLOCK).getAsBoolean();
        }

        public End(Packet packet) {
            super(packet);
        }

        public interface Parameters {
            String SESSION = "s";
            String UNLOCK = "l";
        }
    }

    class EndTied extends Packet.Wrapper {
        public EndedSession session() {
            UUID uuid = UUID.fromString(this.parameter(Parameters.SESSION_UUID).getAsString());

            return new EndedSession(uuid, List.of(), List.of(), true);
        }

        public boolean unlock() {
            return this.parameter(Parameters.UNLOCK).getAsBoolean();
        }

        public EndTied(Packet packet) {
            super(packet);
        }

        public interface Parameters {
            String SESSION_UUID = "s";
            String UNLOCK = "l";
        }
    }

    class Imploded extends Packet.Wrapper {
        public String reason() {
            return this.parameters().get(Parameters.REASON).getAsString();
        }
        public UUID sessionUUID() {
            return UUID.fromString(this.parameters().get(Parameters.SESSION_UUID).getAsString());
        }

        public boolean unlock() {
            return this.parameter(Parameters.UNLOCK).getAsBoolean();
        }

        public Imploded(Packet packet) {
            super(packet);
        }

        public interface Parameters {
            String REASON = "r";
            String SESSION_UUID = "u";
            String UNLOCK = "l";
        }
    }
}
