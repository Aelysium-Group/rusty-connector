package group.aelysium.rustyconnector.toolkit.velocity.friends;

import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;

import java.util.UUID;

public interface IFriendRequest {
    /**
     * Gets this {@link IFriendRequest friend request's} UUID.
     * @return {@link UUID}
     */
    UUID uuid();

    /**
     * Gets the sender of this {@link IFriendRequest}.
     * @return {@link IFriendRequest}
     */
    IPlayer sender();

    /**
     * Gets the target of this {@link IFriendRequest}.
     * Target is in the form of a username.
     * @return {@link IFriendRequest}
     */
    String target();

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