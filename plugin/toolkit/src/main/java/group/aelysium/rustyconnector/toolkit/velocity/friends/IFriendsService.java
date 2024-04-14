package group.aelysium.rustyconnector.toolkit.velocity.friends;

import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IDatabase;

import java.util.*;

public interface IFriendsService extends Service {
    /**
     * Gets the settings that this {@link IFriendsService} abides by.
     * @return {@link IFriendsService}
     */
    FriendsServiceSettings settings();

    /**
     * Searches for a list of {@link IFriendRequest friend requests} that are addressed to a target.
     * @param target The target to search for.
     * @return {@link List<IFriendRequest>}
     */
    List<IFriendRequest> findRequestsToTarget(IPlayer target);

    /**
     * Searches for a list of {@link IFriendRequest friend requests} that are addressed to a target.
     * @param target The target to search for.
     * @return {@link List<IFriendRequest>}
     */
    Optional<IFriendRequest> findRequest(IPlayer target, IPlayer sender);

    /**
     * Gets the storage system for the Friends Service.
     */
    IDatabase.FriendLinks friendStorage();

    /**
     * Sends a friend request to the target username.
     * If the user is online they will be notified immediately.
     * If the user is offline, they will be notified once they login.
     * Friend requests expire after 10 minutes.
     * @param sender The user sending the friend request.
     * @param targetUsername The target of the friend request.
     */
    void sendRequest(IPlayer sender, String targetUsername);

    void closeInvite(IFriendRequest request);

    long friendCount(IPlayer player);
}
