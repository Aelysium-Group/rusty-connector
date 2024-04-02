package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageService;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import net.kyori.adventure.text.Component;
import org.eclipse.serializer.persistence.types.Persister;

import java.util.*;

public class Player implements IPlayer {
    protected transient Persister storage;
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
    public boolean online() {
        return resolve().isPresent();
    }

    public Optional<IPlayerRank> rank(String gameId) {
        return Tinder.get().services().storage().database().fetchRank(RankKey.from(this.uuid, gameId));
    }

    public Optional<MCLoader> server() {
        try {
            com.velocitypowered.api.proxy.Player resolvedPlayer = this.resolve().orElseThrow();
            UUID mcLoaderUUID = UUID.fromString(resolvedPlayer.getCurrentServer().orElseThrow().getServerInfo().getName());

            MCLoader mcLoader = new MCLoader.Reference(mcLoaderUUID).get();

            return Optional.of(mcLoader);
        } catch (Exception ignore) {}
        return Optional.empty();
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

    /**
     * Fetches a RustyConnector player from the provided Velocity player.
     * If no player is stored in storage, the player will be stored.
     * If a player was already stored in the storage, that player will be returned.
     *
     * This method will also update the player's username if it has been changed.
     * @param velocityPlayer The player to fetch.
     * @return {@link Player}
     */
    public static Player from(com.velocitypowered.api.proxy.Player velocityPlayer) {
        // If player doesn't exist, we need to make one and store it.
        StorageService storage = Tinder.get().services().storage();

        try {
            Player player = new Reference(velocityPlayer.getUniqueId()).get();
            if(!player.username().equals(velocityPlayer.getUsername())) {
                player.username = velocityPlayer.getUsername();
                storage.database().savePlayer(player);
                return player;
            }
        } catch (Exception ignore) {}

        Player player = new Player(velocityPlayer.getUniqueId(), velocityPlayer.getUsername());

        storage.database().savePlayer(player);

        return player;
    }
}