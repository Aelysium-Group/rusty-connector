package group.aelysium.rustyconnector.plugin.velocity.lib.processor;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import com.sun.jdi.request.DuplicateRequestException;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.Callable;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisClient;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisPublisher;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisService;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.firewall.MessageTunnel;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.cache.MessageCache;
import group.aelysium.rustyconnector.core.lib.database.MySQL;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageFamilyRegister;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageTPAQueuePlayer;
import group.aelysium.rustyconnector.core.lib.exception.BlockedMessageException;
import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.core.lib.model.VirtualProcessor;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.config.PrivateKeyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.Clock;
import group.aelysium.rustyconnector.plugin.velocity.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.database.HomeServerMappingsDatabase;
import group.aelysium.rustyconnector.plugin.velocity.lib.database.RedisSubscriber;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ScalarServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.StaticServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.managers.FamilyManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.managers.WhitelistManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.Whitelist;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.security.InvalidAlgorithmParameterException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirtualProxyProcessor implements VirtualProcessor {
    private MessageCache messageCache;
    private RedisService redisService;
    private final Map<ServerInfo, Boolean> lifeMatrix = new HashMap<>();
    private final FamilyManager familyManager = new FamilyManager();
    private final WhitelistManager whitelistManager = new WhitelistManager();
    private String rootFamily;
    private String proxyWhitelist;
    private Clock serverLifecycleHeart;
    private LoadBalancingService loadBalancingService = null;
    private Clock tpaRequestCleaner;
    private MessageTunnel messageTunnel;
    public boolean catchDisconnectingPlayers = false;

    public ScalarServerFamily getRootFamily() {
        return (ScalarServerFamily) this.familyManager.find(this.rootFamily);
    }
    public RedisService getRedisService() {
        return this.redisService;
    }

    protected void setRedisService(RedisService redis) throws IllegalStateException {
        if(this.redisService != null) throw new IllegalStateException("This has already been set! You can't set this twice!");
        this.redisService = redis;
    }

    public void closeRedis() {
        this.redisService.kill();
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
    public void validateMessage(GenericRedisMessage message) throws BlockedMessageException {
        this.messageTunnel.validate(message);
    }

    /**
     * Remove any home server mappings which have been cached for a specific player.
     * @param player The player to uncache mappings for.
     */
    public void uncacheHomeServerMappings(Player player) {
        List<BaseServerFamily> familyList = this.getFamilyManager().dump().stream().filter(family -> family instanceof StaticServerFamily).toList();
        if(familyList.size() == 0) return;

        for (BaseServerFamily family : familyList) {
            ((StaticServerFamily) family).uncacheHomeServer(player);
        }
    }

    private void startServerLifecycleHeart(long heartbeat, boolean shouldUnregister) {
        Callable<Boolean> callable = () -> {
            VelocityAPI api = VelocityRustyConnector.getAPI();
            PluginLogger logger = api.getLogger();

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

                    server.ping();
                }
            } catch (Exception e) {
                logger.log(e.getMessage());
            }
            try {
                api.getVirtualProcessor().getFamilyManager().dump().forEach(family -> {
                    if(!(family instanceof StaticServerFamily)) return;

                    try {
                        ((StaticServerFamily) family).purgeExpiredMappings();
                    } catch (Exception e) {
                        VelocityLang.BOXED_MESSAGE_COLORED.send(logger, Component.text("There was an issue while purging expired mappings for: "+family.getName()+". "+e.getMessage()),NamedTextColor.RED);
                    }
                });
            } catch (Exception e) {
                logger.log(e.getMessage());
            }
            return true;
        };

        this.serverLifecycleHeart = new Clock(callable, heartbeat);
        this.serverLifecycleHeart.start();
    }

    private void startTPARequestCleaner(long heartbeat) {
        Callable<Boolean> callable = () -> {
            for(BaseServerFamily family : this.getFamilyManager().dump()) {
                if(!family.getTPAHandler().getSettings().isEnabled()) continue;
                family.getTPAHandler().clearExpired();
            }
            return true;
        };

        this.tpaRequestCleaner = new Clock(callable, heartbeat);
        this.tpaRequestCleaner.start();
    }

    /**
     * Ends the current heart lifecycles and prepares them for garbage collection.
     */
    public void killServices() {
        if(this.loadBalancingService != null) {
            this.loadBalancingService.kill();
            this.loadBalancingService = null;
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
     * Ends the current heart lifecycles and prepares them for garbage collection.
     */
    public void startServices() {
        PluginLogger logger = VelocityRustyConnector.getAPI().getLogger();
        logger.log("Starting services!");

        if(this.loadBalancingService != null) {
            logger.log("Starting Load Balancing service...");
            this.loadBalancingService.init();
            logger.log("Finished starting Load Balancing service!");
        } else
            logger.send(Component.text("The Load Balancing service is set as disabled! All families will effectively operate in ROUND_ROBIN mode.", NamedTextColor.YELLOW));
        logger.log("Starting Redis Service...");
        this.getRedisService().start(RedisSubscriber.class);
        logger.log("Finished starting Redis service!");
        logger.log("Finished starting TPA Request Cleaning service!");
        this.tpaRequestCleaner.start();
        logger.log("Finished starting TPA Request Cleaning service!");

        logger.log("Finished starting services!");
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
        for(BaseServerFamily family : this.getFamilyManager().dump()) {
            PlayerServer server = family.getServer(serverInfo);
            if(server == null) continue;

            return server;
        }
        return null;
    }

    public boolean contains(ServerInfo serverInfo) {
        for(BaseServerFamily family : this.getFamilyManager().dump()) {
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
     * Sends a request to all servers listening on this data channel to register themselves.
     * Can be useful if you've just restarted your proxy and need to quickly get all your servers back online.
     */
    public void registerAllServers() {
        PluginLogger logger = VelocityRustyConnector.getAPI().getLogger();

        if(logger.getGate().check(GateKey.CALL_FOR_REGISTRATION))
            VelocityLang.CALL_FOR_REGISTRATION.send(logger);

        GenericRedisMessage message = new GenericRedisMessage.Builder()
                .setType(RedisMessageType.REG_ALL)
                .setOrigin(MessageOrigin.PROXY)
                .buildSendable();

        this.getRedisService().publish(message);

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

        RedisMessageFamilyRegister message = (RedisMessageFamilyRegister) new GenericRedisMessage.Builder()
                .setType(RedisMessageType.REG_FAMILY)
                .setOrigin(MessageOrigin.PROXY)
                .setParameter(RedisMessageFamilyRegister.ValidParameters.FAMILY_NAME, familyName)
                .buildSendable();


        this.getRedisService().publish(message);

        WebhookEventManager.fire(WebhookAlertFlag.REGISTER_ALL, familyName, DiscordWebhookMessage.FAMILY__REGISTER_ALL.build(familyName));
    }

    /**
     * Registers fake servers into the proxy to help with testing systems.
     */
    public void registerFakeServers() {
        PluginLogger logger = VelocityRustyConnector.getAPI().getLogger();
        for (BaseServerFamily family : this.getFamilyManager().dump()) {
            logger.log("---| Starting on: " + family.getName());
            // Register 1000 servers into each family
            for (int i = 0; i < 1000; i++) {
                InetSocketAddress address = AddressUtil.stringToAddress("localhost:"+i);
                String name = "server"+i;

                ServerInfo info = new ServerInfo(name, address);
                PlayerServer server = new PlayerServer(info, 40, 50, 0);
                server.setPlayerCount((int) (Math.random() * 50));

                try {
                    VelocityAPI api = VelocityRustyConnector.getAPI();
                    RegisteredServer registeredServer = api.getServer().registerServer(server.getServerInfo());
                    server.setRegisteredServer(registeredServer);

                    family.addServer(server);

                    logger.log("-----| Added: " + server.getServerInfo() + " to " + family.getName());
                } catch (Exception ignore) {}
            }
        }
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


        RedisMessageTPAQueuePlayer message = (RedisMessageTPAQueuePlayer) new GenericRedisMessage.Builder()
                .setType(RedisMessageType.TPA_QUEUE_PLAYER)
                .setOrigin(MessageOrigin.PROXY)
                .setParameter(RedisMessageTPAQueuePlayer.ValidParameters.TARGET_SERVER, targetServer.getAddress())
                .setParameter(RedisMessageTPAQueuePlayer.ValidParameters.TARGET_USERNAME, target.getUsername())
                .setParameter(RedisMessageTPAQueuePlayer.ValidParameters.SOURCE_USERNAME, source.getUsername())
                .buildSendable();

        this.getRedisService().publish(message);


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

            BaseServerFamily family = this.familyManager.find(familyName);
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

            BaseServerFamily family = server.getFamily();

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
    public static VirtualProxyProcessor init(DefaultConfig config) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException, SQLException {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();
        VirtualProxyProcessor virtualProxyProcessor = new VirtualProxyProcessor();

        // Setup private key
        PrivateKeyConfig privateKeyConfig = PrivateKeyConfig.newConfig(new File(String.valueOf(api.getDataFolder()), "private.key"));
        if(!privateKeyConfig.generate())
            throw new IllegalStateException("Unable to load or create private.key!");
        char[] privateKey = null;
        try {
            privateKey = privateKeyConfig.get();
        } catch (Exception ignore) {}
        if(privateKey == null) throw new IllegalAccessException("There was a fatal error while reading private.key!");

        logger.log("Should the root family catch players that are disconnected from sub-servers: "+ config.shouldRootFamilyCatchDisconnectingPlayers());
        virtualProxyProcessor.catchDisconnectingPlayers = config.shouldRootFamilyCatchDisconnectingPlayers();

        // Setup Redis
        RedisClient.Builder redisClientBuilder = new RedisClient.Builder()
                .setHost(config.getRedis_host())
                .setPort(config.getRedis_port())
                .setUser(config.getRedis_user())
                .setDataChannel(config.getRedis_dataChannel());

        if(!config.getRedis_password().equals(""))
            redisClientBuilder.setPassword(config.getRedis_password());

        virtualProxyProcessor.setRedisService(new RedisService(redisClientBuilder, privateKey));

        logger.log("Finished setting up redis");

        // Setup MySQL
        try {
            if (config.shouldIgnoreMysql())
                logger.send(Component.text("No use for MySQL has been found. Ignoring MySQL configurations.", NamedTextColor.YELLOW));
            else {
                MySQL mySQL = new MySQL.MySQLBuilder()
                        .setHost(config.getMysql_host())
                        .setPort(config.getMysql_port())
                        .setDatabase(config.getMysql_database())
                        .setUser(config.getMysql_user())
                        .setPassword(config.getMysql_password())
                        .build();
                api.setMySQL(mySQL);

                HomeServerMappingsDatabase.init();
                logger.log("Finished setting up MySQL");

            }
        } catch (CommunicationsException e) {
            throw new IllegalAccessException("Unable to connect to MySQL! Is the server available?");
        }

        // Setup families
        for (String familyName: config.getScalarFamilies())
            virtualProxyProcessor.getFamilyManager().add(ScalarServerFamily.init(virtualProxyProcessor, familyName));
        for (String familyName: config.getStaticFamilies())
            virtualProxyProcessor.getFamilyManager().add(StaticServerFamily.init(virtualProxyProcessor, familyName));

        logger.log("Setting up root family");

        virtualProxyProcessor.getFamilyManager().add(ScalarServerFamily.init(virtualProxyProcessor, config.getRootFamilyName()));

        logger.log("Finished setting up families");

        virtualProxyProcessor.setRootFamily(config.getRootFamilyName());
        logger.log("Finished setting up root family");

        if(config.isHearts_serverLifecycle_enabled()) virtualProxyProcessor.startServerLifecycleHeart(config.getServices_serverLifecycle_interval(),config.shouldHearts_serverLifecycle_unregisterOnIgnore());

        if(config.getMessageTunnel_familyServerSorting_enabled()) {
            virtualProxyProcessor.loadBalancingService = new LoadBalancingService(virtualProxyProcessor.getFamilyManager().size(), config.getMessageTunnel_familyServerSorting_interval());
        }

        virtualProxyProcessor.startTPARequestCleaner(10);
        logger.log("Finished loading services...");

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
        this.killServices();
        if(config.isHearts_serverLifecycle_enabled()) this.startServerLifecycleHeart(config.getServices_serverLifecycle_interval(),config.shouldHearts_serverLifecycle_unregisterOnIgnore());
        if(config.getMessageTunnel_familyServerSorting_enabled()) this.loadBalancingService = new LoadBalancingService(this.getFamilyManager().size(), config.getMessageTunnel_familyServerSorting_interval());
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
        for (BaseServerFamily family : this.familyManager.dump()) {
            family.reloadWhitelist();
        }
        logger.log("Reloaded all family whitelists");
    }
}
