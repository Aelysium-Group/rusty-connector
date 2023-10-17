package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.FakePlayer;

import java.util.NoSuchElementException;

public class FriendRequest {
    private final FriendsService friendsService;
    private long id;
    private FakePlayer sender;
    private FakePlayer target;
    private Boolean isAcknowledged = null;

    public FriendRequest(FriendsService friendsService, long id, FakePlayer sender, FakePlayer target) {
        this.friendsService = friendsService;
        this.id = id;
        this.sender = sender;
        this.target = target;
    }

    public long id() {
        return this.id;
    }
    public FakePlayer sender() {
        return this.sender;
    }
    public FakePlayer target() {
        return this.target;
    }


    /**
     * Accept the party invite.
     * This will subsequently connect the player to the party's server and then decompose the invite and remove it from the PartyService that it belongs to.
     */
    public synchronized void accept() {
        try {
            if (friendsService.friendCount(this.target).orElseThrow() > friendsService.settings().maxFriends())
                throw new IllegalStateException(VelocityLang.FRIEND_INJECTED_MAXED);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(VelocityLang.FRIEND_INJECTED_INTERNAL_ERROR);
        }

        if(this.isAcknowledged != null)
            throw new IllegalStateException(VelocityLang.FRIEND_INJECTED_ACKNOWLEDGED);

        try {
            friendsService.addFriends(this.sender, this.target);

            try {
                Player resolved = this.target.resolve().orElseThrow();
                resolved.sendMessage(VelocityLang.BECOME_FRIENDS.build(sender.username()));
            } catch (NoSuchElementException ignore) {}
            try {
                Player resolved = this.sender.resolve().orElseThrow();
                resolved.sendMessage(VelocityLang.BECOME_FRIENDS.build(target.username()));
            } catch (NoSuchElementException ignore) {}

            friendsService.closeInvite(this);
            this.isAcknowledged = true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(VelocityLang.FRIEND_INJECTED_INTERNAL_ERROR);
        }
    }

    /**
     * Deny the party invite.
     * This will subsequently decompose the invite and remove it from the PartyService that it belongs to.
     */
    public synchronized void ignore() {
        try {
            friendsService.closeInvite(this);
            this.isAcknowledged = true;
        } catch (Exception ignore) {
            throw new IllegalStateException(VelocityLang.FRIEND_INJECTED_INTERNAL_ERROR);
        }
    }

    public synchronized void decompose() {
        this.id = 0;
        this.sender = null;
        this.target = null;
    }
}