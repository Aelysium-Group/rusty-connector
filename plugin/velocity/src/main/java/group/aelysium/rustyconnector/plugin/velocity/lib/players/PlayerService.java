package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.MySQLStorage;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageRoot;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

import java.io.SyncFailedException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlayerService extends Service {
    private final EmbeddedStorageManager storage;

    public PlayerService(MySQLStorage storage) {
        this.storage = storage.storageManager();
    }

    public Optional<FakePlayer> fetch(UUID uuid) {
        try {
            StorageRoot root = (StorageRoot) this.storage.root();

            return root.players().stream().filter(fakePlayer -> fakePlayer.uuid().equals(uuid)).findAny();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public Optional<FakePlayer> fetch(String username) {
        try {
            StorageRoot root = (StorageRoot) this.storage.root();

            return root.players().stream().filter(fakePlayer -> fakePlayer.username().equals(username)).findAny();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public void savePlayer(Player player) {
        try {
            StorageRoot root = (StorageRoot) this.storage.root();

            List<FakePlayer> players = root.players();
            players.add(FakePlayer.from(player));

            this.storage.store(players);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void kill() {}
}
