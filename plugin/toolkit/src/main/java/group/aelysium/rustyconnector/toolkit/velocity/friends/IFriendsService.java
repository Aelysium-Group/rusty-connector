package group.aelysium.rustyconnector.toolkit.velocity.friends;

import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

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

    Optional<List<IPlayer>> findFriends(IPlayer player);

    boolean areFriends(IPlayer player1, IPlayer player2);
    void addFriends(IPlayer player1, IPlayer player2);
    void removeFriends(IPlayer player1, IPlayer player2);

    IFriendMapping sendRequest(IPlayer sender, IPlayer target);

    void closeInvite(IFriendRequest request);

    Optional<Long> friendCount(IPlayer player);
}
