package group.aelysium.rustyconnector.toolkit.velocity.player;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * The player service provides player based services to RustyConnector.
 * To fetch players, you can use a `Player.Reference`
 */
public interface IPlayerService extends Service {
    /**
     * Finds a player based on a uuid.
     * An alternate route of getting a player, other than "tinder.services().player().fetch()", can be to use {@link IPlayer.Reference new Family.Reference(uuid)}{@link IPlayer.Reference#get() .get()}.
     * @param uuid The uuid to search for.
     * @return {@link Optional<IPlayer>}
     */
    Optional<IPlayer> fetch(UUID uuid);

    /**
     * Finds a player based on a username.
     * An alternate route of getting a player, other than "tinder.services().player().fetch()", can be to use {@link IPlayer.UsernameReference new Family.UsernameReference(username)}{@link IPlayer.Reference#get() .get()}.
     * @param username The username to search for.
     * @return {@link Optional<IPlayer>}
     */
    Optional<IPlayer> fetch(String username);
}
