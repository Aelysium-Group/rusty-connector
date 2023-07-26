package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import com.velocitypowered.api.proxy.Player;

import java.util.Objects;

public record FriendMapping(Player player1, Player player2) {
    public FriendMapping(Player player1, Player player2) {
        // Ensure that players are always in order of the lowest uuid to the highest uuid.
        if(player1.getUniqueId().compareTo(player2.getUniqueId()) > 0) {
            this.player1 = player2;
            this.player2 = player1;

            return;
        }

        this.player1 = player1;
        this.player2 = player2;
    }

    /**
     * Return the friend of one of the players in this mapping.
     * @param player The player to get the friend of.
     * @return The friend of `player`.
     */
    public Player getFriendOf(Player player) {
        if(this.player1 == player) return this.player2;
        if(this.player2 == player) return this.player1;

        throw new NullPointerException("This mapping doesn't apply to the provided player!");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendMapping that = (FriendMapping) o;
        return Objects.equals(player1, that.player1) && Objects.equals(player2, that.player2);
    }
}
