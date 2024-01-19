package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage;

import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ISortable;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IPlayerRankProfile;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class PlayerRankProfile implements ISortable, IPlayerRankProfile {
    protected UUID uuid;
    protected IPlayerRank<?> rank;

    public PlayerRankProfile(UUID uuid, IPlayerRank<?> rank) {
        this.uuid = uuid;
        this.rank = rank;
    }

    public UUID uuid() {
        return uuid;
    }

    public Optional<IPlayer> player() {
        try {
            return Optional.of(new Player.Reference(this.uuid).get());
        } catch (Exception ignore) {}

        return Optional.empty();
    }

    public IPlayerRank<?> rank() {
        return this.rank;
    }

    @Override
    public double sortIndex() {
        return (double) this.rank.rank();
    }

    @Override
    public int weight() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IPlayerRankProfile that = (IPlayerRankProfile) o;
        return Objects.equals(this.uuid, that.uuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    public static PlayerRankProfile from(UUID uuid, IPlayerRank<?> rank) {
        return new PlayerRankProfile(uuid, rank);
    }
}
