package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.serviceable.ServiceableService;

import java.io.SyncFailedException;
import java.util.UUID;

public class PlayerService extends ServiceableService<PlayerServiceHandler> {

    public PlayerService(PlayerMySQLService playerMySQLService) {
        super(new PlayerServiceHandler());
        this.services.add(new PlayerDataEnclaveService(playerMySQLService));
    }

    public FakePlayer findPlayer(UUID uuid) throws SyncFailedException {
        return this.services.dataEnclave().findPlayer(uuid);
    }

    public void cachePlayer(Player player) {
        this.services.dataEnclave().cachePlayer(player);
    }

    public void savePlayer(Player player) {
        this.services.dataEnclave().savePlayer(player);
    }

    @Override
    public void kill() {
        this.services.killAll();
    }
}
