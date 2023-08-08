package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;

import java.io.SyncFailedException;
import java.util.*;

/**
 * The data enclave service allows you to store database responses in-memory
 * to be used later.
 * If a value is available in-memory, data enclave will return that.
 * If not, it will query the database.
 */
public class PlayerDataEnclaveService extends Service {
    private final Vector<FakePlayer> cache = new Vector<>(); // Max number of players that can be stored at once
    private final PlayerMySQLService mySQLService;

    public PlayerDataEnclaveService(PlayerMySQLService mySQLService) {
        this.mySQLService = mySQLService;
    }

    public void uncachePlayer(Player player) {
        this.cache.removeIf(playerI -> playerI.equals(FakePlayer.from(player)));
    }
    public void uncachePlayer(FakePlayer player) {
        this.cache.removeIf(playerI -> playerI.equals(player));
    }
    public void uncachePlayer(UUID uuid) {
        this.cache.removeIf(player -> player.uuid().equals(uuid));
    }
    public void uncachePlayer(String username) {
        this.cache.removeIf(player -> player.username().equals(username));
    }

    public void cachePlayer(Player player) {
        this.cache.add(FakePlayer.from(player));

        if(this.cache.size() > 100) this.cache.setSize(100);
    }
    public void cachePlayer(FakePlayer player) {
        this.cache.add(player);

        if(this.cache.size() > 100) this.cache.setSize(100);
    }

    public FakePlayer findPlayer(UUID uuid) throws SyncFailedException {
        // Check velocity for online players first
        try {
            FakePlayer fakePlayer = FakePlayer.from(VelocityAPI.get().velocityServer().getPlayer(uuid).orElseThrow());
            cachePlayer(fakePlayer);

            return fakePlayer;
        } catch (Exception ignore) {}

        // Check the local cache for a matching player
        try {
            return this.cache.stream().filter(fakePlayer1 -> fakePlayer1.uuid().equals(uuid)).findFirst().orElseThrow();
        } catch (Exception ignore) {}

        // Ask MySQL for the player and then cache it.
        try {
            FakePlayer fakePlayer = this.mySQLService.resolveUUID(uuid).orElseThrow();

            this.cachePlayer(fakePlayer);

            return fakePlayer;
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new SyncFailedException("The requested player has never joined the network!");
    }

    public void savePlayer(Player player) {
        try {
            this.mySQLService.addPlayer(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void kill() {
        this.cache.clear();
    }
}
