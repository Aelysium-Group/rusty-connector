package group.aelysium.rustyconnector.toolkit.velocity.friends;

import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;

import java.util.Objects;

/**
 * {@link PlayerPair} operates unorderly. It doesn't matter what order you pass players to the constructor;
 * {@link PlayerPair} will always return them in the same order when you call {@link PlayerPair#player1()} or {@link PlayerPair#player2()}.
 */
public class PlayerPair {
    private final IPlayer player1;
    private final IPlayer player2;

    protected PlayerPair(IPlayer player1, IPlayer player2) {
        // Ensure that players are always in order of the lowest uuid to the highest uuid.
        if(player1.uuid().compareTo(player2.uuid()) > 0) {
            this.player1 = player2;
            this.player2 = player1;

            return;
        }

        this.player1 = player1;
        this.player2 = player2;
    }

    /**
     * Fetches player1 from this mapping.
     * @return {@link IPlayer}
     */
    public IPlayer player1() {
        return player1;
    }

    /**
     * Fetches player2 from this mapping.
     * @return {@link IPlayer}
     */
    public IPlayer player2() {
        return player2;
    }

    /**
     * Checks if the {@link IPlayer} exists in this mapping.
     * @param player The {@link IPlayer} to check for.
     * @return {@link Boolean}
     */
    public boolean contains(IPlayer player) {
        return this.player1.equals(player) || this.player2.equals(player);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerPair that = (PlayerPair) o;
        return Objects.equals(player1, that.player1) && Objects.equals(player2, that.player2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player1.uuid(), player2.uuid());
    }

    /**
     * If {@link IPlayer `player`} exists in this mapping, fetch either {@link PlayerPair#player1()} or {@link PlayerPair#player2()}, whichever one does NOT return {@link IPlayer `player`}.
     * @param player The {@link IPlayer} to NOT get.
     * @return {@link IPlayer}
     */
    public IPlayer fetchOther(IPlayer player) {
        if(this.player1.equals(player)) return this.player2;
        if(this.player2.equals(player)) return this.player1;

        return null;
    }

    public static PlayerPair from(IPlayer player1, IPlayer player2) {
        return new PlayerPair(player1, player2);
    }
}