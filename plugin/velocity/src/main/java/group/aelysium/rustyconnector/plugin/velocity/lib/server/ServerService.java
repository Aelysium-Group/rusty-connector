package group.aelysium.rustyconnector.plugin.velocity.lib.server;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.server.IServerService;
import group.aelysium.rustyconnector.toolkit.velocity.util.AddressUtil;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerService implements IServerService<MCLoader, Player, LoadBalancer, Family> {
    protected final Map<ServerInfo, MCLoader> servers = new ConcurrentHashMap<>(); // Should be used exclusively for serverInfo based lookups.
    private final int serverTimeout;
    private final int serverInterval;

    protected ServerService(int serverTimeout, int serverInterval) {
        this.serverTimeout = serverTimeout;
        this.serverInterval = serverInterval;
    }

    public int serverTimeout() {
        return this.serverTimeout;
    }

    public int serverInterval() {
        return this.serverInterval;
    }

    public void add(MCLoader mcLoader) {
        this.servers.put(mcLoader.serverInfo(), mcLoader);
    }
    public void remove(MCLoader mcLoader) {
        this.servers.remove(mcLoader.serverInfo());
    }

    public Optional<MCLoader> fetch(ServerInfo serverInfo) {
        MCLoader loader = this.servers.get(serverInfo);
        if(loader == null) return Optional.empty();
        return Optional.of(loader);
    }

    protected Optional<K8MCLoader> fetchPods(String podName) {
        for(Family family : Tinder.get().services().family().dump()) {
            try {
                K8MCLoader found = (K8MCLoader) family.loadBalancer().servers().stream().filter(s -> {
                    if (!(s instanceof K8MCLoader)) return false;
                    return ((K8MCLoader) s).podName().equals(podName);
                }).findAny().orElseThrow();

                return Optional.of(found);
            } catch (Exception ignore) {
                if(family.loadBalancer().size(true) == 0) continue;

                K8MCLoader found = (K8MCLoader) family.loadBalancer().lockedServers().stream().filter(s -> {
                    if (!(s instanceof K8MCLoader)) return false;
                    return ((K8MCLoader) s).podName().equals(podName);
                }).findAny().orElseThrow();

                return Optional.of(found);
            }
        }
        return Optional.empty();
    }

    protected Optional<K8MCLoader> fetchPods(String podName, String familyName) {
        Family family = (Family) new Family.Reference(familyName).get();

        try {
            K8MCLoader found = (K8MCLoader) family.loadBalancer().servers().stream().filter(s -> {
                if (!(s instanceof K8MCLoader)) return false;
                return ((K8MCLoader) s).podName().equals(podName);
            }).findAny().orElseThrow();

            return Optional.of(found);
        } catch (Exception ignore1) {}


        if(family.loadBalancer().size(true) == 0) return Optional.empty();


        try {
            K8MCLoader found = (K8MCLoader) family.loadBalancer().lockedServers().stream().filter(s -> {
                if (!(s instanceof K8MCLoader)) return false;
                return ((K8MCLoader) s).podName().equals(podName);
            }).findAny().orElseThrow();

            return Optional.of(found);
        } catch (Exception ignore) {}


        return Optional.empty();
    }

    public List<MCLoader> servers() {
        return this.servers.values().stream().toList();
    }

    public boolean contains(ServerInfo serverInfo) {
        return this.servers.containsKey(serverInfo);
    }

    /**
     * Registers fake servers into the proxy to help with testing systems.
     */
    public void registerFakeServers() {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

        for (Family family : api.services().family().dump()) {
            logger.log("---| Starting on: " + family.id());
            // Register 1000 servers into each family
            for (int i = 0; i < 1000; i++) {
                InetSocketAddress address = AddressUtil.stringToAddress("localhost:"+i);
                String name = "server"+i;

                ServerInfo info = new ServerInfo(name, address);
                MCLoader server = new MCLoader(info, 40, 50, 0, this.serverTimeout);
                server.setPlayerCount((int) (Math.random() * 50));

                try {
                    RegisteredServer registeredServer = api.velocityServer().registerServer(server.serverInfo());
                    server.setRegisteredServer(registeredServer);

                    family.addServer(server);

                    logger.log("-----| Added: " + server.serverInfo() + " to " + family.id());
                } catch (Exception ignore) {}
            }
        }
    }

    public void kill() {
        this.servers.clear();
    }

    public static class Builder {
        protected int timeout = 15;
        protected int interval = 10;

        public ServerService.Builder setServerTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public ServerService.Builder setServerInterval(int interval) {
            this.interval = interval;
            return this;
        }

        public ServerService build() {
            return new ServerService(timeout, interval);
        }
    }

    public static class ServerBuilder {
        private ServerInfo serverInfo;
        private String podName;
        private int weight;
        private int softPlayerCap;
        private int hardPlayerCap;

        protected int initialTimeout = 15;

        public ServerService.ServerBuilder setServerInfo(ServerInfo serverInfo) {
            this.serverInfo = serverInfo;
            return this;
        }

        public ServerService.ServerBuilder setWeight(int weight) {
            this.weight = weight;
            return this;
        }

        public ServerService.ServerBuilder setSoftPlayerCap(int softPlayerCap) {
            this.softPlayerCap = softPlayerCap;
            return this;
        }

        public ServerService.ServerBuilder setHardPlayerCap(int hardPlayerCap) {
            this.hardPlayerCap = hardPlayerCap;
            return this;
        }

        public ServerService.ServerBuilder setPodName(String podName) {
            this.podName = podName;
            return this;
        }

        public MCLoader build() {
            this.initialTimeout = Tinder.get().services().server().serverTimeout();

            if(this.podName.equals("")) return new MCLoader(serverInfo, softPlayerCap, hardPlayerCap, weight, initialTimeout);
            return new K8MCLoader(this.podName, serverInfo, softPlayerCap, hardPlayerCap, weight, initialTimeout);
        }
    }
}
