package group.aelysium.rustyconnector.plugin.velocity.lib.server;

import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.server.IServerService;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerService implements IServerService {
    protected final Map<UUID, IMCLoader> servers = new ConcurrentHashMap<>();
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

    public void add(IMCLoader mcLoader) {
        this.servers.put(mcLoader.uuid(), mcLoader);
    }
    public void remove(IMCLoader mcLoader) {
        this.servers.remove(mcLoader.uuid());
    }

    public Optional<IMCLoader> fetch(UUID uuid) {
        IMCLoader loader = this.servers.get(uuid);
        if(loader == null) return Optional.empty();
        return Optional.of(loader);
    }

    protected Optional<K8MCLoader> fetchPods(String podName) {
        for(IFamily family : Tinder.get().services().family().dump()) {
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
        Family family = new Family.Reference(familyName).get();

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

    public List<IMCLoader> servers() {
        return this.servers.values().stream().toList();
    }

    public boolean contains(UUID uuid) {
        return this.servers.containsKey(uuid);
    }

    /**
     * Registers fake servers into the proxy to help with testing systems.
     */
    /*public void registerFakeServers() {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

        for (Family family : api.services().family().dump()) {
            logger.log("---| Starting on: " + family.id());
            // Register 1000 servers into each family
            for (int i = 0; i < 1000; i++) {
                InetSocketAddress address = AddressUtil.stringToAddress("localhost:"+i);
                String name = "fakeSRV-"+i;

                MCLoader server = new MCLoader(UUID.randomUUID(), address, name, 40, 50, 0, this.serverTimeout);
                server.setPlayerCount((int) (Math.random() * 50));

                try {
                    RegisteredServer registeredServer = api.velocityServer().registerServer(server.serverInfo());
                    server.registeredServer(registeredServer);

                    family.addServer(server);

                    logger.log("-----| Added: " + server.serverInfo() + " to " + family.id());
                } catch (Exception ignore) {}
            }
        }
    }*/

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
}
