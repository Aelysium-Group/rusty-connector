package group.aelysium.rustyconnector.api.velocity.storage;

import group.aelysium.rustyconnector.api.velocity.family.static_family.IServerResidence;
import group.aelysium.rustyconnector.api.velocity.friends.IFriendMapping;
import group.aelysium.rustyconnector.api.velocity.players.IResolvablePlayer;

import java.util.List;

public interface IStorageRoot<players extends IResolvablePlayer, friends extends IFriendMapping<players>, residence extends IServerResidence> {
    /**
     * Gets the player mappings that have been stored by RustyConnector's remote storage connector.
     * As you make requests to this method, RustyConnector will dynamically query the database and fetch the data you need.
     * Interact with this method just like any other Java method!
     * @return {@link List<IResolvablePlayer>}
     */
    List<players> players();

    /**
     * Gets the friend mappings that have been stored by RustyConnector's remote storage connector.
     * As you make requests to this method, RustyConnector will dynamically query the database and fetch the data you need.
     * Interact with this method just like any other Java method!
     * @return {@link List<IResolvablePlayer>}
     */
    List<friends> friends();

    /**
     * Gets the server residences that have been stored by RustyConnector's remote storage connector.
     * As you make requests to this method, RustyConnector will dynamically query the database and fetch the data you need.
     * Interact with this method just like any other Java method!
     * @return {@link List<IServerResidence>}
     */
    List<residence> residence();


}
