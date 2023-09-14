package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import group.aelysium.rustyconnector.core.lib.connectors.implementors.storage.mysql.MySQLConnector;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;

public class PlayerService extends Service {
    protected PlayerDataEnclave dataEnclave;

    public PlayerService(MySQLConnector connector) throws Exception {
        this.dataEnclave = new PlayerDataEnclave(connector);
    }

    public PlayerDataEnclave dataEnclave() { return this.dataEnclave; }

    @Override
    public void kill() {
        this.dataEnclave.kill();
    }
}
