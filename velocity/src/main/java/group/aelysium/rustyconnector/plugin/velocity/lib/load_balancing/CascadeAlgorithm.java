package group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing;

import group.aelysium.rustyconnector.core.lib.generic.server.Family;
import group.aelysium.rustyconnector.core.lib.generic.server.Server;
import group.aelysium.rustyconnector.core.lib.generic.sorting.SortByLeastPlayers;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PaperServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerFamily;

import java.util.List;

public class CascadeAlgorithm extends Algorithm {
    @Override
    public List<PaperServer> balance(ServerFamily family) {
        List<PaperServer> servers = family.getRegisteredServers();

        servers.sort(new SortByLeastPlayers());

        return servers;
    }
}
