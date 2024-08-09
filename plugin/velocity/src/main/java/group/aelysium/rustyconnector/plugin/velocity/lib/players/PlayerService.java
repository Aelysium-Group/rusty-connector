package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageService;

import java.util.*;

public class PlayerService implements IPlayerService {
    private final Map<UUID, IPlayer> recentUUIDPlayers;
    private final Map<String, IPlayer> recentUsernamePlayers;
    private final StorageService storage;

    public PlayerService(StorageService storage) {
        this.storage = storage;
        this.recentUUIDPlayers = new LinkedHashMap<>(500){
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return this.size() > 100;
            }
        };
        this.recentUsernamePlayers = new LinkedHashMap<>(500){
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return this.size() > 100;
            }
        };
    }

    public void store(IPlayer player) {
        if(this.recentUUIDPlayers.containsKey(player.uuid())) return;
        this.storage.database().players().set(player);
        this.recentUUIDPlayers.put(player.uuid(), player);
        this.recentUsernamePlayers.put(player.username(), player);
    }

    public Optional<IPlayer> fetch(UUID uuid) {
        {
            IPlayer recent = this.recentUUIDPlayers.get(uuid);
            if (recent != null) return Optional.of(recent);
        }
        {
            com.velocitypowered.api.proxy.Player velocityPlayer = Tinder.get().velocityServer().getPlayer(uuid).orElse(null);
            if(velocityPlayer != null) return Optional.of(new Player(velocityPlayer));
        }

        Optional<IPlayer> player = this.storage.database().players().get(uuid);
        player.ifPresent(p -> this.recentUUIDPlayers.put(p.uuid(), p));
        player.ifPresent(p -> this.recentUsernamePlayers.put(p.username(), p));
        return player;
    }

    public Optional<IPlayer> fetch(String username) {
        {
            IPlayer recent = this.recentUsernamePlayers.get(username);
            if (recent != null) return Optional.of(recent);
        }
        {
            com.velocitypowered.api.proxy.Player velocityPlayer = Tinder.get().velocityServer().getPlayer(username).orElse(null);
            if(velocityPlayer != null) return Optional.of(new Player(velocityPlayer));
        }

        Optional<IPlayer> player = this.storage.database().players().get(username);
        player.ifPresent(p -> this.recentUUIDPlayers.put(p.uuid(), p));
        player.ifPresent(p -> this.recentUsernamePlayers.put(p.username(), p));
        return player;
    }

    @Override
    public void kill() {
        // this.storage.kill();  -  Storage is cleaned up in a different process.
        this.recentUUIDPlayers.clear();
        this.recentUsernamePlayers.clear();
    }
}
