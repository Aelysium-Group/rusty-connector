package group.aelysium.rustyconnector.core.generic.lib.generic.load_balancing;

import group.aelysium.rustyconnector.core.generic.lib.generic.server.Family;
import group.aelysium.rustyconnector.core.generic.lib.generic.server.Server;

import java.util.Map;

public class TroughAlgorithm extends Algorithm {
    @Override
    public Server processConnection(Family family) {
        Map<Object,Server> servers = family.getRegisteredServers();
        final Server[] lowestValue = {servers.get(0)};
        servers.forEach((key, server) -> {
            if(server.getPlayerCount() < lowestValue[0].getPlayerCount()) lowestValue[0] = server;
        });

        return lowestValue[0];
    }
}
