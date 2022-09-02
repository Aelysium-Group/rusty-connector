package group.aelysium.rustyconnector.plugin.velocity.lib.generic;

import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerFamily;
import rustyconnector.generic.lib.generic.whitelist.Whitelist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Proxy implements rustyconnector.generic.lib.generic.server.Proxy {
    private String privateKey = null;
    private VelocityRustyConnector plugin;
    private ServerFamily rootFamily;
    private String proxyWhitelist;
    private List<ServerFamily> registeredFamilies = new ArrayList<>();
    private Map<String, Whitelist> registeredWhitelists = new HashMap<>();
    public List<ServerFamily> getRegisteredFamilies() { return this.registeredFamilies; }

    public ServerFamily getRootFamily() { return this.rootFamily; }

    public Proxy(VelocityRustyConnector plugin, String privateKey) {
        this.plugin = plugin;
        this.privateKey = privateKey;
    }

    @Override
    public void init() {
        this.requestGlobalRegistration();
    }

    @Override
    public void requestGlobalRegistration() {

    }

    @Override
    public void registerWhitelist(String name, Whitelist whitelist) {
        this.registeredWhitelists.put(name, whitelist);
    }

    @Override
    public Whitelist getProxyWhitelist() {
        return this.registeredWhitelists.get(this.proxyWhitelist);
    }

    @Override
    public Whitelist getWhitelist(String name) {
        return this.registeredWhitelists.get(name);
    }

    /**
     * Registers a family to the proxy
     * @param family The family to register
     */
    public void registerFamily(ServerFamily family) {
        this.registeredFamilies.add(family);
    }


    public boolean validatePrivateKey(String keyToValidate) {
        return this.privateKey.equals(keyToValidate);
    }
}
