package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageService;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import net.kyori.adventure.text.Component;

import java.util.*;

public class Player implements group.aelysium.rustyconnector.toolkit.velocity.players.Player {
    protected UUID uuid;
    protected String username;
    protected long firstLogin;
    protected long lastLogin;

    protected Player(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    public UUID uuid() { return this.uuid; }
    public String username() { return this.username; }

    public void sendMessage(Component message) {
        try {
            this.resolve().orElseThrow().sendMessage(message);
        } catch (Exception ignore) {}
    }

    public void disconnect(Component reason) {
        try {
            this.resolve().orElseThrow().disconnect(reason);
        } catch (Exception ignore) {}
    }

    public Optional<com.velocitypowered.api.proxy.Player> resolve() {
        return Tinder.get().velocityServer().getPlayer(this.uuid);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Player that = (Player) object;
        return Objects.equals(uuid, that.uuid) && Objects.equals(username, that.username);
    }

    @Override
    public String toString() {
        return "<Player uuid="+this.uuid.toString()+" username="+this.username+">";
    }

    /**
     * Get a resolvable player from the provided player.
     * If no player is stored in storage, the player will be stored.
     * If a player was already stored in the storage, that player will be returned.
     *
     * This method really only every needs to be used the first time a player connects to the proxy.
     * @param player The player to convert.
     * @return {@link Player}
     */
    public static Player from(com.velocitypowered.api.proxy.Player player) {
        StorageService storageService = Tinder.get().services().storage();
        Player tempPlayer = new Player(player.getUniqueId(), player.getUsername());

        Set<Player> players = storageService.root().players();
        if(players.add(tempPlayer)) {
            storageService.store(players);
            return tempPlayer;
        }

        return players.stream().filter(player1 -> player1.equals(tempPlayer)).findAny().orElseThrow();
    }
    public static Player from(UUID uuid, String username) {
        return new Player(uuid, username);
    }
}