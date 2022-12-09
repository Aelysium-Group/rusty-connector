package group.aelysium.rustyconnector.plugin.velocity.lib.module;

import com.sun.jdi.request.DuplicateRequestException;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.Callable;
import group.aelysium.rustyconnector.core.lib.message.firewall.MessageTunnel;
import group.aelysium.rustyconnector.core.lib.message.RedisMessage;
import group.aelysium.rustyconnector.core.lib.message.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.message.cache.MessageCache;
import group.aelysium.rustyconnector.core.lib.util.logger.GateKey;
import group.aelysium.rustyconnector.core.lib.util.logger.Lang;
import group.aelysium.rustyconnector.core.lib.util.logger.LangKey;
import group.aelysium.rustyconnector.core.lib.util.logger.LangMessage;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.Clock;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.database.Redis;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.PaperServerLoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.managers.FamilyManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.managers.PlayerManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.managers.WhitelistManager;

import java.net.InetSocketAddress;
import java.security.InvalidAlgorithmParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Proxy {
    private Redis redis;
    private final Map<ServerInfo, Boolean> lifeMatrix = new HashMap<>();
    private final FamilyManager familyManager;
    private final PlayerManager playerManager;
    private final WhitelistManager whitelistManager;
    private final String privateKey;
    private String rootFamily;
    private String proxyWhitelist;
    private Clock heart;

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

    private Proxy(String privateKey) {
        this.privateKey = privateKey;

        this.familyManager = new FamilyManager();
        this.playerManager = new PlayerManager();
        this.whitelistManager = new WhitelistManager();
    }

    public MessageCache getMessageCache() {
        return this.redis.getMessageCache();
    }

    public void startHeart(long heartbeat) {
        this.heart = new Clock(heartbeat);

        heart.start((Callable<Boolean>) () -> {
            VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

            if(plugin.logger().getGate().check(GateKey.PING))
                (new LangMessage(plugin.logger()))
                        .insert(Lang.border())
                        .insert("Sending out pings and killing dead servers...")
                        .print();

            try {
                for (Map.Entry<ServerInfo, Boolean> entry : lifeMatrix.entrySet()) {
                    ServerInfo serverInfo = entry.getKey();

                    PaperServer server = this.findServer(serverInfo);
                    if(server == null) throw new NullPointerException(serverInfo.getName() + " couldn't be found!");

                    if(!entry.getValue()) {
                        //this.unregisterServer(serverInfo, server.getFamilyName());
                        if(plugin.logger().getGate().check(GateKey.PING))
                            (new LangMessage(plugin.logger()))
                                    .insert(Lang.border())
                                    .insert(server.getServerInfo().getName()+" never responded to ping! It should be killed!")
                                    .print();
                        continue;
                    }

                    lifeMatrix.put(serverInfo,false);

                    server.ping(this.redis,privateKey);
                }
                return true;
            } catch (Exception error) {
                (new LangMessage(plugin.logger()))
                        .insert("There was an issue with the Proxy heartbeat!")
                        .insert(error.getMessage())
                        .print();
            }
            return false;
        });
    }
    public void killHeartbeat() {
        this.heart.end();
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
        if(VelocityRustyConnector.getInstance().logger().getGate().check(GateKey.CALL_FOR_REGISTRATION))
            VelocityRustyConnector.getInstance().logger().log("[Velocity](127.0.0.1) "+Lang.get(LangKey.ICON_CALL_FOR_REGISTRATION) +" EVERYONE");

        RedisMessage message = new RedisMessage(
                this.privateKey,
                RedisMessageType.REG_ALL,
                "127.0.0.1:0",
                false
        );

        message.dispatchMessage(this.redis);
    }

    /**
     * Register a server to the proxy.
     * @param server The server to be registered.
     * @param familyName The family to register the server into.
     * @return A RegisteredServer node.
     */
    public RegisteredServer registerServer(PaperServer server, String familyName) throws DuplicateRequestException {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();
        try {
            if(VelocityRustyConnector.getInstance().logger().getGate().check(GateKey.REGISTRATION_REQUEST))
                (new LangMessage(plugin.logger()))
                    .insert(
                        "["+server.getServerInfo().getName()+"]" +
                        "("+server.getServerInfo().getAddress().getHostName()+":"+server.getServerInfo().getAddress().getPort()+")" +
                        " "+ Lang.get(LangKey.ICON_REQUEST_REGISTRATION) +" "+familyName
                    )
                    .print();

            if(plugin.getProxy().contains(server.getServerInfo().getName())) throw new DuplicateRequestException("Server ["+server.getServerInfo().getName()+"]("+server.getServerInfo().getAddress()+":"+server.getServerInfo().getAddress().getPort()+") can't be registered twice!");

            ServerFamily<? extends PaperServerLoadBalancer> family = this.familyManager.find(familyName);
            if(family == null) throw new InvalidAlgorithmParameterException("A family with the name `"+familyName+"` doesn't exist!");

            RegisteredServer registeredServer = plugin.getVelocityServer().registerServer(server.getServerInfo());
            if(registeredServer == null) throw new NullPointerException("Unable to register the server to the proxy.");

            family.addServer(server);

            this.lifeMatrix.put(server.getServerInfo(),true);

            if(VelocityRustyConnector.getInstance().logger().getGate().check(GateKey.REGISTRATION_REQUEST))
                (new LangMessage(plugin.logger()))
                    .insert(
                        "["+registeredServer.getServerInfo().getName()+"]" +
                                "("+registeredServer.getServerInfo().getAddress().getHostName()+":"+registeredServer.getServerInfo().getAddress().getPort()+")" +
                                " "+ Lang.get(LangKey.ICON_REGISTERED) +" "+family.getName()
                    )
                    .print();

            return registeredServer;
        } catch (Exception error) {
            if(plugin.logger().getGate().check(GateKey.REGISTRATION_REQUEST))
                (new LangMessage(plugin.logger()))
                    .insert(
                            "["+server.getServerInfo().getName()+"]" +
                                    "("+server.getServerInfo().getAddress().getHostName()+":"+server.getServerInfo().getAddress().getPort()+")" +
                                    " "+ Lang.get(LangKey.ICON_CANCELED) +" "+familyName
                    )
                    .insert(error.getMessage())
                    .print();
        }
        return null;
    }

    /**
     * Unregister a server from the proxy.
     * @param serverInfo The server to be unregistered.
     */
    public void unregisterServer(ServerInfo serverInfo, String familyName) {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();
        PaperServer server = this.findServer(serverInfo);
        try {
            if(server == null) throw new NullPointerException("Server ["+serverInfo.getName()+"]("+serverInfo.getAddress()+":"+serverInfo.getAddress().getPort()+") doesn't exist! It can't be unregistered!");

            if(plugin.logger().getGate().check(GateKey.UNREGISTRATION_REQUEST))
                (new LangMessage(plugin.logger()))
                    .insert(
                        "["+serverInfo.getName()+"]" +
                              "("+serverInfo.getAddress().getHostName()+":"+serverInfo.getAddress().getPort()+")" +
                              " "+ Lang.get(LangKey.ICON_REQUESTING_UNREGISTRATION) +" "+server.getFamilyName()
                    )
                    .print();

            ServerFamily<? extends PaperServerLoadBalancer> family = server.getFamily();
            family.removeServer(server);

            plugin.getVelocityServer().unregisterServer(server.getServerInfo());

            this.lifeMatrix.remove(serverInfo);

            if(plugin.logger().getGate().check(GateKey.UNREGISTRATION_REQUEST))
                (new LangMessage(plugin.logger()))
                        .insert(
                                "["+server.getServerInfo().getName()+"]" +
                                        "("+server.getServerInfo().getAddress().getHostName()+":"+server.getServerInfo().getAddress().getPort()+")" +
                                        " "+ Lang.get(LangKey.ICON_UNREGISTERED) +" "+server.getFamilyName()
                        )
                        .print();
        } catch (Exception error) {
            if(plugin.logger().getGate().check(GateKey.UNREGISTRATION_REQUEST)) {
                assert server != null;
                (new LangMessage(plugin.logger()))
                        .insert(
                                "["+server.getServerInfo().getName()+"]" +
                                        "("+server.getServerInfo().getAddress().getHostName()+":"+server.getServerInfo().getAddress().getPort()+")" +
                                        " "+ Lang.get(LangKey.ICON_CANCELED) +" "+familyName
                        )
                        .insert(error.getMessage())
                        .print();
            }
        }
    }

    /**
     * Initializes the proxy based on the configuration.
     * @param config The configuration file.
     */
    public static Proxy init(DefaultConfig config) throws IllegalAccessException {
        Proxy proxy = new Proxy(config.getPrivate_key());

        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        List<ServerFamily<? extends PaperServerLoadBalancer>> families = ServerFamily.init(config);
        families.forEach(family -> proxy.getFamilyManager().add(family));

        proxy.setRootFamily(config.getRoot_family());

        Redis redis = new Redis();
        redis.setConnection(
                config.getRedis_host(),
                config.getRedis_port(),
                config.getRedis_password(),
                config.getRedis_dataChannel()
        );
        redis.connect(plugin);

        proxy.setRedis(redis);

        proxy.startHeart(config.getHeartbeat());

        if(config.isWhitelist_enabled()) {
            proxy.setWhitelist(config.getWhitelist_name());

            proxy.whitelistManager.add(Whitelist.init(config.getWhitelist_name()));
        } else {
        }

        if(config.isMessageTunnel_enabled()) {
            List<String> whitelist = config.getMessageTunnel_whitelist();
            List<String> blacklist = config.getMessageTunnel_denylist();

            boolean useWhitelist = whitelist.size() > 0;
            boolean useBlacklist = blacklist.size() > 0;

            MessageTunnel messageTunnel = new MessageTunnel(useBlacklist, useWhitelist);

            plugin.setMessageTunnel(messageTunnel);

            whitelist.forEach(entry -> {
                String[] addressSplit = entry.split(":");

                InetSocketAddress address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));

                messageTunnel.whitelistAddress(address);
            });

            blacklist.forEach(entry -> {
                String[] addressSplit = entry.split(":");

                InetSocketAddress address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));

                messageTunnel.blacklistAddress(address);
            });

            plugin.logger().log("---------| Finished!");
        } else {
            plugin.logger().log("---------| Finished! Proxy doesn't have a message tunnel.");
        }

        return proxy;
    }
}
