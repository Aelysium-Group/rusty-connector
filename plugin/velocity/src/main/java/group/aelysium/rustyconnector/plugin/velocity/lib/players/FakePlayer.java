package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record FakePlayer(UUID uuid, String username) {
    public static FakePlayer from(UUID uuid, String username) {
        return new FakePlayer(uuid, username);
    }
    public static FakePlayer from(Player player) {
        return new FakePlayer(player.getUniqueId(), player.getUsername());
    }

    public Optional<Player> resolve() {
        return VelocityAPI.get().velocityServer().getPlayer(this.uuid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FakePlayer that = (FakePlayer) o;
        return Objects.equals(uuid, that.uuid) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, username);
    }
}
