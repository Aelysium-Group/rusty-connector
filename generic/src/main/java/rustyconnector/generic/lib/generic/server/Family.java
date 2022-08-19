package rustyconnector.generic.lib.generic.server;

import rustyconnector.generic.lib.generic.whitelist.Whitelist;

import java.util.ArrayList;
import java.util.List;

public class Family {
    private final List<Server> servers = new ArrayList<>();
    private String name;
    private LoadBalancingAlgorithm algorithm = LoadBalancingAlgorithm.TROUGH;
    private Whitelist whitelist = null;

    public Family(String name) {
        this.name = name;
    }
    public Family(String name, LoadBalancingAlgorithm algorithm) {
        this.name = name;
        this.algorithm = algorithm;
    }
    public Family(String name, LoadBalancingAlgorithm algorithm, Whitelist whitelist) {
        this.name = name;
        this.algorithm = algorithm;
        this.whitelist = whitelist;
    }
}
