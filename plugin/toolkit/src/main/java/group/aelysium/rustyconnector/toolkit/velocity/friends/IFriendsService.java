package group.aelysium.rustyconnector.toolkit.velocity.friends;

import group.aelysium.rustyconnector.toolkit.velocity.players.Player;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

import java.util.*;

public interface IFriendsService<TPlayer extends Player, TFriendRequest extends IFriendRequest> extends Service {
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
    List<TFriendRequest> findRequestsToTarget(TPlayer target);

    /**
     * Searches for a list of {@link IFriendRequest friend requests} that are addressed to a target.
     * @param target The target to search for.
     * @return {@link List<IFriendRequest>}
     */
    Optional<TFriendRequest> findRequest(TPlayer target, TPlayer sender);

    Optional<List<TPlayer>> findFriends(TPlayer player);

    boolean areFriends(TPlayer player1, TPlayer player2);
    void addFriends(TPlayer player1, TPlayer player2);
    void removeFriends(TPlayer player1, TPlayer player2);

    IFriendMapping<TPlayer> sendRequest(com.velocitypowered.api.proxy.Player sender, TPlayer target);

    void closeInvite(TFriendRequest request);

    Optional<Long> friendCount(TPlayer player);
}
