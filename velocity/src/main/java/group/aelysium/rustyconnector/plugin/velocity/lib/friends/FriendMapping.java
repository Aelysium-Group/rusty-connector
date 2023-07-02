package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import com.velocitypowered.api.proxy.Player;

public record FriendMapping(Player player1, Player player2) {
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
}
