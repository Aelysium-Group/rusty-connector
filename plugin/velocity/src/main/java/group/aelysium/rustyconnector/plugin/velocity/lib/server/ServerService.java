package group.aelysium.rustyconnector.plugin.velocity.lib.server;

import com.sun.jdi.request.DuplicateRequestException;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.api.velocity.log_gate.GateKey;
import group.aelysium.rustyconnector.api.velocity.server.IServerService;
import group.aelysium.rustyconnector.api.velocity.util.AddressUtil;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.Vector;

public class ServerService implements IServerService<PlayerServer, BaseFamily> {
    private final Vector<WeakReference<PlayerServer>> servers =  new Vector<>();

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
    public PlayerServer search(ServerInfo serverInfo) {
        for(BaseFamily family : Tinder.get().services().family().dump()) {
            PlayerServer server = family.findServer(serverInfo);
            if(server == null) continue;

            return server;
        }
        return null;
    }

    public Vector<WeakReference<PlayerServer>> servers() {
        return this.servers;
    }

    public boolean contains(ServerInfo serverInfo) {
        for(BaseFamily family : Tinder.get().services().family().dump()) {
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

        for (BaseFamily family : api.services().family().dump()) {
            logger.log("---| Starting on: " + family.name());
            // Register 1000 servers into each family
            for (int i = 0; i < 1000; i++) {
                InetSocketAddress address = AddressUtil.stringToAddress("localhost:"+i);
                String name = "server"+i;

                ServerInfo info = new ServerInfo(name, address);
                PlayerServer server = new PlayerServer(info, 40, 50, 0, this.serverTimeout);
                server.setPlayerCount((int) (Math.random() * 50));

                try {
                    RegisteredServer registeredServer = api.velocityServer().registerServer(server.serverInfo());
                    server.setRegisteredServer(registeredServer);

                    family.addServer(server);

                    logger.log("-----| Added: " + server.serverInfo() + " to " + family.name());
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
    public RegisteredServer registerServer(PlayerServer server, BaseFamily family) throws Exception {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

        try {
            if(logger.loggerGate().check(GateKey.REGISTRATION_ATTEMPT))
                VelocityLang.REGISTRATION_REQUEST.send(logger, server.serverInfo(), family.name());

            if(this.contains(server.serverInfo())) throw new DuplicateRequestException("Server ["+server.serverInfo().getName()+"]("+server.serverInfo().getAddress()+":"+server.serverInfo().getAddress().getPort()+") can't be registered twice!");

            RegisteredServer registeredServer = api.registerServer(server.serverInfo());
            if(registeredServer == null) throw new NullPointerException("Unable to register the server to the proxy.");

            family.addServer(server);

            this.servers.add(new WeakReference<>(server));

            if(logger.loggerGate().check(GateKey.REGISTRATION_ATTEMPT))
                VelocityLang.REGISTERED.send(logger, server.serverInfo(), family.name());

            WebhookEventManager.fire(WebhookAlertFlag.SERVER_REGISTER, DiscordWebhookMessage.PROXY__SERVER_REGISTER.build(server, family.name()));
            WebhookEventManager.fire(WebhookAlertFlag.SERVER_REGISTER, family.name(), DiscordWebhookMessage.FAMILY__SERVER_REGISTER.build(server, family.name()));
            return registeredServer;
        } catch (Exception error) {
            if(logger.loggerGate().check(GateKey.REGISTRATION_ATTEMPT))
                VelocityLang.ERROR.send(logger, server.serverInfo(), family.name());
            throw new Exception(error.getMessage());
        }
    }

    /**
     * Unregister a server from the proxy.
     * @param serverInfo The server to be unregistered.
     * @param familyName The name of the family associated with the server.
     * @param removeFromFamily Should the server be removed from it's associated family?
     */
    public void unregisterServer(ServerInfo serverInfo, String familyName, Boolean removeFromFamily) throws Exception {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();
        try {
            PlayerServer server = this.search(serverInfo);
            if(server == null) throw new NullPointerException("Server ["+serverInfo.getName()+"]("+serverInfo.getAddress()+":"+serverInfo.getAddress().getPort()+") doesn't exist! It can't be unregistered!");

            if(logger.loggerGate().check(GateKey.UNREGISTRATION_ATTEMPT))
                VelocityLang.UNREGISTRATION_REQUEST.send(logger, serverInfo, familyName);

            BaseFamily family = server.family();

            api.unregisterServer(server.serverInfo());
            if(removeFromFamily)
                family.removeServer(server);

            if(logger.loggerGate().check(GateKey.UNREGISTRATION_ATTEMPT))
                VelocityLang.UNREGISTERED.send(logger, serverInfo, familyName);

            WebhookEventManager.fire(WebhookAlertFlag.SERVER_UNREGISTER, DiscordWebhookMessage.PROXY__SERVER_UNREGISTER.build(server));
            WebhookEventManager.fire(WebhookAlertFlag.SERVER_UNREGISTER, familyName, DiscordWebhookMessage.FAMILY__SERVER_UNREGISTER.build(server));
        } catch (NullPointerException e) {
            if(logger.loggerGate().check(GateKey.UNREGISTRATION_ATTEMPT))
                VelocityLang.ERROR.send(logger, serverInfo, familyName);
            throw new NullPointerException(e.getMessage());
        } catch (Exception e) {
            if(logger.loggerGate().check(GateKey.UNREGISTRATION_ATTEMPT))
                VelocityLang.ERROR.send(logger, serverInfo, familyName);
            throw new Exception(e);
        }
    }

    @Override
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

        public PlayerServer build() {
            this.initialTimeout = Tinder.get().services().server().serverTimeout();

            return new PlayerServer(serverInfo, softPlayerCap, hardPlayerCap, weight, initialTimeout);
        }
    }
}
