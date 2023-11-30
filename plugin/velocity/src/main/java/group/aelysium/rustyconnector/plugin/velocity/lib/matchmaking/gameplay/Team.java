package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.gameplay;

import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RankedPlayer;

import java.util.Vector;

public class Team {
    protected final Settings settings;
    protected final Vector<RankedPlayer> players;

    public Team(Settings settings, Vector<RankedPlayer> players) {
        this.settings = settings;
        this.players = players;
    }

    public boolean add(RankedPlayer player) {
        if(full()) return false;

        this.players.add(player);
        return true;
    }

    public boolean satisfactory() {
        return this.players.size() > settings.min();
    }

    public boolean full() {
        return this.players.size() > settings.max();
    }

    public record Settings(int min, int max) {}
}
