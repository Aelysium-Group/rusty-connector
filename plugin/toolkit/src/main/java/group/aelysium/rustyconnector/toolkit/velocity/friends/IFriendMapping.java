package group.aelysium.rustyconnector.toolkit.velocity.friends;

import group.aelysium.rustyconnector.toolkit.velocity.players.Player;

/**
 * {@link IFriendMapping} operates unorderly. It doesn't matter what order you pass players to the constructor;
 * {@link IFriendMapping} will always return them in the same order when you call {@link IFriendMapping#player1()} or {@link IFriendMapping#player2()}.
 */
public interface IFriendMapping<P extends Player> {
    /**
     * Fetches player1 from this mapping.
     * @return {@link Player}
     */
    P player1();

    /**
     * Fetches player2 from this mapping.
     * @return {@link Player}
     */
    P player2();

    /**
     * Checks if the {@link Player} exists in this mapping.
     * @param player The {@link Player} to check for.
     * @return {@link Boolean}
     */
    boolean contains(P player);

    /**
     * If {@link Player `player`} exists in this mapping, fetch either {@link IFriendMapping#player1()} or {@link IFriendMapping#player2()}, whichever one does NOT return {@link Player `player`}.
     * @param player The {@link Player} to NOT get.
     * @return {@link Player}
     */
    P fetchOther(P player);
}