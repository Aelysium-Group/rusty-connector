package group.aelysium.rustyconnector.toolkit.velocity.connection;

import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;

public interface PlayerConnectable {
    /**
     * Connects the player to the specified resource.
     * This method will never return anything to the player.
     * It is the caller's job to handle outputs.
     * This method should never throw any exceptions.
     * @param player The player to connect.
     * @return A {@link Request} for the player's attempt.
     */
    Request connect(IPlayer player);

    /**
     * Handles logic when a player leaves this connectable.
     * @param player The player that left.
     */
    void leave(IPlayer player);

    record Request(@NotNull IPlayer player, Future<ConnectionResult> result) {
    }
}
