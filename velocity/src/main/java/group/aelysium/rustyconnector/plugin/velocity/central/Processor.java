package group.aelysium.rustyconnector.plugin.velocity.central;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import com.typesafe.config.Optional;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisClient;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisService;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.firewall.MessageTunnelService;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.database.MySQLService;
import group.aelysium.rustyconnector.core.lib.model.IKLifecycle;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.config.FamiliesConfig;
import group.aelysium.rustyconnector.plugin.velocity.config.PrivateKeyConfig;
import group.aelysium.rustyconnector.plugin.velocity.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.database.HomeServerMappingsDatabase;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ScalarServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.StaticServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancingService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.MagicLinkService;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.TPACleaningService;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.WhitelistService;
import group.aelysium.rustyconnector.core.lib.model.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Processor extends IKLifecycle {
    protected Processor(Map<Class<? extends Service>, Service> services) {
        super(services);
    }

    @Override
    public void kill() {
        this.services.values().forEach(Service::kill);
        this.services.clear();
    }

    public static Processor init(DefaultConfig config) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException, SQLException {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();
        Processor.Builder builder = new Processor.Builder();

        // Setup private key
        PrivateKeyConfig privateKeyConfig = PrivateKeyConfig.newConfig(new File(String.valueOf(api.getDataFolder()), "private.key"));
        if(!privateKeyConfig.generate())
            throw new IllegalStateException("Unable to load or create private.key!");
        char[] privateKey = null;
        try {
            privateKey = privateKeyConfig.get();
        } catch (Exception ignore) {}
        if(privateKey == null) throw new IllegalAccessException("There was a fatal error while reading private.key!");

        // Setup Redis
        RedisClient.Builder redisClientBuilder = new RedisClient.Builder()
                .setHost(config.getRedis_host())
                .setPort(config.getRedis_port())
                .setUser(config.getRedis_user())
                .setPrivateKey(privateKey)
                .setDataChannel(config.getRedis_dataChannel());

        if(!config.getRedis_password().equals(""))
            redisClientBuilder.setPassword(config.getRedis_password());

        builder.addService(new RedisService(redisClientBuilder, privateKey));

        logger.log("Finished setting up redis");

        // Setup families
        logger.log("Setting up Families");

        FamiliesConfig familiesConfig = FamiliesConfig.newConfig(new File(String.valueOf(api.getDataFolder()), "families.yml"), "velocity_families_template.yml");
        if(!familiesConfig.generate())
            throw new IllegalStateException("Unable to load or create families.yml!");
        familiesConfig.register();

        // Setup MySQL
        java.util.Optional<MySQLService> mySQLService = java.util.Optional.empty();
        try {
            if (!familiesConfig.getStaticFamilies().isEmpty()) {
                MySQLService builtMySQLService = new MySQLService.Builder()
                        .setHost(familiesConfig.getMysql_host())
                        .setPort(familiesConfig.getMysql_port())
                        .setDatabase(familiesConfig.getMysql_database())
                        .setUser(familiesConfig.getMysql_user())
                        .setPassword(familiesConfig.getMysql_password())
                        .build();

                HomeServerMappingsDatabase.init(builtMySQLService);

                mySQLService = java.util.Optional.of(builtMySQLService);
                logger.log("Finished setting up MySQL");
            }

        } catch (CommunicationsException e) {
            throw new IllegalAccessException("Unable to connect to MySQL! Is the server available?");
        } catch (Exception e) {
            throw new IllegalAccessException("Unable to connect to initialize MySQL for Static Families!");
        }

        FamilyService familyService = new FamilyService(familiesConfig.shouldRootFamilyCatchDisconnectingPlayers(), mySQLService);
        builder.addService(familyService);

        WhitelistService whitelistService = new WhitelistService();
        builder.addService(whitelistService);

        for (String familyName: familiesConfig.getScalarFamilies())
            familyService.add(ScalarServerFamily.init(whitelistService, familyName));
        for (String familyName: familiesConfig.getStaticFamilies())
            familyService.add(StaticServerFamily.init(whitelistService, familyName));

        logger.log("Setting up root family");

        familyService.setRootFamily(ScalarServerFamily.init(whitelistService, familiesConfig.getRootFamilyName()));

        logger.log("Finished setting up root family");

        logger.log("Finished setting up families");

        if(config.getServices_loadBalancing_enabled()) {
            builder.addService(new LoadBalancingService(familyService.size(), config.getServices_loadBalancing_interval()));
        }

        builder.addService(new TPACleaningService(10));
        logger.log("Finished loading services...");

        // Setup network whitelist
        if(config.isWhitelist_enabled())
            whitelistService.setProxyWhitelist(Whitelist.init(config.getWhitelist_name()));
        logger.log("Finished setting up network whitelist");

        // Setup message tunnel
        builder.addService(new MessageCacheService(config.getMessageTunnel_messageCacheSize()));
        logger.log("Set message cache size to be: "+config.getMessageTunnel_messageCacheSize());

        MessageTunnelService messageTunnelService = new MessageTunnelService(
                config.isMessageTunnel_denylist_enabled(),
                config.isMessageTunnel_whitelist_enabled(),
                config.getMessageTunnel_messageMaxLength()
        );
        builder.addService(messageTunnelService);

        if(config.isMessageTunnel_whitelist_enabled())
            config.getMessageTunnel_whitelist_addresses().forEach(entry -> {
                String[] addressSplit = entry.split(":");

                InetSocketAddress address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));

                messageTunnelService.whitelistAddress(address);
            });

        if(config.isMessageTunnel_denylist_enabled())
            config.getMessageTunnel_denylist_addresses().forEach(entry -> {
                String[] addressSplit = entry.split(":");

                InetSocketAddress address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));

                messageTunnelService.blacklistAddress(address);
            });

        logger.log("Finished setting up message tunnel");


        MagicLinkService magicLinkService = new MagicLinkService(5, config.getServices_serverLifecycle_serverPingInterval());

        ServerService.Builder serverServiceBuilder = new ServerService.Builder()
                .setServerTimeout(config.getServices_serverLifecycle_serverTimeout())
                .setServerInterval(config.getServices_serverLifecycle_serverPingInterval())
                .addService(magicLinkService);

        builder.addService(serverServiceBuilder.build());

        return builder.build();
    }

    protected static class Builder {
        protected final Map<Class<? extends Service>, Service> services = new HashMap<>();

        public Builder addService(Service service) {
            this.services.put(service.getClass(), service);
            return this;
        }

        public Processor build() {
            return new Processor(this.services);
        }
    }

    /**
     * The services that are valid for this service provider.
     * Services marked as @Optional should be handled accordingly.
     * If a service is not marked @Optional it should be impossible for that service to be unavailable.
     */
    public static class ValidServices {
        public static Class<FamilyService> FAMILY_SERVICE = FamilyService.class;
        public static Class<ServerService> SERVER_SERVICE = ServerService.class;
        public static Class<RedisService> REDIS_SERVICE = RedisService.class;
        public static Class<MessageTunnelService> MESSAGE_TUNNEL_SERVICE = MessageTunnelService.class;
        public static Class<MessageCacheService> MESSAGE_CACHE_SERVICE = MessageCacheService.class;
        public static Class<WhitelistService> WHITELIST_SERVICE = WhitelistService.class;
        public static Class<LoadBalancingService> LOAD_BALANCING_SERVICE = LoadBalancingService.class;

        @Optional
        public static Class<PartyService> PARTY_SERVICE = PartyService.class;

        @Optional
        public static Class<FriendsService> FRIENDS_SERVICE = FriendsService.class;

        @Optional
        public static Class<DynamicTeleportService> DYNAMIC_TELEPORT_SERVICE = DynamicTeleportService.class;

        public static boolean isOptional(Class<? extends Service> clazz) {
            if(clazz == PARTY_SERVICE) return true;
            if(clazz == FRIENDS_SERVICE) return true;
            if(clazz == DYNAMIC_TELEPORT_SERVICE) return true;

            return false;
        }
    }
}
