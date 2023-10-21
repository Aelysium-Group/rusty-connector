package group.aelysium.rustyconnector.plugin.velocity.lib.storage;

import group.aelysium.rustyconnector.plugin.velocity.lib.family.static_family.ServerResidence;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendMapping;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.ResolvablePlayer;
import one.microstream.reference.Lazy;

import java.util.ArrayList;
import java.util.List;

public class StorageRoot{
    private final String name = "RustyConnector-storage";

    private final Lazy<List<ResolvablePlayer>> players = Lazy.Reference(new ArrayList<>());
    private final Lazy<List<FriendMapping>> friends = Lazy.Reference(new ArrayList<>());
    private final Lazy<List<ServerResidence>> residence = Lazy.Reference(new ArrayList<>());

    public List<ResolvablePlayer> players() {
        return players.get();
    }

    public List<FriendMapping> friends() {
        return friends.get();
    }

    public List<ServerResidence> residence() {
        return residence.get();
    }


}
