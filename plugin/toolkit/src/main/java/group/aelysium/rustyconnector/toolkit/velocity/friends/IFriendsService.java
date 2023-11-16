package group.aelysium.rustyconnector.toolkit.velocity.friends;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.toolkit.velocity.players.IRustyPlayer;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

import java.util.*;

public interface IFriendsService<TRustyPlayer extends IRustyPlayer, TFriendRequest extends IFriendRequest> extends Service {
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
    List<TFriendRequest> findRequestsToTarget(TRustyPlayer target);

    /**
     * Searches for a list of {@link IFriendRequest friend requests} that are addressed to a target.
     * @param target The target to search for.
     * @return {@link List<IFriendRequest>}
     */
    Optional<TFriendRequest> findRequest(TRustyPlayer target, TRustyPlayer sender);

    Optional<List<TRustyPlayer>> findFriends(TRustyPlayer player);

    boolean areFriends(TRustyPlayer player1, TRustyPlayer player2);
    void addFriends(TRustyPlayer player1, TRustyPlayer player2);
    void removeFriends(TRustyPlayer player1, TRustyPlayer player2);

    IFriendMapping<TRustyPlayer> sendRequest(Player sender, TRustyPlayer target);

    void closeInvite(TFriendRequest request);

    Optional<Long> friendCount(TRustyPlayer player);
}
