package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking;

import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RandomizedPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;

import java.util.Objects;

public class MatchPlayer implements IMatchPlayer<IPlayerRank> {
    private final IPlayer player;
    private final IPlayerRank rank;
    private final String gameId;

    public MatchPlayer(IPlayer player, IPlayerRank rank, String gameId) {
        this.player = player;
        this.rank = rank;
        this.gameId = gameId;
    }

    public void markWin() {
        this.rank.markWin();
        Tinder.get().services().storage().database().ranks().set(this);
    }
    public void markLoss() {
        this.rank.markLoss();
        Tinder.get().services().storage().database().ranks().set(this);
    }

    public boolean isRandomizedPlayerRank() {
        return this.rank instanceof RandomizedPlayerRank;
    }

    public JsonObject rankToJSON() {
        return this.rank.toJSON();
    }

    public String rankSchemaName() {
        return this.rank.schemaName();
    }

    public IPlayer player() {
        return this.player;
    }

    public double rank() {
        return this.rank.rank();
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
