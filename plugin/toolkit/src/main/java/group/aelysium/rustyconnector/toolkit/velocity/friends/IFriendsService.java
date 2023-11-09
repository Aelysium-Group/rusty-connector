package group.aelysium.rustyconnector.toolkit.velocity.friends;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.toolkit.velocity.players.IResolvablePlayer;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

import java.util.*;

public interface IFriendsService<TResolvablePlayer extends IResolvablePlayer, TFriendRequest extends IFriendRequest> extends Service {
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
    List<TFriendRequest> findRequestsToTarget(TResolvablePlayer target);

    /**
     * Searches for a list of {@link IFriendRequest friend requests} that are addressed to a target.
     * @param target The target to search for.
     * @return {@link List<IFriendRequest>}
     */
    Optional<TFriendRequest> findRequest(TResolvablePlayer target, TResolvablePlayer sender);

    Optional<List<TResolvablePlayer>> findFriends(Player player);

    boolean areFriends(TResolvablePlayer player1, TResolvablePlayer player2);
    void addFriends(TResolvablePlayer player1, TResolvablePlayer player2);
    void removeFriends(TResolvablePlayer player1, TResolvablePlayer player2);

    IFriendMapping<TResolvablePlayer> sendRequest(Player sender, TResolvablePlayer target);

    void closeInvite(TFriendRequest request);

    Optional<Long> friendCount(TResolvablePlayer player);
}
