package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.friends.IFriendRequest;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

public class FriendRequest implements IFriendRequest {
    private final FriendsService friendsService;
    private UUID uuid = UUID.randomUUID();
    private IPlayer sender;
    private String target;
    private Boolean isAcknowledged = null;

    public FriendRequest(FriendsService friendsService, IPlayer sender, String target) {
        this.friendsService = friendsService;
        this.sender = sender;
        this.target = target;
    }

    public UUID uuid() {
        return this.uuid;
    }
    public IPlayer sender() {
        return this.sender;
    }
    public String target() {
        return this.target;
    }

    public synchronized void accept() {
        try {
            Player player = new IPlayer.UsernameReference(this.target).get();

            try {
                if (friendsService.friendCount(player) > friendsService.settings().maxFriends())
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
                friendsService.friendStorage().set(this.sender, player);

                try {
                    player.sendMessage(ProxyLang.BECOME_FRIENDS.build(sender.username()));
                } catch (NoSuchElementException ignore) {}
                try {
                    com.velocitypowered.api.proxy.Player resolved = this.sender.resolve().orElseThrow();
                    resolved.sendMessage(ProxyLang.BECOME_FRIENDS.build(player.username()));
                } catch (NoSuchElementException ignore) {}

                friendsService.closeInvite(this);
                this.isAcknowledged = true;
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException(ProxyLang.FRIEND_INJECTED_INTERNAL_ERROR);
            }
        } catch (Exception e) {
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
        this.sender = null;
        this.target = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendRequest that = (FriendRequest) o;
        return
                Objects.equals(sender, that.sender) && Objects.equals(target, that.target) ||
                Objects.equals(sender.username(), that.target) && Objects.equals(target, that.sender.username());
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, target);
    }
}