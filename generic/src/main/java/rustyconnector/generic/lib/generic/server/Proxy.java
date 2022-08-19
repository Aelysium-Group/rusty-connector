package group.aelysium.rustyconnector.plugin.velocity.lib.generic.server;

import group.aelysium.rustyconnector.plugin.velocity.lib.generic.Whitelist;

import java.util.ArrayList;
import java.util.List;

public class Proxy {
    private static List<Proxy> proxies = new ArrayList<>();
    private final String name;
    private final Server rootServer;
    private final List<Server> servers;

    private final Whitelist whitelist;

    private Proxy(ProxyBuilder builder) {
        this.name = builder.name;
        this.servers = builder.servers;
        this.whitelist = builder.whitelist;
    }

    public String getName() {
        return name;
    }

    public List<Server> getServers() {
        return servers;
    }

    public Whitelist getWhitelist() {
        return whitelist;
    }

    public static class ProxyBuilder
    {
        private final String name;
        private final Family root;
        private List<Family> families = new ArrayList<>();
        private Whitelist whitelist = null;

        public ProxyBuilder(String name, Family root) {
            this.name = name;
            this.root = root;
        }
        public ProxyBuilder whitelist(Whitelist whitelist) {
            this.whitelist = whitelist;
            return this;
        }
        public ProxyBuilder addFamily(Family family) {
            this.families.add(family);
            return this;
        }

        public Proxy build() {
            return new Proxy(this);
        }
    }
}
