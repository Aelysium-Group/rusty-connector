package group.aelysium.rustyconnector.toolkit.velocity.friends;

import group.aelysium.rustyconnector.toolkit.velocity.players.Player;

import java.util.List;
import java.util.Optional;

public interface IFriendsDataEnclave<TPlayer extends Player, TFriendMapping extends IFriendMapping<TPlayer>> {
    /**
     * Find all friends of a player.
     * @param player The player to find friends of.
     * @return A list of friends.
     */
    Optional<List<TFriendMapping>> findFriends(TPlayer player);

    /**
     * Check if two players are friends.
     * @param player1 The first player.
     * @param player2 The second player.
     * @return `true` If the two players are friends.
     */
    boolean areFriends(TPlayer player1, TPlayer player2) throws RuntimeException;

    /**
     * Get number of friends of a player.
     * @param player The player to get the friend count of.
     * @return The number of friends a player has.
     */
    Optional<Long> getFriendCount(TPlayer player);

    Optional<TFriendMapping> addFriend(TPlayer player1, TPlayer player2);

    /**
     * Delete two players friend mapping.
     * @param player1 {@link Player}
     * @param player2 {@link Player}
     */
    void removeFriend(TPlayer player1, TPlayer player2);
}
