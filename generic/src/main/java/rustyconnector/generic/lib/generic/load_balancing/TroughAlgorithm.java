package rustyconnector.generic.lib.generic.load_balancing;

import rustyconnector.generic.lib.generic.server.Family;
import rustyconnector.generic.lib.generic.server.Server;

import java.util.List;

public class TroughAlgorithm extends Algorithm {
    @Override
    public Server processConnection(Family family) {
        List<Server> servers = family.getRegisteredServers();
        final Server[] lowestValue = {servers.get(0)};
        servers.forEach(server -> {
            if(server.getPlayerCount() < lowestValue[0].getPlayerCount()) lowestValue[0] = server;
        });

        return lowestValue[0];
    }
}
