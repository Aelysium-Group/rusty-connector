package group.aelysium.rustyconnector.plugin.velocity.lib.storage;

import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RankedGame;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IStorageRoot;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.static_family.ServerResidence;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendMapping;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class StorageRoot implements IStorageRoot<Player, FriendMapping, ServerResidence> {
    private final Set<Player> players = new HashSet<>();
    private final Set<FriendMapping> friends = new HashSet<>();
    private final Set<ServerResidence> residence = new HashSet<>();
    private final HashMap<String, RankedGame> games = new HashMap<>();

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
    public void saveGame(MySQLStorage storage, RankedGame game) {
        games.put(game.name(), game);

        storage.store(this.games);
    }
}
