package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage;

import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ISortable;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.Party;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;

import java.util.Objects;
import java.util.Optional;

public class RankedPlayer<TPlayerRank extends IPlayerRank<?>> implements ISortable {
    protected Player player;
    protected TPlayerRank rank;

    public RankedPlayer(Player player, TPlayerRank rank) {
        this.player = player;
        this.rank = rank;
    }

    public Optional<Party> party() {
        try {
            PartyService partyService = Tinder.get().services().party().orElseThrow();
            return partyService.find(player.resolve().orElseThrow());
        } catch (Exception ignore) {}

        return Optional.empty();
    }

    public Player player() {
        return player;
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
        Optional<Party> party = this.party();
        if(party.isEmpty()) return 0;
        try {
            return this.party().orElseThrow().players().size();
        } catch (Exception ignore) {}

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RankedPlayer that = (RankedPlayer) o;
        return Objects.equals(this.player(), that.player());
    }

    @Override
    public int hashCode() {
        return Objects.hash(player);
    }
}
