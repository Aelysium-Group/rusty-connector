package rustyconnector.generic.lib.generic.server;

import ninja.leaping.configurate.ConfigurationNode;
import rustyconnector.RustyConnector;
import rustyconnector.generic.database.Redis;
import rustyconnector.generic.lib.generic.Config;

import java.util.ArrayList;
import java.util.List;

public class Proxy {
    private RustyConnector plugin;
    private String privateKey = null;
    private List<Family> registeredFamilies = new ArrayList<>();
    private Family rootFamily;
    private Redis redis;

    public Proxy(RustyConnector plugin, String privateKey) {
        this.plugin = plugin;
        this.privateKey = privateKey;
    }

    public void init() {}

    /**
     * Send a request over Redis asking all servers to register themselves
     */
    public void requestGlobalRegistration() {
    }

}
