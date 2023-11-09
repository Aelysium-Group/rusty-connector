package group.aelysium.rustyconnector.toolkit.velocity.friends;

import group.aelysium.rustyconnector.toolkit.velocity.players.IResolvablePlayer;

/**
 * {@link IFriendMapping} operates unorderly. It doesn't matter what order you pass players to the constructor;
 * {@link IFriendMapping} will always return them in the same order when you call {@link IFriendMapping#player1()} or {@link IFriendMapping#player2()}.
 */
public interface IFriendMapping<P extends IResolvablePlayer> {
    /**
     * Fetches player1 from this mapping.
     * @return {@link IResolvablePlayer}
     */
    P player1();

    /**
     * Fetches player2 from this mapping.
     * @return {@link IResolvablePlayer}
     */
    P player2();

    /**
     * Checks if the {@link IResolvablePlayer} exists in this mapping.
     * @param player The {@link IResolvablePlayer} to check for.
     * @return {@link Boolean}
     */
    boolean contains(P player);

    /**
     * If {@link IResolvablePlayer `player`} exists in this mapping, fetch either {@link IFriendMapping#player1()} or {@link IFriendMapping#player2()}, whichever one does NOT return {@link IResolvablePlayer `player`}.
     * @param player The {@link IResolvablePlayer} to NOT get.
     * @return {@link IResolvablePlayer}
     */
    P fetchOther(P player);
}