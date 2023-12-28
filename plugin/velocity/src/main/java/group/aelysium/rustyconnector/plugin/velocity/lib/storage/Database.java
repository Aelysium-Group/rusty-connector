package group.aelysium.rustyconnector.plugin.velocity.lib.storage;

import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RankedGame;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IStorageRoot;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.static_family.ServerResidence;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendMapping;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import org.eclipse.serializer.collections.lazy.LazyHashMap;
import org.eclipse.serializer.collections.lazy.LazyHashSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Database implements IStorageRoot<Player, FriendMapping, ServerResidence> {
    private final LazyHashSet<Player> players = new LazyHashSet<>();
    private final LazyHashSet<FriendMapping> friends = new LazyHashSet<>();
    private final LazyHashSet<ServerResidence> residence = new LazyHashSet<>();
    private final LazyHashMap<String, RankedGame> games = new LazyHashMap<>();

    public Set<Player> players() {
        return players;
    }

    public Set<FriendMapping> friends() {
        return friends;
    }

    public Set<ServerResidence> residence() {
        return residence;
    }

    public Optional<RankedGame> getGame(String name) {
        return Optional.ofNullable(games.get(name));
    }
    public void saveGame(StorageService storage, RankedGame game) {
        games.put(game.name(), game);

        storage.store(this.games);
    }
    public boolean deleteGame(StorageService storage, String name) {
        RankedGame game = games.remove(name);

        storage.store(this.games);

        return game == null;
    }
}
