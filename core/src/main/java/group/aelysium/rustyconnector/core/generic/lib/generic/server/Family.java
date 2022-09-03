package group.aelysium.rustyconnector.core.generic.lib.generic.server;

import group.aelysium.rustyconnector.core.generic.lib.generic.whitelist.Whitelist;
import group.aelysium.rustyconnector.core.generic.lib.generic.load_balancing.AlgorithmType;

import java.util.Map;

public interface Family {
    String name = "";
    int playerCount = 0;
    AlgorithmType algorithm = AlgorithmType.TROUGH;
    Whitelist whitelist = null;

    String getName();

    void registerServer(Server server);

    Map<Object, Server> getRegisteredServers();
}
