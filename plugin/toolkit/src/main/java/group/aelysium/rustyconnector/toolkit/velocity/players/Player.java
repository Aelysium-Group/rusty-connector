package group.aelysium.rustyconnector.toolkit.velocity.players;

import group.aelysium.rustyconnector.toolkit.RustyConnector;
import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.UUID;

public interface Player {
    UUID uuid();
    String username();

    /**
     * Resolves the rusty player into a currently active online player.
     * If the player isn't online, the resolution will be empty.
     * @return {@link Optional< com.velocitypowered.api.proxy.Player >}
     */
    Optional<com.velocitypowered.api.proxy.Player> resolve();

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

    class Reference extends group.aelysium.rustyconnector.toolkit.velocity.util.Reference<Player, UUID> {
        public Reference(UUID uuid) {
            super(uuid);
        }

        public Player get() {
            return RustyConnector.Toolkit.proxy().orElseThrow().services().player().fetch(this.referencer).orElseThrow();
        }
    }

    class UsernameReference extends group.aelysium.rustyconnector.toolkit.velocity.util.Reference<Player, String> {
        public UsernameReference(String username) {
            super(username);
        }

        public Player get() {
            return RustyConnector.Toolkit.proxy().orElseThrow().services().player().fetch(this.referencer).orElseThrow();
        }
    }
}