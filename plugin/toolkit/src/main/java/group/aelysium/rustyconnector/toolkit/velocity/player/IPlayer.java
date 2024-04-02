package group.aelysium.rustyconnector.toolkit.velocity.player;

import group.aelysium.rustyconnector.toolkit.RustyConnector;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import net.kyori.adventure.text.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public interface IPlayer {
    UUID uuid();
    String username();

    /**
     * Resolves the rusty player into a currently active online player.
     * If the player isn't online, the resolution will be empty.
     * @return {@link Optional<com.velocitypowered.api.proxy.Player>}
     */
    Optional<com.velocitypowered.api.proxy.Player> resolve();

    /**
     * Check whether the Player is online.
     * @return `true` if the player is online. `false` otherwise.
     */
    boolean online();

    /**
     * Convenience method that will resolve the player and then send a message to them if the resolution was successful.
     * If the resolution was not successful, nothing will happen.
     * @param message The message to send.
     */
    void sendMessage(Component message);

    /**
     * Convenience method that will resolve the player and then disconnect them if the resolution was successful.
     * If the resolution was not successful, nothing will happen.
     * @param reason The message to send as the reason for the disconnection.
     */
    void disconnect(Component reason);

    /**
     * Convenience method that will resolve the player and then return their MCLoader if there is one.
     */
    Optional<? extends IMCLoader> server();

    /**
     * Fetches the player's rank for a specific game.
     * @param gameId The game id to fetch the player's rank from.
     */
    Optional<? extends IPlayerRank> rank(String gameId);

    class Reference extends group.aelysium.rustyconnector.toolkit.velocity.util.Reference<IPlayer, UUID> {
        public Reference(UUID uuid) {
            super(uuid);
        }

        public <TPlayer extends IPlayer> TPlayer get() {
            return (TPlayer) RustyConnector.Toolkit.proxy().orElseThrow().services().player().fetch(this.referencer).orElseThrow();
        }
    }

    class UsernameReference extends group.aelysium.rustyconnector.toolkit.velocity.util.Reference<IPlayer, String> {
        public UsernameReference(String username) {
            super(username);
        }

        public <TPlayer extends IPlayer> TPlayer get() {
            return (TPlayer) RustyConnector.Toolkit.proxy().orElseThrow().services().player().fetch(this.referencer).orElseThrow();
        }
    }

    /**
     * Used to fetch the player's rank from the storage system.
     */
    class RankKey {
        private final UUID player;
        private final String game;

        private RankKey(UUID player, String game) {
            this.player = player;
            this.game = game;
        }

        public String gameId() {
            return this.game;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RankKey rankKey = (RankKey) o;
            return Objects.equals(player, rankKey.player) && Objects.equals(game, rankKey.game);
        }

        @Override
        public int hashCode() {
            return Objects.hash(player, game);
        }

        public static RankKey from(UUID player, String gameId) {
            return new RankKey(player, gameId);
        }
    }
}