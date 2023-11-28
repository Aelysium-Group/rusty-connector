package group.aelysium.rustyconnector.toolkit.velocity.friends;

import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;

/**
 * {@link IFriendMapping} operates unorderly. It doesn't matter what order you pass players to the constructor;
 * {@link IFriendMapping} will always return them in the same order when you call {@link IFriendMapping#player1()} or {@link IFriendMapping#player2()}.
 */
public interface IFriendMapping<P extends IPlayer> {
    /**
     * Fetches player1 from this mapping.
     * @return {@link IPlayer}
     */
    P player1();

    /**
     * Fetches player2 from this mapping.
     * @return {@link IPlayer}
     */
    P player2();

    /**
     * Checks if the {@link IPlayer} exists in this mapping.
     * @param player The {@link IPlayer} to check for.
     * @return {@link Boolean}
     */
    boolean contains(P player);

    /**
     * If {@link IPlayer `player`} exists in this mapping, fetch either {@link IFriendMapping#player1()} or {@link IFriendMapping#player2()}, whichever one does NOT return {@link IPlayer `player`}.
     * @param player The {@link IPlayer} to NOT get.
     * @return {@link IPlayer}
     */
    P fetchOther(P player);
}