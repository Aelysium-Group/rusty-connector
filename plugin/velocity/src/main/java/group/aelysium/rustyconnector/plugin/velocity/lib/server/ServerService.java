package group.aelysium.rustyconnector.plugin.velocity.lib.server;

import com.sun.jdi.request.DuplicateRequestException;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.core.log_gate.GateKey;
import group.aelysium.rustyconnector.toolkit.velocity.server.IServerService;
import group.aelysium.rustyconnector.toolkit.velocity.util.AddressUtil;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Vector;

public class ServerService implements IServerService<MCLoader, Player, Family> {
    private final Vector<WeakReference<MCLoader>> servers =  new Vector<>();

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

    /**
     * Search for a server.
     * @param serverInfo The server info to search for.
     * @return A server or `null`
     */
    protected Optional<MCLoader> fetch(ServerInfo serverInfo) {
        for(Family family : Tinder.get().services().family().dump()) {
            MCLoader server = family.findServer(serverInfo);
            if(server == null) continue;

            return Optional.of(server);
        }
        return Optional.empty();
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
                if(family.lockedServers().size() == 0) continue;

                K8MCLoader found = (K8MCLoader) family.lockedServers().stream().filter(s -> {
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


        if(family.lockedServers().size() == 0) return Optional.empty();


        try {
            K8MCLoader found = (K8MCLoader) family.lockedServers().stream().filter(s -> {
                if (!(s instanceof K8MCLoader)) return false;
                return ((K8MCLoader) s).podName().equals(podName);
            }).findAny().orElseThrow();

            return Optional.of(found);
        } catch (Exception ignore) {}


        return Optional.empty();
    }

    public Vector<WeakReference<MCLoader>> servers() {
        return this.servers;
    }

    public boolean contains(ServerInfo serverInfo) {
        for(Family family : Tinder.get().services().family().dump()) {
            if(family.containsServer(serverInfo)) return true;
        }
        return false;
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

    /**
     * Register a server to the proxy.
     * @param server The server to be registered.
     * @param family The family to register the server into.
     * @return A RegisteredServer node.
     */
    public RegisteredServer registerServer(MCLoader server, Family family) throws Exception {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

        try {
            if(logger.loggerGate().check(GateKey.REGISTRATION_ATTEMPT))
                VelocityLang.REGISTRATION_REQUEST.send(logger, server.serverInfo(), family.id());

            if(this.contains(server.serverInfo())) throw new DuplicateRequestException("Server ["+server.serverInfo().getName()+"]("+server.serverInfo().getAddress()+":"+server.serverInfo().getAddress().getPort()+") can't be registered twice!");

            RegisteredServer registeredServer = api.registerServer(server.serverInfo());
            if(registeredServer == null) throw new NullPointerException("Unable to register the server to the proxy.");

            family.addServer(server);

            this.servers.add(new WeakReference<>(server));

            if(logger.loggerGate().check(GateKey.REGISTRATION_ATTEMPT))
                VelocityLang.REGISTERED.send(logger, server.serverInfo(), family.id());

            WebhookEventManager.fire(WebhookAlertFlag.SERVER_REGISTER, DiscordWebhookMessage.PROXY__SERVER_REGISTER.build(server, family.id()));
            WebhookEventManager.fire(WebhookAlertFlag.SERVER_REGISTER, family.id(), DiscordWebhookMessage.FAMILY__SERVER_REGISTER.build(server, family.id()));
            return registeredServer;
        } catch (Exception error) {
            if(logger.loggerGate().check(GateKey.REGISTRATION_ATTEMPT))
                VelocityLang.ERROR.send(logger, server.serverInfo(), family.id());
            throw new Exception(error.getMessage());
        }
    }

    /**
     * Unregister a server from the proxy.
     * @param serverInfo The server to be unregistered.
     * @param familyName The id of the family associated with the server.
     * @param removeFromFamily Should the server be removed from it's associated family?
     */
    public void unregisterServer(ServerInfo serverInfo, String familyName, Boolean removeFromFamily) throws Exception {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();
        try {
            MCLoader server = new MCLoader.Reference(serverInfo).get();

            if (logger.loggerGate().check(GateKey.UNREGISTRATION_ATTEMPT))
                VelocityLang.UNREGISTRATION_REQUEST.send(logger, serverInfo, familyName);

            Family family = server.family();

            api.unregisterServer(server.serverInfo());
            if (removeFromFamily)
                family.removeServer(server);

            if (logger.loggerGate().check(GateKey.UNREGISTRATION_ATTEMPT))
                VelocityLang.UNREGISTERED.send(logger, serverInfo, familyName);

            WebhookEventManager.fire(WebhookAlertFlag.SERVER_UNREGISTER, DiscordWebhookMessage.PROXY__SERVER_UNREGISTER.build(server));
            WebhookEventManager.fire(WebhookAlertFlag.SERVER_UNREGISTER, familyName, DiscordWebhookMessage.FAMILY__SERVER_UNREGISTER.build(server));
        } catch (NullPointerException e) {
            if(logger.loggerGate().check(GateKey.UNREGISTRATION_ATTEMPT))
                VelocityLang.ERROR.send(logger, serverInfo, familyName);
            throw new NullPointerException(e.getMessage());
        } catch (NoSuchElementException ignore) {
            if(logger.loggerGate().check(GateKey.UNREGISTRATION_ATTEMPT))
                VelocityLang.ERROR.send(logger, serverInfo, familyName);
            throw new NullPointerException("Server ["+serverInfo.getName()+"]("+serverInfo.getAddress()+":"+serverInfo.getAddress().getPort()+") doesn't exist! It can't be unregistered!");
        } catch (Exception e) {
            if(logger.loggerGate().check(GateKey.UNREGISTRATION_ATTEMPT))
                VelocityLang.ERROR.send(logger, serverInfo, familyName);
            throw new Exception(e);
        }
    }

    public void kill() {
        this.servers.forEach(Reference::clear);
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
        private String familyName;
        private int playerCount = 0;
        private int weight;
        private int softPlayerCap;
        private int hardPlayerCap;

        private String parentFamilyName;

        protected int initialTimeout = 15;

        public ServerService.ServerBuilder setServerInfo(ServerInfo serverInfo) {
            this.serverInfo = serverInfo;
            return this;
        }

        public ServerService.ServerBuilder setFamilyName(String familyName) {
            this.familyName = familyName;
            return this;
        }

        public ServerService.ServerBuilder setParentFamilyName(String parentFamilyName) {
            this.parentFamilyName = parentFamilyName;
            return this;
        }

        public ServerService.ServerBuilder setPlayerCount(int playerCount) {
            this.playerCount = playerCount;
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

        public MCLoader build() {
            this.initialTimeout = Tinder.get().services().server().serverTimeout();

            return new MCLoader(serverInfo, softPlayerCap, hardPlayerCap, weight, initialTimeout);
        }
    }
}
