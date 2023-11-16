package group.aelysium.rustyconnector.plugin.velocity.lib.storage;

import group.aelysium.rustyconnector.toolkit.velocity.storage.IStorageRoot;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.static_family.ServerResidence;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendMapping;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.RustyPlayer;

import java.util.HashSet;
import java.util.Set;

public class StorageRoot implements IStorageRoot<RustyPlayer, FriendMapping, ServerResidence> {
    private final Set<RustyPlayer> players = new HashSet<>();
    private final Set<FriendMapping> friends = new HashSet<>();
    private final Set<ServerResidence> residence = new HashSet<>();

    public Set<RustyPlayer> players() {
        return players;
    }

    public Set<FriendMapping> friends() {
        return friends;
    }

    public Set<ServerResidence> residence() {
        return residence;
    }
}
