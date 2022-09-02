package rustyconnector.generic.lib.generic.server;

import rustyconnector.generic.lib.generic.whitelist.Whitelist;
import rustyconnector.generic.lib.generic.load_balancing.AlgorithmType;

import java.util.ArrayList;
import java.util.List;

public interface Family {
    List<Server> registeredServers = new ArrayList<>();
    String name = "";
    int playerCount = 0;
    AlgorithmType algorithm = AlgorithmType.TROUGH;
    Whitelist whitelist = null;

    String getName();

    void registerServer(Server server);
}
