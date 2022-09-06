package group.aelysium.rustyconnector.plugin.velocity.lib.generic;

import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerFamily;
import group.aelysium.rustyconnector.core.lib.generic.Lang;
import group.aelysium.rustyconnector.core.lib.generic.firewall.Whitelist;

import java.util.*;

public class Proxy implements group.aelysium.rustyconnector.core.lib.generic.server.Proxy {
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

    public ServerFamily findFamily(String name) {
        return this.getRegisteredFamilies().stream()
                .filter(family ->
                        Objects.equals(family.getName(), name)
                ).findFirst().orElse(null);
    }

    public void printFamilies() {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        Lang.print(plugin.logger(), Lang.get("registered-families"));
        plugin.logger().log(Lang.spacing());
        this.getRegisteredFamilies().forEach(family -> {
            plugin.logger().log("   ---| "+family.getName());
        });
        plugin.logger().log(Lang.spacing());
        plugin.logger().log("To see more details about a particular family use:");
        plugin.logger().log("/rc family info <family name>");
        plugin.logger().log("To see all servers currently saved to a family use:");
        plugin.logger().log("/rc family info <family name> servers");
        plugin.logger().log(Lang.spacing());
        plugin.logger().log(Lang.border());
    }
}
