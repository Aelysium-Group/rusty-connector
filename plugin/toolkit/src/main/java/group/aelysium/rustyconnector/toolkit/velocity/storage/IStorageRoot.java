package group.aelysium.rustyconnector.toolkit.velocity.storage;

import group.aelysium.rustyconnector.toolkit.velocity.family.static_family.IServerResidence;
import group.aelysium.rustyconnector.toolkit.velocity.friends.IFriendMapping;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;

import java.util.Set;

public interface IStorageRoot<players extends IPlayer, friends extends IFriendMapping<players>, residence extends IServerResidence> {
    /**
     * Gets the player mappings that have been stored by RustyConnector's remote storage connector.
     * As you make requests to this method, RustyConnector will dynamically query the database and fetch the data you need.
     * Interact with this method just like any other Java method!
     * @return {@link Set<  IPlayer  >}
     */
    Set<players> players();

    /**
     * Gets the friend mappings that have been stored by RustyConnector's remote storage connector.
     * As you make requests to this method, RustyConnector will dynamically query the database and fetch the data you need.
     * Interact with this method just like any other Java method!
     * @return {@link Set<  IPlayer  >}
     */
    Set<friends> friends();

    /**
     * Gets the server residences that have been stored by RustyConnector's remote storage connector.
     * As you make requests to this method, RustyConnector will dynamically query the database and fetch the data you need.
     * Interact with this method just like any other Java method!
     * @return {@link Set<IServerResidence>}
     */
    Set<residence> residence();
}
