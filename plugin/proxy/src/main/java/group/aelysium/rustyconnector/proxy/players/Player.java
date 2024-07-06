package group.aelysium.rustyconnector.proxy.players;

import group.aelysium.rustyconnector.toolkit.RC;
import group.aelysium.rustyconnector.toolkit.proxy.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.proxy.family.mcloader.IMCLoader;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Player implements IPlayer {
    protected UUID uuid;
    protected String username;

    public Player(@NotNull UUID uuid, @NotNull String username) {
        this.uuid = uuid;
        this.username = username;
    }
    public Player(@NotNull com.velocitypowered.api.proxy.Player velocityPlayer) {
        this.uuid = velocityPlayer.getUniqueId();
        this.username = velocityPlayer.getUsername();
    }

    public UUID uuid() { return this.uuid; }
    public String username() { return this.username; }

    public void sendMessage(Component message) {
        try {
            RC.P.Adapter().messagePlayer(this, message);
        } catch (Exception ignore) {}
    }

    public void disconnect(Component reason) {
        try {
            RC.P.Adapter().disconnect(this, reason);
        } catch (Exception ignore) {}
    }

    @Override
    public boolean online() {
        return RC.P.Adapter().fetchMCLoader(this).isPresent();
    }

    public Optional<IMCLoader> server() {
        return RC.P.Adapter().fetchMCLoader(this);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Player that = (Player) object;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public String toString() {
        return "<Player uuid="+this.uuid.toString()+" username="+this.username+">";
    }
}