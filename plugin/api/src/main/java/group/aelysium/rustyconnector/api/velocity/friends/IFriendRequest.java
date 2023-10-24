package group.aelysium.rustyconnector.api.velocity.friends;

import group.aelysium.rustyconnector.api.velocity.players.IResolvablePlayer;

public interface IFriendRequest {
    /**
     * Gets this {@link IFriendRequest friend request's} snowflake based ID.
     * @return {@link Long}
     */
    long id();

    /**
     * Gets the sender of this {@link IFriendRequest}.
     * @return {@link IFriendRequest}
     */
    IResolvablePlayer sender();

    /**
     * Gets the target of this {@link IFriendRequest}.
     * @return {@link IFriendRequest}
     */
    IResolvablePlayer target();

    /**
     * Accepts this {@link IFriendRequest}.
     * Upon successful acceptance, this method will subsequently mark this {@link IFriendRequest} as acknowledged and {@link IFriendRequest#decompose()} it.
     * @throws IllegalStateException When something illegal happened while attempting to accept this friend request (Such as accepting an already acknowledged request)
     */
    void accept() throws IllegalStateException;

    /**
     * Ignores this {@link IFriendRequest}.
     * Upon successful acceptance, this method will subsequently mark this {@link IFriendRequest} as acknowledged and {@link IFriendRequest#decompose()} it.
     * @throws IllegalStateException When something illegal happened while attempting to deny this friend request (Such as ignoring an already acknowledged request)
     */
    void ignore();

    /**
     * Decomposes the friend request, removing its data and making it unusable.
     */
    void decompose();
}