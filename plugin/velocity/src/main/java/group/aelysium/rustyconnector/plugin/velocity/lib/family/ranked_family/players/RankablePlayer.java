package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players;

import group.aelysium.rustyconnector.api.velocity.load_balancing.ISortable;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.Party;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.ResolvablePlayer;

import java.util.Objects;
import java.util.Optional;

public class RankablePlayer implements ISortable {
    protected ResolvablePlayer player;
    protected ScoreCard scorecard;

    public RankablePlayer(ResolvablePlayer player, ScoreCard scorecard) {
        this.player = player;
        this.scorecard = scorecard;
    }

    public Optional<Party> party() {
        try {
            PartyService partyService = Tinder.get().services().party().orElseThrow();
            return partyService.find(player.resolve().orElseThrow());
        } catch (Exception ignore) {}

        return Optional.empty();
    }

    public ResolvablePlayer player() {
        return player;
    }

    public ScoreCard scorecard() {
        return scorecard;
    }

    @Override
    public double sortIndex() {
        return this.scorecard().rating().getConservativeRating();
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
        RankablePlayer that = (RankablePlayer) o;
        return Objects.equals(this.player(), that.player());
    }

    @Override
    public int hashCode() {
        return Objects.hash(player);
    }
}
