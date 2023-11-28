package group.aelysium.rustyconnector.toolkit.velocity.players;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.UUID;

public interface IPlayer {
    UUID uuid();
    String username();

    /**
     * Resolves the rusty player into a currently active online player.
     * If the player isn't online, the resolution will be empty.
     * @return {@link Optional<Player>}
     */
    Optional<Player> resolve();

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
}