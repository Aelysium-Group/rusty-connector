package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.MySQLStorage;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageRoot;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PlayerService implements IPlayerService {
    private final MySQLStorage storage;

    public PlayerService(MySQLStorage storage) {
        this.storage = storage;
    }

    public Optional<RustyPlayer> fetch(UUID uuid) {
        try {
            StorageRoot root = this.storage.root();

            return root.players().stream().filter(fakePlayer -> fakePlayer.uuid().equals(uuid)).findAny();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public Optional<RustyPlayer> fetch(String username) {
        try {
            StorageRoot root = this.storage.root();

            return root.players().stream().filter(fakePlayer -> fakePlayer.username().equals(username)).findAny();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public void savePlayer(Player player) {
        try {
            StorageRoot root = this.storage.root();

            Set<RustyPlayer> players = root.players();
            players.add(RustyPlayer.from(player));

            this.storage.store(players);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void kill() {}
}
