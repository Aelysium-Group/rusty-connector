package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.gameplay;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RankedPlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.gameplay.ITeam;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IRankedPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IPlayerRank;

import java.util.List;
import java.util.Vector;

public class Team implements ITeam {
    protected final Settings settings;
    protected final Vector<IRankedPlayer> players;

    public Team(Settings settings, Vector<IRankedPlayer> players) {
        this.settings = settings;
        this.players = players;
    }

    public boolean add(IRankedPlayer player) {
        if(full()) return false;

        this.players.add(player);
        return true;
    }

    public List<IRankedPlayer> players() {
        return this.players.stream().toList();
    }

    public boolean satisfactory() {
        return this.players.size() >= settings.min();
    }

    public boolean full() {
        return this.players.size() >= settings.max();
    }

    public JsonObject toJSON() {
        JsonObject object = new JsonObject();

        object.add("name", new JsonPrimitive(settings.name()));
        JsonArray players = new JsonArray();
        this.players().forEach(player -> players.add(new JsonPrimitive(player.uuid().toString())));
        object.add("players", players);

        return object;
    }
}
