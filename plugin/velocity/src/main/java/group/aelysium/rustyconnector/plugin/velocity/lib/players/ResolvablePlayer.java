package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ResolvablePlayer {
    protected UUID uuid;
    protected String username;

    protected ResolvablePlayer(UUID uuid, String username) {
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

        ResolvablePlayer that = (ResolvablePlayer) object;
        return Objects.equals(uuid, that.uuid) && Objects.equals(username, that.username);
    }

    @Override
    public String toString() {
        return "<Player uuid="+this.uuid.toString()+" username="+this.username+">";
    }

    public static ResolvablePlayer from(Player player) {
        return new ResolvablePlayer(player.getUniqueId(), player.getUsername());
    }
    public static ResolvablePlayer from(UUID uuid, String username) {
        return new ResolvablePlayer(uuid, username);
    }
}