package group.aelysium.rustyconnector.plugin.velocity.central;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisClient;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisService;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.firewall.DataTransitService;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.database.mysql.MySQLService;
import group.aelysium.rustyconnector.core.lib.model.IKLifecycle;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.config.*;
import group.aelysium.rustyconnector.plugin.velocity.lib.database.HomeServerMappingsDatabase;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.RootServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ScalarServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.StaticServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsMySQLService;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancingService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.MagicLinkService;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.PlayerMySQLService;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.PlayerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.WhitelistService;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Processor extends IKLifecycle<ProcessorServiceHandler> {
    protected Processor(Map<Class<? extends Service>, Service> services) {
        super(new ProcessorServiceHandler(services));
    }

    @Override
    public void kill() {
        this.services.killAll();
    }

    public static Processor init(DefaultConfig config) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException, SQLException {
        VelocityAPI api = VelocityAPI.get();
        PluginLogger logger = api.logger();
        Processor.Builder builder = new Processor.Builder();

        // Setup private key
        PrivateKeyConfig privateKeyConfig = PrivateKeyConfig.newConfig(new File(String.valueOf(api.dataFolder()), "private.key"));
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

        FamiliesConfig familiesConfig = FamiliesConfig.newConfig(new File(String.valueOf(api.dataFolder()), "families.yml"), "velocity_families_template.yml");
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
            familyService.add(ScalarServerFamily.init(familyName));
        for (String familyName: familiesConfig.getStaticFamilies())
            familyService.add(StaticServerFamily.init(familyName));

        logger.log("Setting up root family");

        RootServerFamily rootFamily = RootServerFamily.init(familiesConfig.getRootFamilyName());
        familyService.setRootFamily(rootFamily);

        logger.log("Finished setting up root family");

        logger.log("Finished setting up families");

        if(config.getServices_loadBalancing_enabled()) {
            builder.addService(new LoadBalancingService(familyService.size(), config.getServices_loadBalancing_interval()));
        }

        logger.log("Finished loading services...");

        // Setup network whitelist
        if(config.isWhitelist_enabled())
            whitelistService.setProxyWhitelist(Whitelist.init(config.getWhitelist_name()));
        logger.log("Finished setting up network whitelist");

        // Setup Data Transit
        DataTransitConfig dataTransitConfig = DataTransitConfig.newConfig(new File(String.valueOf(api.dataFolder()), "data-transit.yml"), "velocity_data_transit_template.yml");
        if(!dataTransitConfig.generate())
            throw new IllegalStateException("Unable to load or create data-transit.yml!");
        dataTransitConfig.register();

        builder.addService(new MessageCacheService(dataTransitConfig.getCache_size(), dataTransitConfig.getCache_ignoredStatuses(), dataTransitConfig.getCache_ignoredTypes()));
        logger.log("Set message cache size to be: "+dataTransitConfig.getCache_size());

        DataTransitService dataTransitService = new DataTransitService(
                dataTransitConfig.isDenylist_enabled(),
                dataTransitConfig.isWhitelist_enabled(),
                dataTransitConfig.getMaxPacketLength()
        );
        builder.addService(dataTransitService);

        if(dataTransitConfig.isWhitelist_enabled())
            dataTransitConfig.getWhitelist_addresses().forEach(entry -> {
                String[] addressSplit = entry.split(":");

                InetSocketAddress address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));

                dataTransitService.whitelistAddress(address);
            });

        if(dataTransitConfig.isDenylist_enabled())
            dataTransitConfig.getDenylist_addresses().forEach(entry -> {
                String[] addressSplit = entry.split(":");

                InetSocketAddress address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));

                dataTransitService.blacklistAddress(address);
            });

        logger.log("Finished setting up message tunnel");


        MagicLinkService magicLinkService = new MagicLinkService(3, config.getServices_serverLifecycle_serverPingInterval());
        builder.addService(magicLinkService);

        ServerService.Builder serverServiceBuilder = new ServerService.Builder()
                .setServerTimeout(config.getServices_serverLifecycle_serverTimeout())
                .setServerInterval(config.getServices_serverLifecycle_serverPingInterval());

        builder.addService(serverServiceBuilder.build());

        return builder.build();
    }

    public static class Initializer {
        public static Optional<PartyService> buildPartyService() {
            VelocityAPI api = VelocityAPI.get();
            PluginLogger logger = api.logger();
            try {
                PartyConfig config = PartyConfig.newConfig(new File(String.valueOf(api.dataFolder()), "party.yml"), "velocity_party_template.yml");
                if (!config.generate())
                    throw new IllegalStateException("Unable to load or create party.yml!");
                config.register();

                if(!config.isEnabled()) return Optional.empty();

                PartyService.PartySettings settings = new PartyService.PartySettings(
                        config.getMaxMembers(),
                        config.isFriendsOnly(),
                        config.isLocalOnly(),
                        config.isPartyLeader_onlyLeaderCanInvite(),
                        config.isPartyLeader_onlyLeaderCanKick(),
                        config.isPartyLeader_onlyLeaderCanSwitchServers(),
                        config.isPartyLeader_disbandOnLeaderQuit(),
                        config.getSwitchingServers_switchPower()
                );

                return Optional.of(new PartyService(settings));
            } catch (Exception e) {
                logger.send(VelocityLang.BOXED_MESSAGE_COLORED.build(Component.text(e.getMessage()), NamedTextColor.RED));
            }
            return Optional.empty();
        }

        public static Optional<FriendsService> buildFriendsService() {
            VelocityAPI api = VelocityAPI.get();
            PluginLogger logger = api.logger();
            try {
                FriendsConfig config = FriendsConfig.newConfig(new File(String.valueOf(api.dataFolder()), "friends.yml"), "velocity_friends_template.yml");
                if (!config.generate())
                    throw new IllegalStateException("Unable to load or create friends.yml!");
                config.register();

                if(!config.isEnabled()) return Optional.empty();

                FriendsService.FriendsSettings settings = new FriendsService.FriendsSettings(
                        config.getMaxFriends(),
                        config.isSendNotifications(),
                        config.isShowFamilies(),
                        config.isAllowMessaging()
                );

                FriendsMySQLService friendsMySQLService = new FriendsMySQLService.Builder()
                        .setHost(config.getMysql_host())
                        .setPort(config.getMysql_port())
                        .setDatabase(config.getMysql_database())
                        .setUser(config.getMysql_user())
                        .setPassword(config.getMysql_password())
                        .build();

                friendsMySQLService.init();

                return Optional.of(new FriendsService(settings, friendsMySQLService));
            } catch (Exception e) {
                logger.send(VelocityLang.BOXED_MESSAGE_COLORED.build(Component.text(e.getMessage()), NamedTextColor.RED));
            }
            return Optional.empty();
        }

        public static Optional<PlayerService> buildPlayerService() {
            VelocityAPI api = VelocityAPI.get();
            PluginLogger logger = api.logger();
            try {
                FriendsConfig config = FriendsConfig.newConfig(new File(String.valueOf(api.dataFolder()), "friends.yml"), "velocity_friends_template.yml");
                if (!config.generate())
                    throw new IllegalStateException("Unable to load or create friends.yml!");
                config.register();

                if(!config.isEnabled()) return Optional.empty();

                PlayerMySQLService playerMySQLService = new PlayerMySQLService.Builder()
                        .setHost(config.getMysql_host())
                        .setPort(config.getMysql_port())
                        .setDatabase(config.getMysql_database())
                        .setUser(config.getMysql_user())
                        .setPassword(config.getMysql_password())
                        .build();

                playerMySQLService.init();

                return Optional.of(new PlayerService(playerMySQLService));
            } catch (Exception e) {
                logger.send(VelocityLang.BOXED_MESSAGE_COLORED.build(Component.text(e.getMessage()), NamedTextColor.RED));
            }
            return Optional.empty();
        }

        public static Optional<DynamicTeleportService> buildDynamicTeleportService() {
            VelocityAPI api = VelocityAPI.get();
            PluginLogger logger = api.logger();
            try {
                DynamicTeleportConfig config = DynamicTeleportConfig.newConfig(new File(String.valueOf(api.dataFolder()), "dynamic_teleport.yml"), "velocity_dynamic_teleport_template.yml");
                if (!config.generate())
                    throw new IllegalStateException("Unable to load or create dynamic_teleport.yml!");
                config.register();

                if(!config.isEnabled()) return Optional.empty();

                return DynamicTeleportService.init(config);
            } catch (Exception e) {
                logger.send(VelocityLang.BOXED_MESSAGE_COLORED.build(Component.text(e.getMessage()), NamedTextColor.RED));
            }
            return Optional.empty();
        }
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

}