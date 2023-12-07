package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.gameplay;

import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RankedPlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.gameplay.ITeam;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IPlayerRank;

import java.util.List;
import java.util.Vector;

public class Team implements ITeam<Player, RankedPlayer<IPlayerRank<?>>> {
    protected final Settings settings;
    protected final Vector<RankedPlayer<IPlayerRank<?>>> players;

    public Team(Settings settings, Vector<RankedPlayer<IPlayerRank<?>>> players) {
        this.settings = settings;
        this.players = players;
    }

    public boolean add(RankedPlayer<IPlayerRank<?>> player) {
        if(full()) return false;

        this.players.add(player);
        return true;
    }

    public List<RankedPlayer<IPlayerRank<?>>> players() {
        return this.players.stream().toList();
    }

    public boolean satisfactory() {
        return this.players.size() >= settings.min();
    }

    public boolean full() {
        return this.players.size() >= settings.max();
    }
}
