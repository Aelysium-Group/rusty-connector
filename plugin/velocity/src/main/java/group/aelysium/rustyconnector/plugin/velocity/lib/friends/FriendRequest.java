package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import group.aelysium.rustyconnector.toolkit.velocity.friends.IFriendRequest;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;

import java.util.NoSuchElementException;

public class FriendRequest implements IFriendRequest {
    private final FriendsService friendsService;
    private long id;
    private Player sender;
    private Player target;
    private Boolean isAcknowledged = null;

    public FriendRequest(FriendsService friendsService, long id, Player sender, Player target) {
        this.friendsService = friendsService;
        this.id = id;
        this.sender = sender;
        this.target = target;
    }

    public long id() {
        return this.id;
    }
    public Player sender() {
        return this.sender;
    }
    public Player target() {
        return this.target;
    }

    public synchronized void accept() {
        try {
            if (friendsService.friendCount(this.target).orElseThrow() > friendsService.settings().maxFriends())
                throw new IllegalStateException(ProxyLang.FRIEND_INJECTED_MAXED);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(ProxyLang.FRIEND_INJECTED_INTERNAL_ERROR);
        }

        if(this.isAcknowledged != null)
            throw new IllegalStateException(ProxyLang.FRIEND_INJECTED_ACKNOWLEDGED);

        try {
            friendsService.addFriends(this.sender, this.target);

            try {
                com.velocitypowered.api.proxy.Player resolved = this.target.resolve().orElseThrow();
                resolved.sendMessage(ProxyLang.BECOME_FRIENDS.build(sender.username()));
            } catch (NoSuchElementException ignore) {}
            try {
                com.velocitypowered.api.proxy.Player resolved = this.sender.resolve().orElseThrow();
                resolved.sendMessage(ProxyLang.BECOME_FRIENDS.build(target.username()));
            } catch (NoSuchElementException ignore) {}

            friendsService.closeInvite(this);
            this.isAcknowledged = true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(ProxyLang.FRIEND_INJECTED_INTERNAL_ERROR);
        }
    }

    public synchronized void ignore() {
        try {
            friendsService.closeInvite(this);
            this.isAcknowledged = true;
        } catch (Exception ignore) {
            throw new IllegalStateException(ProxyLang.FRIEND_INJECTED_INTERNAL_ERROR);
        }
    }

    public synchronized void decompose() {
        this.id = 0;
        this.sender = null;
        this.target = null;
    }
}