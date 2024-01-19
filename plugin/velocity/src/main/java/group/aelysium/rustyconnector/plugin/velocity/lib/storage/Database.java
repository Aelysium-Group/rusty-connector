package group.aelysium.rustyconnector.plugin.velocity.lib.storage;

import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.GamemodeRankManager;
import group.aelysium.rustyconnector.toolkit.velocity.family.static_family.IServerResidence;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.friends.PlayerPair;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IStorageRoot;
import org.eclipse.serializer.collections.lazy.LazyHashMap;
import org.eclipse.serializer.collections.lazy.LazyHashSet;

import java.util.*;

public class Database implements IStorageRoot {
    private final Map<UUID, Player> players = new LazyHashMap<>();
    private final Set<PlayerPair> friends = new LazyHashSet<>();
    private final Map<String, Map<UUID, IServerResidence.MCLoaderEntry>> residence = new LazyHashMap<>();
    private final Map<String, GamemodeRankManager> games = new LazyHashMap<>();

    public void savePlayer(StorageService storage, Player player) {
        this.players.put(player.uuid(), player);

        storage.store(this.players);
    }
    public Map<UUID, Player> players() {
        return players;
    }

    public Set<PlayerPair> friends() {
        return friends;
    }

    public Map<String, Map<UUID, IServerResidence.MCLoaderEntry>> residence() {
        return residence;
    }

    public Optional<GamemodeRankManager> getGame(String name) {
        return Optional.ofNullable(games.get(name));
    }
    public void saveGame(StorageService storage, GamemodeRankManager game) {
        games.put(game.name(), game);

        storage.store(this.games);
    }
    public boolean deleteGame(StorageService storage, String name) {
        GamemodeRankManager game = games.remove(name);

        storage.store(this.games);

        return game == null;
    }
}
