package group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing;

import group.aelysium.rustyconnector.core.lib.sorting.SortByLeastPlayers;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PaperServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.ServerFamily;

import java.util.List;

public class TroughAlgorithm extends Algorithm {
    @Override
    public List<PaperServer> balance(ServerFamily family) {
        List<PaperServer> servers = family.getRegisteredServers();

        servers.sort(new SortByLeastPlayers());

        return servers;
    }
}