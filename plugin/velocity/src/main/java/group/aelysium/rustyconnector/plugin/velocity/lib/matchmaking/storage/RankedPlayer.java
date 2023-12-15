package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage;

import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ISortable;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IRankedPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IPlayerRank;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class RankedPlayer<TPlayerRank extends IPlayerRank<?>> implements ISortable, IRankedPlayer<Player, TPlayerRank> {
    protected UUID uuid;
    protected TPlayerRank rank;

    public RankedPlayer(UUID uuid, TPlayerRank rank) {
        this.uuid = uuid;
        this.rank = rank;
    }

    public UUID uuid() {
        return uuid;
    }

    public Optional<Player> player() {
        try {
            return Optional.of((Player) new Player.Reference(this.uuid).get());
        } catch (Exception ignore) {}

        return Optional.empty();
    }

    public TPlayerRank rank() {
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
        RankedPlayer<?> that = (RankedPlayer<?>) o;
        return Objects.equals(this.uuid, that.uuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    public static <TPlayerRank extends IPlayerRank<?>> RankedPlayer<TPlayerRank> from(UUID uuid, TPlayerRank rank) {
        return new RankedPlayer<>(uuid, rank);
    }
}
