package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageService;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.Database;

import java.util.Optional;
import java.util.UUID;

public class PlayerService implements IPlayerService {
    private final StorageService storage;

    public PlayerService(StorageService storage) {
        this.storage = storage;
    }

    public Optional<Player> fetch(UUID uuid) {
        try {
            Database root = this.storage.root();

            return root.players().stream().filter(fakePlayer -> fakePlayer.uuid().equals(uuid)).findAny();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public Optional<Player> fetch(String username) {
        try {
            Database root = this.storage.root();

            return root.players().stream().filter(fakePlayer -> fakePlayer.username().equals(username)).findAny();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public void kill() {}
}
