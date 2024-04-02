package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking;

import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;

public class MatchPlayer implements IMatchPlayer<IPlayerRank> {
    private final IPlayer player;
    private final IPlayerRank rank;
    private final String gameId;

    public MatchPlayer(IPlayer player, IPlayerRank rank, String gameId) {
        this.player = player;
        this.rank = rank;
        this.gameId = gameId;
    }

    public IPlayer player() {
        return this.player;
    }

    public IPlayerRank rank() {
        return this.rank;
    }

    public String gameId() {
        return this.gameId;
    }

    @Override
    public double sortIndex() {
        return rank.rank();
    }

    @Override
    public int weight() {
        return 0;
    }
}
