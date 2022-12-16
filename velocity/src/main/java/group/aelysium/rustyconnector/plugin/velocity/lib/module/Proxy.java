package group.aelysium.rustyconnector.plugin.velocity.lib.module;

import com.sun.jdi.request.DuplicateRequestException;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.Callable;
import group.aelysium.rustyconnector.core.lib.data_messaging.firewall.MessageTunnel;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessage;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.data_messaging.cache.MessageCache;
import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.Clock;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.database.Redis;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.PaperServerLoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.managers.FamilyManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.managers.PlayerManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.managers.WhitelistManager;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.security.InvalidAlgorithmParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Proxy {
    private MessageCache messageCache;
    private Redis redis;
    private final Map<ServerInfo, Boolean> lifeMatrix = new HashMap<>();
    private final FamilyManager familyManager;
    private final PlayerManager playerManager;
    private final WhitelistManager whitelistManager;
    private final String privateKey;
    private String rootFamily;
    private String proxyWhitelist;
    private Clock serverLifecycleHeart;
    private Clock familyServerSorting;
    private MessageTunnel messageTunnel;

    public ServerFamily<? extends PaperServerLoadBalancer> getRootFamily() {
        return this.familyManager.find(this.rootFamily);
    }

    public void setRedis(Redis redis) throws IllegalStateException {
        if(this.redis != null) throw new IllegalStateException("This has already been set! You can't set this twice!");
        this.redis = redis;
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

    private Proxy(String privateKey) {
        this.privateKey = privateKey;

        this.familyManager = new FamilyManager();
        this.playerManager = new PlayerManager();
        this.whitelistManager = new WhitelistManager();
    }

    public MessageCache getMessageCache() {
        return this.messageCache;
    }

    /**
     * Validate a message against the message tunnel.
     * If no message tunnel has been defined this will default to `true`
     * @param message The message to verify.
     * @return `true` if the message is valid. `false` otherwise.
     */
    public boolean validateMessage(RedisMessage message) {
        return this.messageTunnel.validate(message);
    }

    public void startServerLifecycleHeart(long heartbeat, boolean shouldUnregister) {
        this.serverLifecycleHeart = new Clock(heartbeat);

        serverLifecycleHeart.start((Callable<Boolean>) () -> {
            VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

            if(plugin.logger().getGate().check(GateKey.PING))
                plugin.logger().log("Sending out pings and killing dead servers...");

            try {
                for (Map.Entry<ServerInfo, Boolean> entry : lifeMatrix.entrySet()) {
                    ServerInfo serverInfo = entry.getKey();

                    PaperServer server = this.findServer(serverInfo);
                    if(server == null) {
                        plugin.logger().log(serverInfo.getName() + " couldn't be found! Ignoring...");
                        continue;
                    }

                    if(!entry.getValue()) {
                        if(shouldUnregister) {
                            this.unregisterServer(serverInfo, server.getFamilyName());
                            if (plugin.logger().getGate().check(GateKey.PING))
                                plugin.logger().log(server.getServerInfo().getName() + " never responded to ping! Killing it...");
                        } else {
                            if (plugin.logger().getGate().check(GateKey.PING))
                                plugin.logger().log(server.getServerInfo().getName() + " never responded to ping!");
                        }
                        continue;
                    }

                    lifeMatrix.put(serverInfo,false);

                    server.ping(this.redis,privateKey);
                }
                return true;
            } catch (Exception error) {
                plugin.logger().log(error.getMessage());
            }
            return false;
        });
    }

    public void startFamilyServerSorting(long heartbeat) {
        this.familyServerSorting = new Clock(heartbeat);

        familyServerSorting.start((Callable<Boolean>) () -> {
            VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();
            try {
                if(plugin.logger().getGate().check(GateKey.FAMILY_BALANCING))
                    plugin.logger().log("Balancing families...");

                for (ServerFamily<? extends PaperServerLoadBalancer> family : plugin.getProxy().getFamilyManager().dump()) {
                    family.getLoadBalancer().completeSort();
                    if(plugin.logger().getGate().check(GateKey.FAMILY_BALANCING))
                        VelocityLang.FAMILY_BALANCING.send(plugin.logger(), family);
                }

                if(plugin.logger().getGate().check(GateKey.FAMILY_BALANCING))
                    plugin.logger().log("Finished balancing families.");
            } catch (Exception e) {
                return false;
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
    }

    public void killRedis() {
        this.redis.disconnect();
    }

    /**
     * Revive a server so that it isn't killed in the next heartbeat
     * @param serverInfo The server to revive.
     */
    public void reviveServer(ServerInfo serverInfo) {
        if(this.lifeMatrix.get(serverInfo) == null) throw new NullPointerException("This server doesn't exist. Either it never registered or it has already been killed!");

        this.lifeMatrix.put(serverInfo, true);
    }

    public PaperServer findServer(ServerInfo serverInfo) {
        for(ServerFamily<? extends PaperServerLoadBalancer> family : this.getFamilyManager().dump()) {
            PaperServer server = family.getServer(serverInfo);
            if(server == null) continue;

            return server;
        }
        return null;
    }

    public boolean contains(String serverName) {
        for(ServerFamily<? extends PaperServerLoadBalancer> family : this.getFamilyManager().dump()) {
            if(family.containsServer(serverName)) return true;
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
     * Get this proxy's player manager.
     * @return The player manager.
     */
    public PlayerManager getPlayerManager() {
        return playerManager;
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
     * Can be usefull if you've just restarted your proxy and need to quickly get all your servers back online.
     */
    public void registerAllServers() {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        if(VelocityRustyConnector.getInstance().logger().getGate().check(GateKey.CALL_FOR_REGISTRATION))
            VelocityLang.CALL_FOR_REGISTRATION.send(plugin.logger());

        RedisMessage message = new RedisMessage(
                this.privateKey,
                RedisMessageType.REG_ALL,
                "127.0.0.1:0"
        );

        message.dispatchMessage(this.redis);
    }

    /**
     * Sends a request to all servers associated with a specific family asking them to register themselves.
     * Can be usefull if you've just reloaded a family and need to quickly get all your servers back online.
     * @param familyName The name of the family to target.
     */
    public void registerAllServers(String familyName) {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        if(VelocityRustyConnector.getInstance().logger().getGate().check(GateKey.CALL_FOR_REGISTRATION))
            VelocityLang.CALL_FOR_FAMILY_REGISTRATION.send(plugin.logger(), familyName);

        RedisMessage message = new RedisMessage(
                this.privateKey,
                RedisMessageType.REG_ALL,
                "127.0.0.1:0"
        );

        message.addParameter("family",familyName);

        message.dispatchMessage(this.redis);
    }

    /**
     * Register a server to the proxy.
     * @param server The server to be registered.
     * @param familyName The family to register the server into.
     * @return A RegisteredServer node.
     */
    public RegisteredServer registerServer(PaperServer server, String familyName) throws Exception {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();
        try {
            if(VelocityRustyConnector.getInstance().logger().getGate().check(GateKey.REGISTRATION_REQUEST))
                VelocityLang.REGISTRATION_REQUEST.send(plugin.logger(), server, familyName);

            if(plugin.getProxy().contains(server.getServerInfo().getName())) throw new DuplicateRequestException("Server ["+server.getServerInfo().getName()+"]("+server.getServerInfo().getAddress()+":"+server.getServerInfo().getAddress().getPort()+") can't be registered twice!");

            ServerFamily<? extends PaperServerLoadBalancer> family = this.familyManager.find(familyName);
            if(family == null) throw new InvalidAlgorithmParameterException("A family with the name `"+familyName+"` doesn't exist!");

            RegisteredServer registeredServer = plugin.getVelocityServer().registerServer(server.getServerInfo());
            if(registeredServer == null) throw new NullPointerException("Unable to register the server to the proxy.");

            family.addServer(server);

            this.lifeMatrix.put(server.getServerInfo(),true);

            if(VelocityRustyConnector.getInstance().logger().getGate().check(GateKey.REGISTRATION_REQUEST))
                VelocityLang.REGISTERED.send(plugin.logger(), server, familyName);

            return registeredServer;
        } catch (Exception error) {
            if(plugin.logger().getGate().check(GateKey.REGISTRATION_REQUEST))
                VelocityLang.REGISTRATION_CANCELED.send(plugin.logger(), server, familyName);
            throw new Exception(error.getMessage());
        }
    }

    /**
     * Unregister a server from the proxy.
     * @param serverInfo The server to be unregistered.
     */
    public void unregisterServer(ServerInfo serverInfo, String familyName) throws Exception {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();
        PaperServer server = this.findServer(serverInfo);
        try {
            if(server == null) throw new NullPointerException("Server ["+serverInfo.getName()+"]("+serverInfo.getAddress()+":"+serverInfo.getAddress().getPort()+") doesn't exist! It can't be unregistered!");

            if(plugin.logger().getGate().check(GateKey.UNREGISTRATION_REQUEST))
                VelocityLang.UNREGISTRATION_REQUEST.send(plugin.logger(), server, familyName);

            ServerFamily<? extends PaperServerLoadBalancer> family = server.getFamily();
            family.removeServer(server);

            plugin.getVelocityServer().unregisterServer(server.getServerInfo());

            this.lifeMatrix.remove(serverInfo);

            if(plugin.logger().getGate().check(GateKey.UNREGISTRATION_REQUEST))
                VelocityLang.UNREGISTERED.send(plugin.logger(), server, familyName);
        } catch (Exception e) {
            if(plugin.logger().getGate().check(GateKey.UNREGISTRATION_REQUEST)) {
                assert server != null;
                VelocityLang.REGISTRATION_CANCELED.send(plugin.logger(), server, familyName);
            }
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Attempt to dispatch a command as the Proxy
     * @param command The command to dispatch.
     */
    public void dispatchCommand(String command) {
        VelocityRustyConnector.getInstance().getVelocityServer().getCommandManager()
                .executeAsync((ConsoleCommandSource) permission -> null, command);
    }

    /**
     * Initializes the proxy based on the configuration.
     * @param config The configuration file.
     */
    public static Proxy init(DefaultConfig config) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        Proxy proxy = new Proxy(config.getPrivate_key());

        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        // Setup families
        for (String familyName: config.getFamilies())
            proxy.getFamilyManager().add(ServerFamily.init(proxy, familyName));

        plugin.logger().log("Finished setting up families");

        proxy.setRootFamily(config.getRoot_family());
        plugin.logger().log("Finished setting up root family");

        // Setup Redis
        Redis redis = new Redis();
        redis.setConnection(
                config.getRedis_host(),
                config.getRedis_port(),
                config.getRedis_password(),
                config.getRedis_dataChannel()
        );
        redis.connect(plugin);

        proxy.setRedis(redis);
        plugin.logger().log("Finished setting up redis");

        if(config.isHearts_serverLifecycle_enabled()) proxy.startServerLifecycleHeart(config.getHearts_serverLifecycle_interval(),config.shouldHearts_serverLifecycle_unregisterOnIgnore());
        if(config.getMessageTunnel_familyServerSorting_enabled()) proxy.startFamilyServerSorting(config.getMessageTunnel_familyServerSorting_interval());
        plugin.logger().log("Finished setting up heartbeats");

        // Setup network whitelist
        if(config.isWhitelist_enabled()) {
            proxy.setWhitelist(config.getWhitelist_name());

            proxy.whitelistManager.add(Whitelist.init(config.getWhitelist_name()));
        }
        plugin.logger().log("Finished setting up network whitelist");

        // Setup message tunnel
        proxy.setMessageCache(config.getMessageTunnel_messageCacheSize());
        plugin.logger().log("Set message cache size to be: "+config.getMessageTunnel_messageCacheSize());

        MessageTunnel messageTunnel = new MessageTunnel(
                config.isMessageTunnel_denylist_enabled(),
                config.isMessageTunnel_whitelist_enabled(),
                config.getMessageTunnel_messageMaxLength()
        );
        proxy.setMessageTunnel(messageTunnel);

        if(config.isMessageTunnel_whitelist_enabled() || config.isMessageTunnel_denylist_enabled()) {

            List<String> whitelist = config.getMessageTunnel_whitelist_addresses();
            whitelist.forEach(entry -> {
                String[] addressSplit = entry.split(":");

                InetSocketAddress address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));

                messageTunnel.whitelistAddress(address);
            });

            List<String> blacklist = config.getMessageTunnel_denylist_addresses();
            blacklist.forEach(entry -> {
                String[] addressSplit = entry.split(":");

                InetSocketAddress address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));

                messageTunnel.blacklistAddress(address);
            });
        }
        plugin.logger().log("Finished setting up message tunnel");

        return proxy;
    }

    /**
     * Initializes the proxy based on the configuration.
     * @param config The configuration file.
     */
    public void reload(DefaultConfig config) {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();
        plugin.logger().log("Reloading config.yml");

        // Heartbeats
        this.killHeartbeats();
        if(config.isHearts_serverLifecycle_enabled()) this.startServerLifecycleHeart(config.getHearts_serverLifecycle_interval(),config.shouldHearts_serverLifecycle_unregisterOnIgnore());
        if(config.getMessageTunnel_familyServerSorting_enabled()) this.startFamilyServerSorting(config.getMessageTunnel_familyServerSorting_interval());
        plugin.logger().log("Restarted heartbeats");

        this.reloadWhitelists(config);
        plugin.logger().log("Reloaded all whitelists");

        // Setup message tunnel
        this.messageCache.empty();
        this.messageCache = null;
        this.setMessageCache(config.getMessageTunnel_messageCacheSize());
        plugin.logger().log("Message cache size set to: "+config.getMessageTunnel_messageCacheSize());

        this.messageTunnel = null;
        MessageTunnel messageTunnel = new MessageTunnel(
                config.isMessageTunnel_denylist_enabled(),
                config.isMessageTunnel_whitelist_enabled(),
                config.getMessageTunnel_messageMaxLength()
        );
        this.setMessageTunnel(messageTunnel);

        if(config.isMessageTunnel_whitelist_enabled() || config.isMessageTunnel_denylist_enabled()) {

            List<String> whitelist = config.getMessageTunnel_whitelist_addresses();
            whitelist.forEach(entry -> {
                String[] addressSplit = entry.split(":");

                InetSocketAddress address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));

                messageTunnel.whitelistAddress(address);
            });

            List<String> blacklist = config.getMessageTunnel_denylist_addresses();
            blacklist.forEach(entry -> {
                String[] addressSplit = entry.split(":");

                InetSocketAddress address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));

                messageTunnel.blacklistAddress(address);
            });
        }
        plugin.logger().log("Message tunnel reloaded");
    }

    /**
     * Reload all whitelists currently active on the proxy.
     * @param config The default config to read.
     */
    public void reloadWhitelists(DefaultConfig config) {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        this.proxyWhitelist = null;
        this.whitelistManager.clear();

        // Setup network whitelist
        if(config.isWhitelist_enabled()) {
            this.setWhitelist(config.getWhitelist_name());

            this.whitelistManager.add(Whitelist.init(config.getWhitelist_name()));
            plugin.logger().log("Proxy whitelist is enabled");
        } else {
            plugin.logger().log("There is no proxy whitelist");
        }

        // Reload server whitelists
        for (ServerFamily<? extends PaperServerLoadBalancer> family : this.familyManager.dump()) {
            family.reloadWhitelist();
        }
        plugin.logger().log("Reloaded all family whitelists");
    }
}
