package group.aelysium.rustyconnector.toolkit.velocity.players;

import group.aelysium.rustyconnector.toolkit.RustyConnector;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IRankedPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import net.kyori.adventure.text.Component;

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
     * Gets the ranked verison of this player.
     * @param game The game to fetch the rank from.
     */
    Optional<IRankedPlayer> rank(String game);

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
     * A collection of a player's UUID and Username, which can be resolved into a {@link IPlayer}.
     */
    interface IShard {
        /**
         * The UUID of the player shard.
         */
        UUID uuid();

        /**
         * The Username of the player shard.
         */
        String username();

        /**
         * Store the player shard and get the associated {@link IPlayer} that is created.
         * If a {@link IPlayer} already exists for this shard, just get the player.
         * @return {@link IPlayer}
         */
        IPlayer storeAndGet();
    }
}