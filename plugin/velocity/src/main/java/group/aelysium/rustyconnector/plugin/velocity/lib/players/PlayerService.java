package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageService;

import java.util.*;

public class PlayerService implements IPlayerService {
    private final Map<UUID, IPlayer> recentPlayers;
    private final StorageService storage;

    public PlayerService(StorageService storage) {
        this.storage = storage;
        this.recentPlayers = new LinkedHashMap<>(500){
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return this.size() > 100;
            }
        };
    }

    public void store(IPlayer player) {
        if(this.recentPlayers.containsKey(player.uuid())) return;
        this.storage.database().players().set(player);
        this.recentPlayers.put(player.uuid(), player);
    }

    public Optional<IPlayer> fetch(UUID uuid) {
        {
            IPlayer recent = this.recentPlayers.get(uuid);
            if (recent != null) return Optional.of(recent);
        }
        {
            com.velocitypowered.api.proxy.Player velocityPlayer = Tinder.get().velocityServer().getPlayer(uuid).orElse(null);
            if(velocityPlayer != null) return Optional.of(new Player(velocityPlayer));
        }

        Optional<IPlayer> player = this.storage.database().players().get(uuid);
        player.ifPresent(p -> this.recentPlayers.put(p.uuid(), p));
        return player;
    }

    public Optional<IPlayer> fetch(String username) {
        {
            com.velocitypowered.api.proxy.Player velocityPlayer = Tinder.get().velocityServer().getPlayer(username).orElse(null);
            if(velocityPlayer != null) return Optional.of(new Player(velocityPlayer));
        }

        Optional<IPlayer> player = this.storage.database().players().get(username);
        player.ifPresent(p -> this.recentPlayers.put(p.uuid(), p));
        return player;
    }

    @Override
    public void kill() {
        // this.storage.kill();  -  Storage is cleaned up in a different process.
        this.recentPlayers.clear();
    }
}
