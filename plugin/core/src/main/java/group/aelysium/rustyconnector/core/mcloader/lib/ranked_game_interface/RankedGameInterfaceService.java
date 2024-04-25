package group.aelysium.rustyconnector.core.mcloader.lib.ranked_game_interface;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.core.TinderAdapterForCore;
import group.aelysium.rustyconnector.core.lib.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.core.lib.packets.RankedGame;
import group.aelysium.rustyconnector.core.mcloader.central.MCLoaderTinder;
import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketParameter;
import group.aelysium.rustyconnector.toolkit.mc_loader.events.ranked_game.RankedGameEndEvent;
import group.aelysium.rustyconnector.toolkit.mc_loader.ranked_game_interface.IRankedGameInterfaceService;
import group.aelysium.rustyconnector.toolkit.mc_loader.ranked_game_interface.MCLoaderMatchPlayer;

import java.util.*;

public class RankedGameInterfaceService implements IRankedGameInterfaceService {
    private UUID uuid;
    private Map<UUID, MCLoaderMatchPlayer> players;

    public Optional<UUID> uuid() {
        if(this.uuid == null) return Optional.empty();
        return Optional.of(this.uuid);
    }

    public Optional<Map<UUID, MCLoaderMatchPlayer>> players() {
        if(this.players == null) return Optional.empty();
        return Optional.of(this.players);
    }

    public void session(UUID uuid, Map<UUID, MCLoaderMatchPlayer> players) {
        this.uuid = uuid;
        this.players = players;
    }

    public void end(List<UUID> winners, List<UUID> losers, boolean unlock) {
        if(this.uuid == null) return;

        JsonObject object = new JsonObject();
        object.add("uuid", new JsonPrimitive(uuid.toString()));

        JsonArray winnerUUIDs = new JsonArray();
        JsonArray loserUUIDs = new JsonArray();

        for (UUID uuid : winners) {
            if(!players.containsKey(uuid)) continue;

            winnerUUIDs.add(uuid.toString());
        }
        for (UUID uuid : losers) {
            if(!players.containsKey(uuid)) continue;

            loserUUIDs.add(uuid.toString());
        }

        object.add("winners", winnerUUIDs);
        object.add("losers", loserUUIDs);

        MCLoaderTinder tinder = TinderAdapterForCore.getTinder();

        tinder.services().events().fireEvent(new RankedGameEndEvent(uuid, winners, losers, false));

        Packet packet = tinder.services().packetBuilder().newBuilder()
                .identification(BuiltInIdentifications.RANKED_GAME_END)
                .sendingToProxy()
                .parameter(RankedGame.End.Parameters.SESSION, new PacketParameter(object))
                .parameter(RankedGame.End.Parameters.UNLOCK, new PacketParameter(unlock))
                .build();
        tinder.services().magicLink().connection().orElseThrow().publish(packet);

        this.uuid = null;
        this.players = null;
    }
    public void end(List<UUID> winners, List<UUID> losers) {
        this.end(winners, losers, true);
    }

    public void endInTie(boolean unlock) {
        if(this.uuid == null) return;

        MCLoaderTinder tinder = TinderAdapterForCore.getTinder();

        tinder.services().events().fireEvent(new RankedGameEndEvent(uuid, List.of(), List.of(), true));

        Packet packet = tinder.services().packetBuilder().newBuilder()
                .identification(BuiltInIdentifications.RANKED_GAME_END_TIE)
                .sendingToProxy()
                .parameter(RankedGame.EndTied.Parameters.SESSION_UUID, new PacketParameter(uuid.toString()))
                .parameter(RankedGame.EndTied.Parameters.UNLOCK, new PacketParameter(unlock))
                .build();
        tinder.services().magicLink().connection().orElseThrow().publish(packet);

        this.uuid = null;
        this.players = null;
    }

    public void endInTie() {
        this.endInTie(true);
    }

    public void implode(String reason, boolean unlock) {
        MCLoaderTinder tinder = TinderAdapterForCore.getTinder();
        Packet packet = tinder.services().packetBuilder().newBuilder()
                .identification(BuiltInIdentifications.RANKED_GAME_IMPLODE)
                .sendingToProxy()
                .parameter(RankedGame.Imploded.Parameters.SESSION_UUID, new PacketParameter(uuid.toString()))
                .parameter(RankedGame.Imploded.Parameters.REASON, new PacketParameter(reason))
                .parameter(RankedGame.Imploded.Parameters.UNLOCK, new PacketParameter(unlock))
                .build();
        tinder.services().magicLink().connection().orElseThrow().publish(packet);
    }

    public void implode(String reason) {
        this.implode(reason, true);
    }

    public void kill() {
        this.uuid = null;
        this.players = null;
    }
}
