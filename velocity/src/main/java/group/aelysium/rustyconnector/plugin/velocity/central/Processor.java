package group.aelysium.rustyconnector.plugin.velocity.central;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisClient;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.firewall.MessageTunnelService;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.database.MySQLService;
import group.aelysium.rustyconnector.core.lib.model.IRKLifecycle;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.config.FamiliesConfig;
import group.aelysium.rustyconnector.plugin.velocity.config.PrivateKeyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.Clock;
import group.aelysium.rustyconnector.plugin.velocity.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.database.HomeServerMappingsDatabase;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.RoundedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ScalarServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.StaticServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancingService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerLifeMatrixService;
import group.aelysium.rustyconnector.plugin.velocity.lib.tpa.TPAService;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.WhitelistService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.core.lib.model.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Processor extends IRKLifecycle {
    private final Map<Class<? extends Service>, Service> services;
    private Clock serverLifecycleHeart;
    private Clock tpaRequestCleaner;
    
    protected Processor(Map<Class<? extends Service>, Service> services) {
        this.services = services;
    }

    public <S extends Service> S getService(Class<S> type) {
        return (S) this.services.get(type);
    }

    /**
     * Remove any home server mappings which have been cached for a specific player.
     * @param player The player to uncache mappings for.
     */
    public void uncacheHomeServerMappings(Player player) {
        List<BaseServerFamily> familyList = this.getService(FamilyService.class).dump().stream().filter(family -> family instanceof StaticServerFamily).toList();
        if(familyList.size() == 0) return;

        for (BaseServerFamily family : familyList) {
            ((StaticServerFamily) family).uncacheHomeServer(player);
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

    protected static class Builder {
        private final Map<Class<? extends Service>, Service> services = new HashMap<>();

        public Builder addService(Service service) {
            this.services.put(service.getClass(), service);
            return this;
        }
        
        public Processor build() {
            return new Processor(this.services);
        }
    }

    public static class Lifecycle {
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

            logger.log("Finished setting up redis");

            // Setup MySQL
            try {
                if (config.isMysql_enabled()) {
                    MySQLService mySQLService = new MySQLService.MySQLBuilder()
                            .setDisabled()
                            .build();

                    builder.addService(mySQLService);
                    logger.send(Component.text("MySQL is disabled! Any static families that are defined will be disabled.", NamedTextColor.YELLOW));
                } else {
                    MySQLService mySQLService = new MySQLService.MySQLBuilder()
                            .setHost(config.getMysql_host())
                            .setPort(config.getMysql_port())
                            .setDatabase(config.getMysql_database())
                            .setUser(config.getMysql_user())
                            .setPassword(config.getMysql_password())
                            .build();

                    builder.addService(mySQLService);

                    HomeServerMappingsDatabase.init(mySQLService);
                    logger.log("Finished setting up MySQL");

                }
            } catch (CommunicationsException e) {
                throw new IllegalAccessException("Unable to connect to MySQL! Is the server available?");
            }

            // Setup families
            logger.log("Setting up Families");

            FamiliesConfig familiesConfig = FamiliesConfig.newConfig(new File(String.valueOf(api.getDataFolder()), "families.yml"), "velocity_families_template.yml");
            if(!familiesConfig.generate())
                throw new IllegalStateException("Unable to load or create families.yml!");
            familiesConfig.register();

            FamilyService familyService = new FamilyService();
            builder.addService(familyService);

            WhitelistService whitelistService = new WhitelistService();
            builder.addService(whitelistService);

            for (String familyName: familiesConfig.getScalarFamilies())
                familyService.add(ScalarServerFamily.init(whitelistService, familyName));
            if(config.isMysql_enabled())
                for (String familyName: familiesConfig.getStaticFamilies())
                    familyService.add(StaticServerFamily.init(whitelistService, familyName));
            for (String familyName: familiesConfig.getRoundedFamilies())
                familyService.add(RoundedServerFamily.init(familyName));

            logger.log("Setting up root family");

            familyService.setRootFamily(ScalarServerFamily.init(whitelistService, familiesConfig.getRootFamilyName()));

            logger.log("Finished setting up root family");

            logger.log("Finished setting up families");

            if(config.isHearts_serverLifecycle_enabled()) {
                ServerLifeMatrixService lifeMatrixService = new ServerLifeMatrixService(familyService.size(), config.getServices_serverLifecycle_interval(), config.shouldHearts_serverLifecycle_unregisterOnIgnore());
                builder.addService(lifeMatrixService);
            } else builder.addService(new ServerLifeMatrixService());

            if(config.getMessageTunnel_familyServerSorting_enabled()) {
                builder.addService(new LoadBalancingService(familyService.size(), config.getMessageTunnel_familyServerSorting_interval()));
            }

            builder.addService(new TPAService(10));
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

            return builder.build();
        }
    }
}
