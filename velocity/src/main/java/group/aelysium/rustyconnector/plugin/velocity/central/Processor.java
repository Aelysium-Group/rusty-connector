package group.aelysium.rustyconnector.plugin.velocity.central;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisClient;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisService;
import group.aelysium.rustyconnector.core.lib.data_transit.DataTransitService;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
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
        if (!privateKeyConfig.generate())
            throw new IllegalStateException("Unable to load or create private.key!");
        char[] privateKey = null;
        try {
            privateKey = privateKeyConfig.get();
        } catch (Exception ignore) {
        }
        if (privateKey == null) throw new IllegalAccessException("There was a fatal error while reading private.key!");

        {
            // Setup Redis
            logger.send(Component.text("Building Redis...", NamedTextColor.DARK_GRAY));

            RedisClient.Builder redisClientBuilder = new RedisClient.Builder()
                    .setHost(config.redis_host())
                    .setPort(config.redis_port())
                    .setUser(config.redis_user())
                    .setPrivateKey(privateKey)
                    .setDataChannel(config.redis_dataChannel());

            if (!config.redis_password().equals(""))
                redisClientBuilder.setPassword(config.redis_password());

            builder.addService(new RedisService(redisClientBuilder, privateKey));

            logger.send(Component.text("Finished building Redis.", NamedTextColor.GREEN));
        }

        {
            // Setup families
            logger.send(Component.text("Building families service...", NamedTextColor.DARK_GRAY));

            FamiliesConfig familiesConfig = FamiliesConfig.newConfig(new File(String.valueOf(api.dataFolder()), "families.yml"), "velocity_families_template.yml");
            if (!familiesConfig.generate())
                throw new IllegalStateException("Unable to load or create families.yml!");
            familiesConfig.register();

            // Setup Static families MySQL
            java.util.Optional<MySQLService> mySQLService = java.util.Optional.empty();
            {
                logger.send(Component.text(" | Static families detected. Building static family MySQL...", NamedTextColor.DARK_GRAY));
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
                    }
                } catch (CommunicationsException e) {
                    throw new IllegalAccessException("Unable to connect to MySQL! Is the server available?");
                } catch (Exception e) {
                    throw new IllegalAccessException("Unable to connect to initialize MySQL for Static Families!");
                }

                logger.send(Component.text(" | Finished building static family MySQL.", NamedTextColor.GREEN));
            }

            logger.send(Component.text(" | Registering family service to the API...", NamedTextColor.DARK_GRAY));
            FamilyService familyService = new FamilyService(familiesConfig.shouldRootFamilyCatchDisconnectingPlayers(), mySQLService);
            builder.addService(familyService);
            logger.send(Component.text(" | Finished registering family service to API.", NamedTextColor.GREEN));

            {
                logger.send(Component.text(" | Building families...", NamedTextColor.DARK_GRAY));
                for (String familyName : familiesConfig.getScalarFamilies()) {
                    familyService.add(ScalarServerFamily.init(familyName));
                    logger.send(Component.text(" | Registered family: "+familyName, NamedTextColor.YELLOW));
                }
                for (String familyName : familiesConfig.getStaticFamilies()) {
                    familyService.add(StaticServerFamily.init(familyName));
                    logger.send(Component.text(" | Registered family: "+familyName, NamedTextColor.YELLOW));
                }
                logger.send(Component.text(" | Finished building families.", NamedTextColor.GREEN));
            }

            {
                logger.send(Component.text(" | Building root family...", NamedTextColor.DARK_GRAY));

                RootServerFamily rootFamily = RootServerFamily.init(familiesConfig.getRootFamilyName());
                familyService.setRootFamily(rootFamily);
                logger.send(Component.text(" | Registered root family: "+rootFamily.name(), NamedTextColor.YELLOW));

                logger.send(Component.text(" | Finished building root family.", NamedTextColor.GREEN));
            }

            logger.send(Component.text(" | Registering load balancing service to the API...", NamedTextColor.DARK_GRAY));
            if (config.services_loadBalancing_enabled()) {
                builder.addService(new LoadBalancingService(familyService.size(), config.services_loadBalancing_interval()));
            }
            logger.send(Component.text(" | Finished registering load balancing service to the API.", NamedTextColor.GREEN));

            logger.send(Component.text("Finished building families service.", NamedTextColor.GREEN));
        }

        // Setup network whitelist
        {
            logger.send(Component.text("Registering whitelist service to the API...", NamedTextColor.DARK_GRAY));
            WhitelistService whitelistService = new WhitelistService();
            builder.addService(whitelistService);
            logger.send(Component.text("Finished registering whitelist service to the API.", NamedTextColor.GREEN));

            logger.send(Component.text("Building proxy whitelist...", NamedTextColor.DARK_GRAY));
            if (config.whitelist_enabled()) {
                whitelistService.setProxyWhitelist(Whitelist.init(config.whitelist_name()));
                logger.send(Component.text("Finished building proxy whitelist.", NamedTextColor.GREEN));
            } else
                logger.send(Component.text("Finished building proxy whitelist. No whitelist is enabled for the proxy.", NamedTextColor.GREEN));
        }

        {
            logger.send(Component.text("Building data transit service...", NamedTextColor.DARK_GRAY));
            // Setup Data Transit
            DataTransitConfig dataTransitConfig = DataTransitConfig.newConfig(new File(String.valueOf(api.dataFolder()), "data_transit.yml"), "velocity_data_transit_template.yml");
            if (!dataTransitConfig.generate())
                throw new IllegalStateException("Unable to load or create data-transit.yml!");
            dataTransitConfig.register();

            {
                logger.send(Component.text(" | Building message cache service...", NamedTextColor.DARK_GRAY));
                builder.addService(new MessageCacheService(dataTransitConfig.cache_size(), dataTransitConfig.cache_ignoredStatuses(), dataTransitConfig.cache_ignoredTypes()));
                logger.send(Component.text(" | Message cache size set to: "+dataTransitConfig.cache_size(), NamedTextColor.YELLOW));
                logger.send(Component.text(" | Finished building message cache service.", NamedTextColor.GREEN));
            }

            DataTransitService dataTransitService = new DataTransitService(
                    dataTransitConfig.denylist_enabled(),
                    dataTransitConfig.whitelist_enabled(),
                    dataTransitConfig.maxPacketLength()
            );
            builder.addService(dataTransitService);

            if (dataTransitConfig.whitelist_enabled())
                dataTransitConfig.whitelist_addresses().forEach(entry -> {
                    String[] addressSplit = entry.split(":");

                    InetSocketAddress address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));

                    dataTransitService.whitelistAddress(address);
                });

            if (dataTransitConfig.denylist_enabled())
                dataTransitConfig.denylist_addresses().forEach(entry -> {
                    String[] addressSplit = entry.split(":");

                    InetSocketAddress address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));

                    dataTransitService.blacklistAddress(address);
                });

            logger.send(Component.text("Finished building data transit service.", NamedTextColor.GREEN));
        }

        {
            logger.send(Component.text("Building magic link service...", NamedTextColor.DARK_GRAY));

            MagicLinkService magicLinkService = new MagicLinkService(3, config.services_serverLifecycle_serverPingInterval());
            builder.addService(magicLinkService);

            logger.send(Component.text("Finished building magic link service.", NamedTextColor.GREEN));
        }

        {
            logger.send(Component.text("Building server service...", NamedTextColor.DARK_GRAY));

            ServerService.Builder serverServiceBuilder = new ServerService.Builder()
                    .setServerTimeout(config.services_serverLifecycle_serverTimeout())
                    .setServerInterval(config.services_serverLifecycle_serverPingInterval());

            builder.addService(serverServiceBuilder.build());

            logger.send(Component.text("Finished building server service.", NamedTextColor.GREEN));
        }

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