package group.aelysium.rustyconnector.toolkit.velocity.parties;

import group.aelysium.rustyconnector.toolkit.velocity.players.Player;

public interface IPartyInvite<TPlayer extends Player> {
    /**
     * Gets the {@link Player} that sent this invite.
     * @return {@link Player}
     */
    TPlayer sender();

    /**
     * Gets the {@link Player} that was targeted by this invite.
     * @return {@link Player}
     */
    TPlayer target();

    /**
     * Accepts this {@link IPartyInvite}.
     * Upon successful acceptance, this method will subsequently mark this {@link IPartyInvite} as acknowledged and {@link IPartyInvite#decompose()} it.
     * @throws IllegalStateException When something illegal happened while attempting to accept this party invite (Such as accepting an already acknowledged invite)
     */
    void accept() throws IllegalStateException;


    /**
     * Accepts this {@link IPartyInvite}.
     * Upon successful ignoring, this method will subsequently mark this {@link IPartyInvite} as acknowledged and {@link IPartyInvite#decompose()} it.
     * @throws IllegalStateException When something illegal happened while attempting to ignore this party invite (Such as ignoring an already acknowledged invite)
     */
    void ignore() throws IllegalStateException;


    /**
     * Decomposes the party invite, removing its data and making it unusable.
     */
    void decompose();
}
