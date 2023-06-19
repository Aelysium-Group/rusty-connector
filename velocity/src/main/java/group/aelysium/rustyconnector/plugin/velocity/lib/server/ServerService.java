package group.aelysium.rustyconnector.plugin.velocity.lib.server;

import com.sun.jdi.request.DuplicateRequestException;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisPublisher;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisService;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageFamilyRegister;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageTPAQueuePlayer;
import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.core.lib.model.Service;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.Processor;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;

import java.net.InetSocketAddress;
import java.security.InvalidAlgorithmParameterException;

public class ServerService extends Service {
    public ServerService() {
        super(true);
    }

    public PlayerServer findServer(ServerInfo serverInfo) {
        for(BaseServerFamily family : VelocityRustyConnector.getAPI().getService(FamilyService.class).dump()) {
            PlayerServer server = family.getServer(serverInfo);
            if(server == null) continue;

            return server;
        }
        return null;
    }

    public boolean contains(ServerInfo serverInfo) {
        for(BaseServerFamily family : VelocityRustyConnector.getAPI().getService(FamilyService.class).dump()) {
            if(family.containsServer(serverInfo)) return true;
        }
        return false;
    }

    /**
     * Sends a request to all servers listening on this data channel to register themselves.
     * Can be useful if you've just restarted your proxy and need to quickly get all your servers back online.
     */
    public void registerAllServers() {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        if(logger.getGate().check(GateKey.CALL_FOR_REGISTRATION))
            VelocityLang.CALL_FOR_REGISTRATION.send(logger);

        GenericRedisMessage message = new GenericRedisMessage.Builder()
                .setType(RedisMessageType.REGISTER_ALL_SERVERS_TO_PROXY)
                .setOrigin(MessageOrigin.PROXY)
                .buildSendable();

        api.getService(RedisService.class).publish(message);

        WebhookEventManager.fire(WebhookAlertFlag.REGISTER_ALL, DiscordWebhookMessage.PROXY__REGISTER_ALL);
    }

    /**
     * Sends a request to all servers associated with a specific family asking them to register themselves.
     * Can be usefull if you've just reloaded a family and need to quickly get all your servers back online.
     * @param familyName The name of the family to target.
     */
    public void registerAllServers(String familyName) {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        if(logger.getGate().check(GateKey.CALL_FOR_REGISTRATION))
            VelocityLang.CALL_FOR_FAMILY_REGISTRATION.send(logger, familyName);

        RedisMessageFamilyRegister message = (RedisMessageFamilyRegister) new GenericRedisMessage.Builder()
                .setType(RedisMessageType.REGISTER_ALL_SERVERS_TO_FAMILY)
                .setOrigin(MessageOrigin.PROXY)
                .setParameter(RedisMessageFamilyRegister.ValidParameters.FAMILY_NAME, familyName)
                .buildSendable();

        api.getService(RedisService.class).publish(message);

        WebhookEventManager.fire(WebhookAlertFlag.REGISTER_ALL, familyName, DiscordWebhookMessage.FAMILY__REGISTER_ALL.build(familyName));
    }

    /**
     * Registers fake servers into the proxy to help with testing systems.
     */
    public void registerFakeServers() {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        for (BaseServerFamily family : api.getService(FamilyService.class).dump()) {
            logger.log("---| Starting on: " + family.getName());
            // Register 1000 servers into each family
            for (int i = 0; i < 1000; i++) {
                InetSocketAddress address = AddressUtil.stringToAddress("localhost:"+i);
                String name = "server"+i;

                ServerInfo info = new ServerInfo(name, address);
                PlayerServer server = new PlayerServer(info, 40, 50, 0);
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
     * @param familyName The family to register the server into.
     * @return A RegisteredServer node.
     */
    public RegisteredServer registerServer(PlayerServer server, String familyName) throws Exception {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        try {
            if(logger.getGate().check(GateKey.REGISTRATION_REQUEST))
                VelocityLang.REGISTRATION_REQUEST.send(logger, server.getServerInfo(), familyName);

            if(this.contains(server.getServerInfo())) throw new DuplicateRequestException("Server ["+server.getServerInfo().getName()+"]("+server.getServerInfo().getAddress()+":"+server.getServerInfo().getAddress().getPort()+") can't be registered twice!");

            BaseServerFamily family = api.getService(FamilyService.class).find(familyName);
            if(family == null) throw new InvalidAlgorithmParameterException("A family with the name `"+familyName+"` doesn't exist!");

            RegisteredServer registeredServer = api.registerServer(server.getServerInfo());
            if(registeredServer == null) throw new NullPointerException("Unable to register the server to the proxy.");

            family.addServer(server);

            api.getService(ServerLifeMatrixService.class).registerServer(server.getServerInfo());

            if(logger.getGate().check(GateKey.REGISTRATION_REQUEST))
                VelocityLang.REGISTERED.send(logger, server.getServerInfo(), familyName);

            WebhookEventManager.fire(WebhookAlertFlag.SERVER_REGISTER, DiscordWebhookMessage.PROXY__SERVER_REGISTER.build(server, familyName));
            WebhookEventManager.fire(WebhookAlertFlag.SERVER_REGISTER, familyName, DiscordWebhookMessage.FAMILY__SERVER_REGISTER.build(server, familyName));
            return registeredServer;
        } catch (Exception error) {
            if(logger.getGate().check(GateKey.REGISTRATION_REQUEST))
                VelocityLang.REGISTRATION_CANCELED.send(logger, server.getServerInfo(), familyName);
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
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();
        try {
            PlayerServer server = this.findServer(serverInfo);
            if(server == null) throw new NullPointerException("Server ["+serverInfo.getName()+"]("+serverInfo.getAddress()+":"+serverInfo.getAddress().getPort()+") doesn't exist! It can't be unregistered!");

            if(logger.getGate().check(GateKey.UNREGISTRATION_REQUEST))
                VelocityLang.UNREGISTRATION_REQUEST.send(logger, serverInfo, familyName);

            BaseServerFamily family = server.getFamily();

            api.getService(ServerLifeMatrixService.class).unregisterServer(serverInfo);
            api.unregisterServer(server.getServerInfo());
            if(removeFromFamily)
                family.removeServer(server);

            if(logger.getGate().check(GateKey.UNREGISTRATION_REQUEST))
                VelocityLang.UNREGISTERED.send(logger, serverInfo, familyName);

            WebhookEventManager.fire(WebhookAlertFlag.SERVER_UNREGISTER, DiscordWebhookMessage.PROXY__SERVER_UNREGISTER.build(server));
            WebhookEventManager.fire(WebhookAlertFlag.SERVER_UNREGISTER, familyName, DiscordWebhookMessage.FAMILY__SERVER_UNREGISTER.build(server));
        } catch (Exception e) {
            if(logger.getGate().check(GateKey.UNREGISTRATION_REQUEST))
                VelocityLang.UNREGISTRATION_CANCELED.send(logger, serverInfo, familyName);
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void kill() {}
}
