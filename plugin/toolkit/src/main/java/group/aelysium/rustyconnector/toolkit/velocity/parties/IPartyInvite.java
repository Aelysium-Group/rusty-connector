package group.aelysium.rustyconnector.toolkit.velocity.parties;

import group.aelysium.rustyconnector.toolkit.velocity.players.IRustyPlayer;

public interface IPartyInvite<TResolvablePlayer extends IRustyPlayer> {
    /**
     * Gets the {@link IRustyPlayer} that sent this invite.
     * @return {@link IRustyPlayer}
     */
    TResolvablePlayer sender();

    /**
     * Gets the {@link IRustyPlayer} that was targeted by this invite.
     * @return {@link IRustyPlayer}
     */
    TResolvablePlayer target();

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
