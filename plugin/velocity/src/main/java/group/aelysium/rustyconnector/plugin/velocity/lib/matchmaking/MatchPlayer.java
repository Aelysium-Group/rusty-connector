package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking;

import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;

public class MatchPlayer<PlayerRank extends IPlayerRank> implements IMatchPlayer<PlayerRank> {
    private final IPlayer player;
    private final PlayerRank rank;
    private final String gameId;

    public MatchPlayer(IPlayer player, PlayerRank rank, String gameId) {
        this.player = player;
        this.rank = rank;
        this.gameId = gameId;
    }

    public IPlayer player() {
        return this.player;
    }

    public PlayerRank rank() {
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
