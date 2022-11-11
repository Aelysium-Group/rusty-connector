package group.aelysium.rustyconnector.core.lib.model;

import group.aelysium.rustyconnector.core.lib.firewall.Whitelist;
import group.aelysium.rustyconnector.core.lib.load_balancing.AlgorithmType;

public interface Family {
    String name = "";
    int playerCount = 0;
    AlgorithmType algorithm = AlgorithmType.TROUGH;
    Whitelist whitelist = null;

    String getName();

    void registerServer(Server server);
}
