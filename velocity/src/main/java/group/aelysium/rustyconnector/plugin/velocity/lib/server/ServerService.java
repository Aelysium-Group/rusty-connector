package group.aelysium.rustyconnector.plugin.velocity.lib.server;

import com.sun.jdi.request.DuplicateRequestException;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;

import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.Vector;

public class ServerService extends Service {
    private final Vector<WeakReference<PlayerServer>> servers =  new Vector<>();

    private final int serverTimeout;
    private final int serverInterval;

    protected ServerService(int serverTimeout, int serverInterval) {
        this.serverTimeout = serverTimeout;
        this.serverInterval = serverInterval;
    }

    public int getServerTimeout() {
        return this.serverTimeout;
    }

    public int getServerInterval() {
        return this.serverInterval;
    }

    public PlayerServer findServer(ServerInfo serverInfo) {
        for(BaseServerFamily family : VelocityAPI.get().services().familyService().dump()) {
            PlayerServer server = family.getServer(serverInfo);
            if(server == null) continue;

            return server;
        }
        return null;
    }

    public Vector<WeakReference<PlayerServer>> getServers() {
        return this.servers;
    }

    public boolean contains(ServerInfo serverInfo) {
        for(BaseServerFamily family : VelocityAPI.get().services().familyService().dump()) {
            if(family.containsServer(serverInfo)) return true;
        }
        return false;
    }

    /**
     * Registers fake servers into the proxy to help with testing systems.
     */
    public void registerFakeServers() {
        VelocityAPI api = VelocityAPI.get();
        PluginLogger logger = api.logger();

        for (BaseServerFamily family : api.services().familyService().dump()) {
            logger.log("---| Starting on: " + family.getName());
            // Register 1000 servers into each family
            for (int i = 0; i < 1000; i++) {
                InetSocketAddress address = AddressUtil.stringToAddress("localhost:"+i);
                String name = "server"+i;

                ServerInfo info = new ServerInfo(name, address);
                PlayerServer server = new PlayerServer(info, 40, 50, 0, this.serverTimeout);
                server.setPlayerCount((int) (Math.random() * 50));

                try {
                    RegisteredServer registeredServer = api.getServer().registerServer(server.getServerInfo());
                    server.setRegisteredServer(registeredServer);

                    family.addServer(server);

                    logger.log("-----| Added: " + server.getServerInfo() + " to " + family.getName());
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
    public RegisteredServer registerServer(PlayerServer server, BaseServerFamily family) throws Exception {
        VelocityAPI api = VelocityAPI.get();
        PluginLogger logger = api.logger();

        try {
            if(logger.getGate().check(GateKey.REGISTRATION_ATTEMPT))
                VelocityLang.REGISTRATION_REQUEST.send(logger, server.getServerInfo(), family.getName());

            if(this.contains(server.getServerInfo())) throw new DuplicateRequestException("Server ["+server.getServerInfo().getName()+"]("+server.getServerInfo().getAddress()+":"+server.getServerInfo().getAddress().getPort()+") can't be registered twice!");

            RegisteredServer registeredServer = api.registerServer(server.getServerInfo());
            if(registeredServer == null) throw new NullPointerException("Unable to register the server to the proxy.");

            family.addServer(server);

            this.servers.add(new WeakReference<>(server));

            if(logger.getGate().check(GateKey.REGISTRATION_ATTEMPT))
                VelocityLang.REGISTERED.send(logger, server.getServerInfo(), family.getName());

            WebhookEventManager.fire(WebhookAlertFlag.SERVER_REGISTER, DiscordWebhookMessage.PROXY__SERVER_REGISTER.build(server, family.getName()));
            WebhookEventManager.fire(WebhookAlertFlag.SERVER_REGISTER, family.getName(), DiscordWebhookMessage.FAMILY__SERVER_REGISTER.build(server, family.getName()));
            return registeredServer;
        } catch (Exception error) {
            if(logger.getGate().check(GateKey.REGISTRATION_ATTEMPT))
                VelocityLang.REGISTRATION_CANCELED.send(logger, server.getServerInfo(), family.getName());
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
        VelocityAPI api = VelocityAPI.get();
        PluginLogger logger = api.logger();
        try {
            PlayerServer server = this.findServer(serverInfo);
            if(server == null) throw new NullPointerException("Server ["+serverInfo.getName()+"]("+serverInfo.getAddress()+":"+serverInfo.getAddress().getPort()+") doesn't exist! It can't be unregistered!");

            if(logger.getGate().check(GateKey.UNREGISTRATION_ATTEMPT))
                VelocityLang.UNREGISTRATION_REQUEST.send(logger, serverInfo, familyName);

            BaseServerFamily family = server.getFamily();

            api.unregisterServer(server.getServerInfo());
            if(removeFromFamily)
                family.removeServer(server);

            if(logger.getGate().check(GateKey.UNREGISTRATION_ATTEMPT))
                VelocityLang.UNREGISTERED.send(logger, serverInfo, familyName);

            WebhookEventManager.fire(WebhookAlertFlag.SERVER_UNREGISTER, DiscordWebhookMessage.PROXY__SERVER_UNREGISTER.build(server));
            WebhookEventManager.fire(WebhookAlertFlag.SERVER_UNREGISTER, familyName, DiscordWebhookMessage.FAMILY__SERVER_UNREGISTER.build(server));
        } catch (NullPointerException e) {
            if(logger.getGate().check(GateKey.UNREGISTRATION_ATTEMPT))
                VelocityLang.UNREGISTRATION_CANCELED.send(logger, serverInfo, familyName);
            throw new NullPointerException(e.getMessage());
        } catch (Exception e) {
            if(logger.getGate().check(GateKey.UNREGISTRATION_ATTEMPT))
                VelocityLang.UNREGISTRATION_CANCELED.send(logger, serverInfo, familyName);
            throw new Exception(e);
        }
    }

    @Override
    public void kill() {

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
            this.initialTimeout = VelocityAPI.get().services().serverService().getServerTimeout();

            return new PlayerServer(serverInfo, softPlayerCap, hardPlayerCap, weight, initialTimeout);
        }
    }
}
