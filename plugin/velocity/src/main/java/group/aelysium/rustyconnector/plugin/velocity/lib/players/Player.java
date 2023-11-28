package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.MySQLStorage;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players.RankablePlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players.ScoreCard;
import net.kyori.adventure.text.Component;
import one.microstream.reference.Lazy;

import java.util.*;

public class Player implements IPlayer, de.gesundkrank.jskills.IPlayer {
    protected UUID uuid;
    protected String username;
    protected Lazy<List<ScoreCard>> ranks = Lazy.Reference(null);

    protected Player(UUID uuid, String username) {
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

    public Optional<com.velocitypowered.api.proxy.Player> resolve() {
        return Tinder.get().velocityServer().getPlayer(this.uuid);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Player that = (Player) object;
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
     *
     * This method really only every needs to be used the first time a player connects to the proxy.
     * @param player The player to convert.
     * @return {@link Player}
     */
    public static Player from(com.velocitypowered.api.proxy.Player player) {
        MySQLStorage mySQLStorage = Tinder.get().services().storage();
        Player tempPlayer = new Player(player.getUniqueId(), player.getUsername());

        Set<Player> players = mySQLStorage.root().players();
        if(players.add(tempPlayer)) {
            mySQLStorage.store(players);
            return tempPlayer;
        }

        return players.stream().filter(player1 -> player1.equals(tempPlayer)).findAny().orElseThrow();
    }
    public static Player from(UUID uuid, String username) {
        return new Player(uuid, username);
    }

    public static class UUIDReference extends group.aelysium.rustyconnector.toolkit.velocity.util.Reference<Player, UUID> {
        public UUIDReference(UUID uuid) {
            super(uuid);
        }

        /**
         * Gets the family referenced.
         * If no family could be found, this will throw an exception.
         * @return {@link Family}
         * @throws java.util.NoSuchElementException If the family can't be found.
         */
        public Player get() {
            return Tinder.get().services().player().fetch(this.referencer).orElseThrow();
        }
    }

    public static class UsernameReference extends group.aelysium.rustyconnector.toolkit.velocity.util.Reference<Player, String> {
        public UsernameReference(String username) {
            super(username);
        }

        public Player get() {
            return Tinder.get().services().player().fetch(this.referencer).orElseThrow();
        }
    }
}