package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players;

import de.gesundkrank.jskills.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ISortable;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.Party;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.RustyPlayer;

import java.util.Objects;
import java.util.Optional;

public class RankablePlayer implements ISortable, IPlayer {
    protected RustyPlayer player;
    protected ScoreCard scorecard;

    public RankablePlayer(RustyPlayer player, ScoreCard scorecard) {
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

    public RustyPlayer player() {
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


    public static RankablePlayer from(RustyPlayer player, String game) {
        return new RankablePlayer(player, player.scorecard(game));
    }
}
