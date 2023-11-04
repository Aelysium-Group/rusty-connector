package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import com.velocitypowered.api.proxy.Player;
import de.gesundkrank.jskills.IPlayer;
import group.aelysium.rustyconnector.api.velocity.players.IResolvablePlayer;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players.RankablePlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players.ScoreCard;
import one.microstream.reference.Lazy;

import java.util.*;

public class ResolvablePlayer implements IResolvablePlayer, IPlayer {
    protected UUID uuid;
    protected String username;
    protected Lazy<List<ScoreCard>> ranks = Lazy.Reference(null);

    protected ResolvablePlayer(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    public UUID uuid() { return this.uuid; }
    public String username() { return this.username; }

    public Optional<List<ScoreCard>> scorecards() {
        List<ScoreCard> ranks = Lazy.get(this.ranks);

        if(ranks == null) return Optional.empty();
        return Optional.of(ranks);
    }

    public ScoreCard scorecard(String game) {
        List<ScoreCard> ranks = Lazy.get(this.ranks);
        if(ranks == null)
            ranks = new ArrayList<>();

        Optional<ScoreCard> scorecard = ranks.stream().filter(rank -> rank.game().equals(game)).findAny();
        if(scorecard.isEmpty()) {
            scorecard = Optional.of(ScoreCard.create(game));
            ranks.add(scorecard.orElseThrow());

            // Store the new scorecard
            this.ranks = Lazy.Reference(ranks);
            Tinder.get().services().storage().store(this);
        }

        return scorecard.get();
    }

    /**
     * Fetches the ranked profile of this player.
     * @param game The game to fetch the rank for.
     * @return {@link RankablePlayer}
     */
    public RankablePlayer ranked(String game) {
        ScoreCard scorecard = this.scorecard(game);
        return new RankablePlayer(this, scorecard);
    }

    public Optional<Player> resolve() {
        return Tinder.get().velocityServer().getPlayer(this.uuid);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        ResolvablePlayer that = (ResolvablePlayer) object;
        return Objects.equals(uuid, that.uuid) && Objects.equals(username, that.username);
    }

    @Override
    public String toString() {
        return "<Player uuid="+this.uuid.toString()+" username="+this.username+">";
    }

    public static ResolvablePlayer from(Player player) {
        return new ResolvablePlayer(player.getUniqueId(), player.getUsername());
    }
    public static ResolvablePlayer from(UUID uuid, String username) {
        return new ResolvablePlayer(uuid, username);
    }
}