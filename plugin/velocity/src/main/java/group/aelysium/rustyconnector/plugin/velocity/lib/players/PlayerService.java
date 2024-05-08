package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import group.aelysium.rustyconnector.core.lib.cache.CacheableMessage;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageService;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.Database;

import java.util.*;

public class PlayerService implements IPlayerService {
    private final Map<UUID, Boolean> recentPlayers;
    private final StorageService storage;

    public PlayerService(StorageService storage) {
        this.storage = storage;
        this.recentPlayers = new LinkedHashMap<>(100){
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return this.size() > 100;
            }
        };
    }

    public void store(IPlayer player) {
        if(this.recentPlayers.containsKey(player.uuid())) return;
        this.storage.database().players().set(player);
        this.recentPlayers.put(player.uuid(), false);
    }

    public Optional<IPlayer> fetch(UUID uuid) {
        return this.storage.database().players().get(uuid);
    }

    public Optional<IPlayer> fetch(String username) {
        return this.storage.database().players().get(username);
    }

    @Override
    public void kill() {
        // this.storage.kill();  -  Storage is cleaned up in a different process.
    }
}
