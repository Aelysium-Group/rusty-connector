package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayerService;
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
            Optional<com.velocitypowered.api.proxy.Player> velocityPlayer = Tinder.get().velocityServer().getPlayer(uuid);
            Database root = this.storage.database();

            if(velocityPlayer.isEmpty()) {
                Player player = root.players().get(uuid);
                if (player == null) return Optional.empty();

                return Optional.of(player);
            }

            return Optional.of(Player.from(velocityPlayer.orElseThrow()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public Optional<Player> fetch(String username) {
        try {
            Optional<com.velocitypowered.api.proxy.Player> velocityPlayer = Tinder.get().velocityServer().getPlayer(username);
            Database root = this.storage.database();

            if(velocityPlayer.isEmpty())
                return root.players().values().stream().filter(fakePlayer -> fakePlayer.username().equals(username)).findAny();

            return Optional.of(Player.from(velocityPlayer.orElseThrow()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public void kill() {
        // this.storage.kill();  -  Storage is cleaned up in a different process.
    }
}
