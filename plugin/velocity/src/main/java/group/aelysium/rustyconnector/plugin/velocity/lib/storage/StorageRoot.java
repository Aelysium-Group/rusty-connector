package group.aelysium.rustyconnector.plugin.velocity.lib.storage;

import group.aelysium.rustyconnector.plugin.velocity.lib.family.static_family.ServerResidence;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendMapping;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.FakePlayer;

import java.util.ArrayList;
import java.util.List;

public class StorageRoot{
    private final String name = "RustyConnector-storage";

    private final List<FakePlayer> players = new ArrayList<>();
    private final List<FriendMapping> friends = new ArrayList<>();
    private final List<ServerResidence> residence = new ArrayList<>();

    public List<FakePlayer> players() {
        return players;
    }

    public List<FriendMapping> friends() {
        return friends;
    }

    public List<ServerResidence> residence() {
        return residence;
    }


}
