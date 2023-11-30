package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.matchmakers;

import group.aelysium.rustyconnector.core.lib.algorithm.SingleSort;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.RankedGame;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.gameplay.GameMatch;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.gameplay.Team;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RankedPlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.IPlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

import java.util.List;
import java.util.Vector;

public abstract class MatchMaker<TPlayerRank extends IPlayerRank<?>> {
    protected final Settings settings;
    protected final int minPlayersPerGame;
    protected final int maxPlayersPerGame;
    protected Vector<RankedPlayer<TPlayerRank>> items = new Vector<>();

    public MatchMaker(Settings settings) {
        this.settings = settings;

        final int[] min = {0};
        final int[] max = {0};
        settings.teams.forEach(team -> {
            min[0] = min[0] + team.min();
            max[0] = max[0] + team.max();
        });

        this.minPlayersPerGame = min[0];
        this.maxPlayersPerGame = max[0];
    }

    /**
     * Using the players contained in the matchmaker, make a game.
     */
    public abstract GameMatch make();

    public boolean minimumPlayersExist() {
        return this.items.size() > minPlayersPerGame;
    }

    public abstract void completeSort();

    /**
     * Inserts a player into the matchmaker.
     *
     * Specifically, this method performs a {@link SingleSort#sort(List, int)} and injects the player into
     * an approximation of the best place for them to reside.
     * Thus reducing how frequently you'll need to perform a full sort on the metchmaker.
     * @param player The player to add.
     */
    public void add(RankedPlayer<TPlayerRank> player) {
        if(this.items.contains(player)) return;

        this.items.add(player);
        int index = this.items.size() - 1;

        SingleSort.sort(this.items, index);
    }

    public void remove(RankedPlayer<TPlayerRank> item) {
        this.items.remove(item);
    }

    public int size() {
        return this.items.size();
    }

    public List<RankedPlayer<TPlayerRank>> dump() {
        return this.items;
    }

    public boolean contains(MCLoader item) {
        return this.items.contains(item);
    }


    public record Settings (
            String name,
            RankedGame.RankerType type,
            List<Team.Settings> teams,
            double variance,
            LiquidTimestamp interval
    ) {}
}