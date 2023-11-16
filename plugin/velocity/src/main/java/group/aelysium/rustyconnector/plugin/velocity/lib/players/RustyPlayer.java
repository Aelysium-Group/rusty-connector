package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import com.velocitypowered.api.proxy.Player;
import de.gesundkrank.jskills.IPlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.MySQLStorage;
import group.aelysium.rustyconnector.toolkit.velocity.players.IRustyPlayer;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players.RankablePlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players.ScoreCard;
import net.kyori.adventure.text.Component;
import one.microstream.reference.Lazy;

import java.util.*;

public class RustyPlayer implements IRustyPlayer, IPlayer {
    protected UUID uuid;
    protected String username;
    protected Lazy<List<ScoreCard>> ranks = Lazy.Reference(null);

    protected RustyPlayer(UUID uuid, String username) {
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
            ranks.add(scorecard.get());

            // Store the new scorecard
            this.ranks = Lazy.Reference(ranks);
            Tinder.get().services().storage().store(this);
        }

        return scorecard.get();
    }

    public void sendMessage(Component message) {
        try {
            this.resolve().orElseThrow().sendMessage(message);
        } catch (Exception ignore) {}
    }

    public void disconnect(Component reason) {
        try {
            this.resolve().orElseThrow().disconnect(reason);
        } catch (Exception ignore) {}
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

        RustyPlayer that = (RustyPlayer) object;
        return Objects.equals(uuid, that.uuid) && Objects.equals(username, that.username);
    }

    @Override
    public String toString() {
        return "<Player uuid="+this.uuid.toString()+" username="+this.username+">";
    }

    /**
     * Get a resolvable player from the provided player.
     * If no player is stored in storage, the player will be stored.
     * If a player was already stored in the storage, that player will be returned.
     * @param player The player to convert.
     * @return {@link RustyPlayer}
     */
    public static RustyPlayer from(Player player) {
        MySQLStorage mySQLStorage = Tinder.get().services().storage();
        RustyPlayer tempRustyPlayer = new RustyPlayer(player.getUniqueId(), player.getUsername());

        Set<RustyPlayer> players = mySQLStorage.root().players();
        if(players.add(tempRustyPlayer)) {
            mySQLStorage.store(players);
            return tempRustyPlayer;
        }

        return players.stream().filter(player1 -> player1.equals(tempRustyPlayer)).findAny().orElseThrow();
    }
    public static RustyPlayer from(UUID uuid, String username) {
        return new RustyPlayer(uuid, username);
    }
}