package group.aelysium.rustyconnector.plugin.velocity.lib.storage;

import group.aelysium.rustyconnector.plugin.velocity.lib.family.static_family.ServerResidence;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendMapping;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.ResolvablePlayer;
import one.microstream.reference.Lazy;

import java.util.ArrayList;
import java.util.List;

public class StorageRoot{
    private final String name = "RustyConnector-storage";

    private final List<ResolvablePlayer> players = new ArrayList<>();
    private final List<FriendMapping> friends = new ArrayList<>();
    private final List<ServerResidence> residence = new ArrayList<>();

    public List<ResolvablePlayer> players() {
        return players;
    }

    public List<FriendMapping> friends() {
        return friends;
    }

    public List<ServerResidence> residence() {
        return residence;
    }


}
