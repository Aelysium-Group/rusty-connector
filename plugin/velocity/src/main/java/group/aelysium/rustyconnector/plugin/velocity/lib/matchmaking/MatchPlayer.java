package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking;

import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchPlayer;
import group.aelysium.rustyconnector.toolkit.core.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IVelocityPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;

import java.util.Objects;

public class MatchPlayer implements IMatchPlayer {
    private final IPlayer player;
    private final IVelocityPlayerRank rank;
    private final String gameId;

    public MatchPlayer(IPlayer player, IVelocityPlayerRank rank, String gameId) {
        this.player = player;
        this.rank = rank;
        this.gameId = gameId;
    }

    public IPlayer player() {
        return this.player;
    }

    public IVelocityPlayerRank gameRank() {
        return this.rank;
    }

    public String gameId() {
        return this.gameId;
    }

    @Override
    public double sortIndex() {
        return this.rank.rank();
    }

    @Override
    public int weight() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchPlayer that = (MatchPlayer) o;
        return Objects.equals(player, that.player) && Objects.equals(gameId, that.gameId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, gameId);
    }
}
