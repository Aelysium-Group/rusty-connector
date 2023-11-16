package group.aelysium.rustyconnector.toolkit.velocity.friends;

import group.aelysium.rustyconnector.toolkit.velocity.players.IRustyPlayer;

/**
 * {@link IFriendMapping} operates unorderly. It doesn't matter what order you pass players to the constructor;
 * {@link IFriendMapping} will always return them in the same order when you call {@link IFriendMapping#player1()} or {@link IFriendMapping#player2()}.
 */
public interface IFriendMapping<P extends IRustyPlayer> {
    /**
     * Fetches player1 from this mapping.
     * @return {@link IRustyPlayer}
     */
    P player1();

    /**
     * Fetches player2 from this mapping.
     * @return {@link IRustyPlayer}
     */
    P player2();

    /**
     * Checks if the {@link IRustyPlayer} exists in this mapping.
     * @param player The {@link IRustyPlayer} to check for.
     * @return {@link Boolean}
     */
    boolean contains(P player);

    /**
     * If {@link IRustyPlayer `player`} exists in this mapping, fetch either {@link IFriendMapping#player1()} or {@link IFriendMapping#player2()}, whichever one does NOT return {@link IRustyPlayer `player`}.
     * @param player The {@link IRustyPlayer} to NOT get.
     * @return {@link IRustyPlayer}
     */
    P fetchOther(P player);
}