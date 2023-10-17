package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class FakePlayer {
    protected UUID uuid;
    protected String username;

    protected FakePlayer(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    public UUID uuid() { return this.uuid; }
    public String username() { return this.username; }

    public Optional<Player> resolve() {
        return Tinder.get().velocityServer().getPlayer(this.uuid);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        FakePlayer that = (FakePlayer) object;
        return Objects.equals(uuid, that.uuid) && Objects.equals(username, that.username);
    }

    public static FakePlayer from(Player player) {
        return new FakePlayer(player.getUniqueId(), player.getUsername());
    }
    public static FakePlayer from(UUID uuid, String username) {
        return new FakePlayer(uuid, username);
    }
}