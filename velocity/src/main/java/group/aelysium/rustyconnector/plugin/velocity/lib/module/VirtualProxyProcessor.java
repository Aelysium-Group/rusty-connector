package group.aelysium.rustyconnector.plugin.velocity.lib.module;

import com.sun.jdi.request.DuplicateRequestException;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.Callable;
import group.aelysium.rustyconnector.core.lib.data_messaging.firewall.MessageTunnel;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessage;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.data_messaging.cache.MessageCache;
import group.aelysium.rustyconnector.core.lib.exception.BlockedMessageException;
import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.core.lib.model.VirtualProcessor;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.Clock;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.database.Redis;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.PaperServerLoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.managers.FamilyManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.managers.WhitelistManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.security.InvalidAlgorithmParameterException;
import java.util.HashMap;
import java.util.Map;

public class VirtualProxyProcessor implements VirtualProcessor {
    private MessageCache messageCache;
    private Redis redis;
    private String redisDataChannel;
    private final Map<ServerInfo, Boolean> lifeMatrix = new HashMap<>();
    private final FamilyManager familyManager = new FamilyManager();
    private final WhitelistManager whitelistManager = new WhitelistManager();
    private final String privateKey;
    private String rootFamily;
    private String proxyWhitelist;
    private Clock serverLifecycleHeart;
    private Clock familyServerSorting;
    private Clock tpaRequestCleaner;
    private MessageTunnel messageTunnel;

    public VirtualProxyProcessor(String privateKey) {
        this.privateKey = privateKey;
    }

    public ServerFamily<? extends PaperServerLoadBalancer> getRootFamily() {
        return this.familyManager.find(this.rootFamily);
    }

    public void setRedis(Redis redis) throws IllegalStateException {
        if(this.redis != null) throw new IllegalStateException("This has already been set! You can't set this twice!");
        this.redis = redis;
    }

    public void setRedisDataChannel(String redisDataChannel) {
        if(this.redisDataChannel != null) throw new IllegalStateException("This has already been set! You can't set this twice!");
        this.redisDataChannel = redisDataChannel;
    }
    public String getRedisDataChannel() {
        return this.redisDataChannel;
    }
    public void closeRedis() {
        this.redis.shutdown();
    }

    /**
     * Set the root family for the proxy. Once this is set it cannot be changed.
     * @param rootFamily The root family to set.
     */
    public void setRootFamily(String rootFamily) throws IllegalStateException {
        if(this.rootFamily != null) throw new IllegalStateException("This has already been set! You can't set this twice!");
        this.rootFamily = rootFamily;
    }

    /**
     * Set the message cache for the proxy. Once this is set it cannot be changed.
     * @param max The max number of messages the cache will accept.
     */
    public void setMessageCache(int max) throws IllegalStateException {
        if(this.messageCache != null) throw new IllegalStateException("This has already been set! You can't set this twice!");

        if(max <= 0) max = 0;
        if(max > 500) max = 500;
        this.messageCache = new MessageCache(max);
    }

    /**
     * Set the message tunnel for the proxy. Once this is set it cannot be changed.
     * @param messageTunnel The message tunnel to set.
     */
    private void setMessageTunnel(MessageTunnel messageTunnel) throws IllegalStateException {
        if(this.messageTunnel != null) throw new IllegalStateException("This has already been set! You can't set this twice!");
        this.messageTunnel = messageTunnel;
    }

    public MessageCache getMessageCache() {
        return this.messageCache;
    }

    /**
     * Validate a message against the message tunnel.
     * @param message The message to verify.
     * @throws BlockedMessageException If the message should be blocked.
     */
    public void validateMessage(RedisMessage message) throws BlockedMessageException {
        this.messageTunnel.validate(message);
    }

    private void startServerLifecycleHeart(long heartbeat, boolean shouldUnregister) {
        this.serverLifecycleHeart = new Clock(heartbeat);

        this.serverLifecycleHeart.start((Callable<Boolean>) () -> {
            PluginLogger logger = VelocityRustyConnector.getAPI().getLogger();

            if(logger.getGate().check(GateKey.PING))
                logger.log("Sending out pings and killing dead servers...");

            try {
                for (Map.Entry<ServerInfo, Boolean> entry : lifeMatrix.entrySet()) {
                    ServerInfo serverInfo = entry.getKey();

                    PlayerServer server = this.findServer(serverInfo);
                    if(server == null) {
                        logger.log(serverInfo.getName() + " couldn't be found! Ignoring...");
                        continue;
                    }

                    if(!entry.getValue()) {
                        if(shouldUnregister) {
                            this.unregisterServer(serverInfo, server.getFamilyName(), true);
                            if (logger.getGate().check(GateKey.PING))
                                logger.log(server.getServerInfo().getName() + " never responded to ping! Killing it...");
                        } else {
                            if (logger.getGate().check(GateKey.PING))
                                logger.log(server.getServerInfo().getName() + " never responded to ping!");
                        }
                        continue;
                    }

                    lifeMatrix.put(serverInfo,false);

                    server.ping(this.redis,privateKey);
                }
                return true;
            } catch (Exception error) {
                logger.log(error.getMessage());
            }
            return false;
        });
    }

    private void startFamilyServerSorting(long heartbeat) {
        this.familyServerSorting = new Clock(heartbeat);

        this.familyServerSorting.start((Callable<Boolean>) () -> {
            PluginLogger logger = VelocityRustyConnector.getAPI().getLogger();
            try {
                if(logger.getGate().check(GateKey.FAMILY_BALANCING))
                    logger.log("Balancing families...");

                for (ServerFamily<? extends PaperServerLoadBalancer> family : this.getFamilyManager().dump()) {
                    family.getLoadBalancer().completeSort();
                    if(logger.getGate().check(GateKey.FAMILY_BALANCING))
                        VelocityLang.FAMILY_BALANCING.send(logger, family);
                }

                if(logger.getGate().check(GateKey.FAMILY_BALANCING))
                    logger.log("Finished balancing families.");
            } catch (Exception e) {
                return false;
            }
            return true;
        });
    }

    private void startTPARequestCleaner(long heartbeat) {
        this.tpaRequestCleaner = new Clock(heartbeat);

        this.tpaRequestCleaner.start((Callable<Boolean>) () -> {
            for(ServerFamily<? extends PaperServerLoadBalancer> family : this.getFamilyManager().dump()) {
                if(!family.getTPAHandler().getSettings().isEnabled()) continue;
                family.getTPAHandler().clearExpired();
            }
            return true;
        });
    }

    /**
     * Ends the current heart lifecycles and prepares them for garbage collection.
     */
    public void killHeartbeats() {
        if(this.familyServerSorting != null) {
            this.familyServerSorting.end();
            this.familyServerSorting = null;
        }
        if(this.serverLifecycleHeart != null) {
            this.serverLifecycleHeart.end();
            this.serverLifecycleHeart = null;
        }
        if(this.tpaRequestCleaner != null) {
            this.tpaRequestCleaner.end();
            this.tpaRequestCleaner = null;
        }
    }

    /**
     * Revive a server so that it isn't killed in the next heartbeat
     * @param serverInfo The server to revive.
     */
    public void reviveServer(ServerInfo serverInfo) {
        if(this.lifeMatrix.get(serverInfo) == null) throw new NullPointerException("This server doesn't exist. Either it never registered or it has already been killed!");

        this.lifeMatrix.put(serverInfo, true);
    }

    public PlayerServer findServer(ServerInfo serverInfo) {
        for(ServerFamily<? extends PaperServerLoadBalancer> family : this.getFamilyManager().dump()) {
            PlayerServer server = family.getServer(serverInfo);
            if(server == null) continue;

            return server;
        }
        return null;
    }

    public boolean contains(ServerInfo serverInfo) {
        for(ServerFamily<? extends PaperServerLoadBalancer> family : this.getFamilyManager().dump()) {
            if(family.containsServer(serverInfo)) return true;
        }
        return false;
    }

    /**
     * Get this proxy's family manager.
     * @return The family manager.
     */
    public FamilyManager getFamilyManager() {
        return familyManager;
    }

    /**
     * Get this proxy's whitelist manager.
     * @return The whitelist manager.
     */
    public WhitelistManager getWhitelistManager() {
        return whitelistManager;
    }

    /**
     * Get the whitelist used for the whole proxy.
     * @return The whitelist, if any. Otherwise `null`.
     */
    public Whitelist getProxyWhitelist() {
        if(this.proxyWhitelist == null) return null;
        return this.whitelistManager.find(this.proxyWhitelist);
    }

    /**
     * Set the whitelist used for the whole proxy.
     * @param whitelistName Name of the whitelist.
     */
    public void setWhitelist(String whitelistName) {
        this.proxyWhitelist = whitelistName;
    }

    /**
     * Validate a private key.
     * @param key The private key that needs to be validated.
     * @return `true` if the key is valid. `false` otherwise.
     */
    public boolean validatePrivateKey(String key) {
        return this.privateKey.equals(key);
    }

    /**
     * Sends a request to all servers listening on this data channel to register themselves.
     * Can be useful if you've just restarted your proxy and need to quickly get all your servers back online.
     */
    public void registerAllServers() {
        PluginLogger logger = VelocityRustyConnector.getAPI().getLogger();

        if(logger.getGate().check(GateKey.CALL_FOR_REGISTRATION))
            VelocityLang.CALL_FOR_REGISTRATION.send(logger);

        RedisMessage message = new RedisMessage(
                this.privateKey,
                RedisMessageType.REG_ALL,
                "127.0.0.1:0"
        );

        message.dispatchMessage(this.redis);
        WebhookEventManager.fire(WebhookAlertFlag.REGISTER_ALL, DiscordWebhookMessage.PROXY__REGISTER_ALL);
    }

    /**
     * Sends a request to all servers associated with a specific family asking them to register themselves.
     * Can be usefull if you've just reloaded a family and need to quickly get all your servers back online.
     * @param familyName The name of the family to target.
     */
    public void registerAllServers(String familyName) {
        PluginLogger logger = VelocityRustyConnector.getAPI().getLogger();

        if(logger.getGate().check(GateKey.CALL_FOR_REGISTRATION))
            VelocityLang.CALL_FOR_FAMILY_REGISTRATION.send(logger, familyName);

        RedisMessage message = new RedisMessage(
                this.privateKey,
                RedisMessageType.REG_FAMILY,
                "127.0.0.1:0"
        );

        message.addParameter("family",familyName);

        message.dispatchMessage(this.redis);
        WebhookEventManager.fire(WebhookAlertFlag.REGISTER_ALL, familyName, DiscordWebhookMessage.FAMILY__REGISTER_ALL.build(familyName));
    }

    /**
     * Attempts to directly connect a player to a server and then teleport that player to another player.
     * @param source The player requesting to tpa.
     * @param target The player to tpa to.
     * @param targetServerInfo The server to send the player to.
     * @throws NullPointerException If the server doesn't exist in the family.
     */
    public void tpaSendPlayer(Player source, Player target, ServerInfo targetServerInfo) {
        VelocityAPI api = VelocityRustyConnector.getAPI();

        ServerInfo senderServerInfo = source.getCurrentServer().orElseThrow().getServerInfo();

        PlayerServer targetServer = api.getVirtualProcessor().findServer(targetServerInfo);
        if(targetServer == null) throw new NullPointerException();

        Callable<Boolean> queuePlayer = () -> {
            RedisMessage message = new RedisMessage(
                    this.privateKey,
                    RedisMessageType.TPA_QUEUE_PLAYER,
                    targetServer.getAddress()
            );
            message.addParameter("target-username",target.getUsername());
            message.addParameter("source-username",source.getUsername());

            message.dispatchMessage(this.redis);
            return true;
        };

        queuePlayer.execute();

        if(senderServerInfo.equals(targetServerInfo)) return;

        ConnectionRequestBuilder connection = source.createConnectionRequest(targetServer.getRegisteredServer());
        try {
            connection.connect().get().isSuccessful();
        } catch (Exception e) {
            source.sendMessage(VelocityLang.TPA_FAILURE.build(target.getUsername()));
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

            if(api.getVirtualProcessor().contains(server.getServerInfo())) throw new DuplicateRequestException("Server ["+server.getServerInfo().getName()+"]("+server.getServerInfo().getAddress()+":"+server.getServerInfo().getAddress().getPort()+") can't be registered twice!");

            ServerFamily<? extends PaperServerLoadBalancer> family = this.familyManager.find(familyName);
            if(family == null) throw new InvalidAlgorithmParameterException("A family with the name `"+familyName+"` doesn't exist!");

            RegisteredServer registeredServer = api.registerServer(server.getServerInfo());
            if(registeredServer == null) throw new NullPointerException("Unable to register the server to the proxy.");

            family.addServer(server);

            this.lifeMatrix.put(server.getServerInfo(),true);

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

            ServerFamily<? extends PaperServerLoadBalancer> family = server.getFamily();

            this.lifeMatrix.remove(serverInfo);
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

    /**
     * Attempt to dispatch a command as the Proxy
     * @param command The command to dispatch.
     */
    public void dispatchCommand(String command) {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        api.getServer().getCommandManager()
                .executeAsync((ConsoleCommandSource) permission -> null, command);
    }

    /**
     * Initializes the proxy based on the configuration.
     * By the time this runs, the configuration file should be able to guarantee that all values are present.
     * @param config The configuration file.
     */
    public static VirtualProxyProcessor init(DefaultConfig config) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();
        VirtualProxyProcessor virtualProxyProcessor = new VirtualProxyProcessor(config.getPrivate_key());

        // Setup families
        for (String familyName: config.getFamilies())
            virtualProxyProcessor.getFamilyManager().add(ServerFamily.init(virtualProxyProcessor, familyName));

        logger.log("Finished setting up families");

        virtualProxyProcessor.setRootFamily(config.getRoot_family());
        logger.log("Finished setting up root family");

        // Setup Redis
        Redis redis;
        if(config.getRedis_password().equals(""))
            redis = new Redis.RedisConnector()
                    .setHost(config.getRedis_host())
                    .setPort(config.getRedis_port())
                    .setUser(config.getRedis_user())
                    .build();
        else
            redis = new Redis.RedisConnector()
                    .setHost(config.getRedis_host())
                    .setPort(config.getRedis_port())
                    .setUser(config.getRedis_user())
                    .setPassword(config.getRedis_password())
                    .build();

        virtualProxyProcessor.setRedis(redis);

        redis.subscribeToChannel(config.getRedis_dataChannel());
        virtualProxyProcessor.setRedisDataChannel(config.getRedis_dataChannel());

        logger.log("Finished setting up redis");

        if(config.isHearts_serverLifecycle_enabled()) virtualProxyProcessor.startServerLifecycleHeart(config.getHearts_serverLifecycle_interval(),config.shouldHearts_serverLifecycle_unregisterOnIgnore());
        if(config.getMessageTunnel_familyServerSorting_enabled()) virtualProxyProcessor.startFamilyServerSorting(config.getMessageTunnel_familyServerSorting_interval());
        virtualProxyProcessor.startTPARequestCleaner(10);
        logger.log("Finished setting up heartbeats");

        // Setup network whitelist
        if(config.isWhitelist_enabled()) {
            virtualProxyProcessor.setWhitelist(config.getWhitelist_name());

            virtualProxyProcessor.whitelistManager.add(Whitelist.init(config.getWhitelist_name()));
        }
        logger.log("Finished setting up network whitelist");

        // Setup message tunnel
        virtualProxyProcessor.setMessageCache(config.getMessageTunnel_messageCacheSize());
        logger.log("Set message cache size to be: "+config.getMessageTunnel_messageCacheSize());

        MessageTunnel messageTunnel = new MessageTunnel(
                config.isMessageTunnel_denylist_enabled(),
                config.isMessageTunnel_whitelist_enabled(),
                config.getMessageTunnel_messageMaxLength()
        );
        virtualProxyProcessor.setMessageTunnel(messageTunnel);

        if(config.isMessageTunnel_whitelist_enabled())
            config.getMessageTunnel_whitelist_addresses().forEach(entry -> {
                String[] addressSplit = entry.split(":");

                InetSocketAddress address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));

                messageTunnel.whitelistAddress(address);
            });

        if(config.isMessageTunnel_denylist_enabled())
            config.getMessageTunnel_denylist_addresses().forEach(entry -> {
                String[] addressSplit = entry.split(":");

                InetSocketAddress address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));

                messageTunnel.blacklistAddress(address);
            });

        logger.log("Finished setting up message tunnel");

        return virtualProxyProcessor;
    }

    /**
     * Initializes the proxy based on the configuration.
     * By the time this runs, the configuration file should be able to guarantee that all values are present.
     * @param config The configuration file.
     */
    public void reload(DefaultConfig config) {
        PluginLogger logger = VelocityRustyConnector.getAPI().getLogger();
        logger.log("Reloading config.yml");

        // Heartbeats
        this.killHeartbeats();
        if(config.isHearts_serverLifecycle_enabled()) this.startServerLifecycleHeart(config.getHearts_serverLifecycle_interval(),config.shouldHearts_serverLifecycle_unregisterOnIgnore());
        if(config.getMessageTunnel_familyServerSorting_enabled()) this.startFamilyServerSorting(config.getMessageTunnel_familyServerSorting_interval());
        logger.log("Restarted heartbeats");

        this.reloadWhitelists(config);
        logger.log("Reloaded all whitelists");

        // Setup message tunnel
        this.messageCache.empty();
        this.messageCache = null;
        this.setMessageCache(config.getMessageTunnel_messageCacheSize());
        logger.log("Message cache size set to: "+config.getMessageTunnel_messageCacheSize());

        this.messageTunnel = null;
        MessageTunnel messageTunnel = new MessageTunnel(
                config.isMessageTunnel_denylist_enabled(),
                config.isMessageTunnel_whitelist_enabled(),
                config.getMessageTunnel_messageMaxLength()
        );
        this.setMessageTunnel(messageTunnel);

        if(config.isMessageTunnel_whitelist_enabled())
            config.getMessageTunnel_whitelist_addresses().forEach(entry -> {
                String[] addressSplit = entry.split(":");

                InetSocketAddress address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));

                messageTunnel.whitelistAddress(address);
            });

        if(config.isMessageTunnel_denylist_enabled())
            config.getMessageTunnel_denylist_addresses().forEach(entry -> {
                String[] addressSplit = entry.split(":");

                InetSocketAddress address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));

                messageTunnel.blacklistAddress(address);
            });
        logger.log("Message tunnel reloaded");
    }

    /**
     * Reload all whitelists currently active on the proxy.
     * By the time this runs, the configuration file should be able to guarantee that all values are present.
     * @param config The default config to read.
     */
    public void reloadWhitelists(DefaultConfig config) {
        PluginLogger logger = VelocityRustyConnector.getAPI().getLogger();

        this.proxyWhitelist = null;
        this.whitelistManager.clear();

        // Setup network whitelist
        if(config.isWhitelist_enabled()) {
            this.setWhitelist(config.getWhitelist_name());

            this.whitelistManager.add(Whitelist.init(config.getWhitelist_name()));
            logger.log("Proxy whitelist is enabled");
        } else {
            logger.log("There is no proxy whitelist");
        }

        // Reload server whitelists
        for (ServerFamily<? extends PaperServerLoadBalancer> family : this.familyManager.dump()) {
            family.reloadWhitelist();
        }
        logger.log("Reloaded all family whitelists");
    }
}
