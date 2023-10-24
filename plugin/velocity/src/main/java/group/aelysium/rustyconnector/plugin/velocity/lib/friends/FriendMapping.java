package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import group.aelysium.rustyconnector.api.velocity.friends.IFriendMapping;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.ResolvablePlayer;

import java.util.Objects;

public class FriendMapping implements IFriendMapping<ResolvablePlayer> {
    private final ResolvablePlayer player1;
    private final ResolvablePlayer player2;

    protected FriendMapping(ResolvablePlayer player1, ResolvablePlayer player2) {
        // Ensure that players are always in order of the lowest uuid to the highest uuid.
        if(player1.uuid().compareTo(player2.uuid()) > 0) {
            this.player1 = player2;
            this.player2 = player1;

            return;
        }

        this.player1 = player1;
        this.player2 = player2;
    }

    public ResolvablePlayer player1() {
        return player1;
    }

    public ResolvablePlayer player2() {
        return player2;
    }

    public boolean contains(ResolvablePlayer player) {
        return this.player1.equals(player) || this.player2.equals(player);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendMapping that = (FriendMapping) o;
        return Objects.equals(player1, that.player1) && Objects.equals(player2, that.player2);
    }

    public static FriendMapping from(ResolvablePlayer player1, ResolvablePlayer player2) {
        return new FriendMapping(player1, player2);
    }

    public ResolvablePlayer fetchOther(ResolvablePlayer player) {
        if(this.player1.equals(player)) return this.player2;
        if(this.player2.equals(player)) return this.player1;

        return null;
    }
}