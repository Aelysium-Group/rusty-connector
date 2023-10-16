package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.MySQLStorage;

public class PlayerService extends Service {
    protected PlayerDataEnclave dataEnclave;

    public PlayerService(MySQLStorage connector) throws Exception {
        this.dataEnclave = new PlayerDataEnclave(connector);
    }

    public PlayerDataEnclave dataEnclave() { return this.dataEnclave; }

    @Override
    public void kill() {
    }
}
