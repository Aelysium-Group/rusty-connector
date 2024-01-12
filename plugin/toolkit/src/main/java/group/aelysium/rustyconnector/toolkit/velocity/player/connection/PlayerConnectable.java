package group.aelysium.rustyconnector.toolkit.velocity.player.connection;

import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;

public interface PlayerConnectable {
    /**
     * Connects the player to the specified resource.
     * This method will never return anything to the player.
     * It is the caller's job to handle outputs.
     * This method should never throw any exceptions.
     * @param player The player to connect.
     * @return A {@link ConnectionRequest} for the player's attempt.
     */
    ConnectionRequest connect(IPlayer player);

    /**
     * Handles logic when a player leaves this connectable.
     * @param player The player that left.
     */
    void leave(IPlayer player);
}
