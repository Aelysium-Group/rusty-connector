package group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa;

import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;

public interface ITPARequest {
    /**
     * Gets the plauer which sent this tpa request.
     * @return {@link IPlayer}
     */
    IPlayer sender();

    /**
     * Gets the player which received this tpa request.
     * @return {@link IPlayer}
     */
    IPlayer target();

    /**
     * Checks whether or not this tpa request has expired.
     * Expired requests should no longer be used as they are queued for deletion.
     * @return {@link Boolean}
     */
    boolean expired();

    /**
     * Sends the tpa request to the target player.
     */
    void submit();

    /**
     * Denies the tpa request.
     */
    void deny();

    /**
     * Accepts the tpa request.
     * This method doesn't return any data, instead players may receive error messages in-chat.
     */
    void accept();
}
