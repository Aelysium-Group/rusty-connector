package group.aelysium.rustyconnector.toolkit.velocity.friends;

import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;

import java.util.List;
import java.util.Optional;

public interface IFriendsDataEnclave {
    /**
     * Find all friends of a player.
     * @param player The player to find friends of.
     * @return A list of friends.
     */
    Optional<List<PlayerPair>> findFriends(IPlayer player);

    /**
     * Check if two players are friends.
     * @param player1 The first player.
     * @param player2 The second player.
     * @return `true` If the two players are friends.
     */
    boolean areFriends(IPlayer player1, IPlayer player2) throws RuntimeException;

    /**
     * Get number of friends of a player.
     * @param player The player to get the friend count of.
     * @return The number of friends a player has.
     */
    long getFriendCount(IPlayer player);

    Optional<PlayerPair> addFriend(IPlayer player1, IPlayer player2);

    /**
     * Delete two players friend mapping.
     * @param player1 {@link IPlayer}
     * @param player2 {@link IPlayer}
     */
    void removeFriend(IPlayer player1, IPlayer player2);
}
