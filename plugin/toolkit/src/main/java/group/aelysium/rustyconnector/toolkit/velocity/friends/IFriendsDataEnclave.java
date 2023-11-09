package group.aelysium.rustyconnector.toolkit.velocity.friends;

import group.aelysium.rustyconnector.toolkit.velocity.players.IResolvablePlayer;

import java.util.List;
import java.util.Optional;

public interface IFriendsDataEnclave<TResolvablePlayer extends IResolvablePlayer, TFriendMapping extends IFriendMapping<TResolvablePlayer>> {
    /**
     * Find all friends of a player.
     * @param player The player to find friends of.
     * @return A list of friends.
     */
    Optional<List<TFriendMapping>> findFriends(TResolvablePlayer player);

    /**
     * Check if two players are friends.
     * @param player1 The first player.
     * @param player2 The second player.
     * @return `true` If the two players are friends.
     */
    boolean areFriends(TResolvablePlayer player1, TResolvablePlayer player2) throws RuntimeException;

    /**
     * Get number of friends of a player.
     * @param player The player to get the friend count of.
     * @return The number of friends a player has.
     */
    Optional<Long> getFriendCount(TResolvablePlayer player);

    Optional<TFriendMapping> addFriend(TResolvablePlayer player1, TResolvablePlayer player2);

    /**
     * Delete two players friend mapping.
     * @param player1 {@link IResolvablePlayer}
     * @param player2 {@link IResolvablePlayer}
     */
    void removeFriend(TResolvablePlayer player1, TResolvablePlayer player2);
}
