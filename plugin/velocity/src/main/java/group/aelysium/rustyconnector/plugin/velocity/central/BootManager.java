package group.aelysium.rustyconnector.plugin.velocity.central;

import group.aelysium.rustyconnector.core.lib.connectors.ConnectorsService;
import group.aelysium.rustyconnector.core.lib.connectors.Connector;
import group.aelysium.rustyconnector.core.lib.connectors.storage.StorageConnector;
import group.aelysium.rustyconnector.core.lib.data_transit.DataTransitService;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.database.mysql.MySQLConnector;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.config.*;
import group.aelysium.rustyconnector.plugin.velocity.lib.Core;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.RootServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.ScalarServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.static_family.StaticServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsMySQL;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancingService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.MagicLinkService;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.PlayerMySQL;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.PlayerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.ViewportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.GatewayService;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.WhitelistService;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.*;

public class BootManager {
    public static Core buildCore() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException, SQLException {
        VelocityAPI api = VelocityAPI.get();
        PluginLogger logger = api.logger();
        Map<Class<? extends Service>, Service> services = new HashMap<>();
        List<String> requestedConnectors = new ArrayList<>();

        // Setup private key
        char[] privateKey;
        {
            PrivateKeyConfig privateKeyConfig = PrivateKeyConfig.newConfig(new File(String.valueOf(api.dataFolder()), "private.key"));
            if (!privateKeyConfig.generate())
                throw new IllegalStateException("Unable to load or create private.key!");
            try {
                privateKey = privateKeyConfig.get();
            } catch (Exception ignore) {
                throw new IllegalAccessException("There was a fatal error while reading private.key!");
            }
        }

        // Setup default config
        DefaultConfig defaultConfig = DefaultConfig.newConfig(new File(String.valueOf(api.dataFolder()), "config.yml"), "velocity_config_template.yml");
        {
            if (!defaultConfig.generate())
                throw new IllegalStateException("Unable to load or create config.yml!");
            defaultConfig.register();
        }

        // Setup logger config
        LoggerConfig loggerConfig = LoggerConfig.newConfig(new File(String.valueOf(api.dataFolder()), "logger.yml"), "velocity_logger_template.yml");
        {
            if (!loggerConfig.generate())
                throw new IllegalStateException("Unable to load or create logger.yml!");
            loggerConfig.register();
            PluginLogger.init(loggerConfig);
        }

        // Setup connectors
        {
            logger.send(Component.text("Building Connectors...", NamedTextColor.DARK_GRAY));

            ConnectorsConfig connectorsConfig = ConnectorsConfig.newConfig(new File(String.valueOf(api.dataFolder()), "connectors.yml"), "velocity_connectors_template.yml");
            if (!connectorsConfig.generate())
                throw new IllegalStateException("Unable to load or create connectorsConfig.yml!");
            services.put(ConnectorsService.class, connectorsConfig.register(privateKey));

            requestedConnectors.add(defaultConfig.messenger());

            logger.send(Component.text("Finished building Connectors.", NamedTextColor.GREEN));
        }

        {
            // Setup families
            logger.send(Component.text("Building families service...", NamedTextColor.DARK_GRAY));

            FamiliesConfig familiesConfig = FamiliesConfig.newConfig(new File(String.valueOf(api.dataFolder()), "families.yml"), "velocity_families_template.yml");
            if (!familiesConfig.generate())
                throw new IllegalStateException("Unable to load or create families.yml!");
            familiesConfig.register();

            java.util.Optional<MySQLConnector> connector = java.util.Optional.empty();
            {
                try {
                    if (!familiesConfig.staticFamilies().isEmpty()) {
                        logger.send(Component.text(" | Static families detected. Building static family MySQL...", NamedTextColor.DARK_GRAY));
                        if(!requestedConnectors.contains(familiesConfig.staticFamilyStorage())) throw new NoOutputException();
                        Connector<?> fetchedConnector = ((ConnectorsService) services.get(ConnectorsService.class)).get(familiesConfig.staticFamilyStorage());
                        if(fetchedConnector == null) throw new NoOutputException();
                        connector = Optional.of((MySQLConnector) fetchedConnector);
                    }
                } catch (NoOutputException ignore) {
                } catch (Exception e) {
                    throw new IllegalAccessException("Unable to connect to initialize MySQL for Static Families!");
                }

                logger.send(Component.text(" | Finished building static family MySQL.", NamedTextColor.GREEN));
            }

            logger.send(Component.text(" | Registering family service to the API...", NamedTextColor.DARK_GRAY));
            FamilyService familyService = new FamilyService(familiesConfig.shouldRootFamilyCatchDisconnectingPlayers(), connector);
            services.put(FamilyService.class, familyService);
            logger.send(Component.text(" | Finished registering family service to API.", NamedTextColor.GREEN));

            {
                logger.send(Component.text(" | Building families...", NamedTextColor.DARK_GRAY));
                for (String familyName : familiesConfig.scalarFamilies()) {
                    familyService.add(ScalarServerFamily.init(familyName));
                    logger.send(Component.text(" | Registered family: "+familyName, NamedTextColor.YELLOW));
                }
                for (String familyName : familiesConfig.staticFamilies()) {
                    familyService.add(StaticServerFamily.init(familyName));
                    logger.send(Component.text(" | Registered family: "+familyName, NamedTextColor.YELLOW));
                }
                logger.send(Component.text(" | Finished building families.", NamedTextColor.GREEN));
            }

            {
                logger.send(Component.text(" | Building root family...", NamedTextColor.DARK_GRAY));

                RootServerFamily rootFamily = RootServerFamily.init(familiesConfig.rootFamilyName());
                familyService.setRootFamily(rootFamily);
                logger.send(Component.text(" | Registered root family: "+rootFamily.name(), NamedTextColor.YELLOW));

                logger.send(Component.text(" | Finished building root family.", NamedTextColor.GREEN));
            }

            logger.send(Component.text(" | Registering load balancing service to the API...", NamedTextColor.DARK_GRAY));
            if (defaultConfig.services_loadBalancing_enabled()) {
                services.put(LoadBalancingService.class, new LoadBalancingService(familyService.size(), defaultConfig.services_loadBalancing_interval()));
            }
            logger.send(Component.text(" | Finished registering load balancing service to the API.", NamedTextColor.GREEN));

            logger.send(Component.text("Finished building families service.", NamedTextColor.GREEN));
        }

        // Setup network whitelist
        {
            logger.send(Component.text("Registering whitelist service to the API...", NamedTextColor.DARK_GRAY));
            WhitelistService whitelistService = new WhitelistService();
            services.put(WhitelistService.class, whitelistService);
            logger.send(Component.text("Finished registering whitelist service to the API.", NamedTextColor.GREEN));

            logger.send(Component.text("Building proxy whitelist...", NamedTextColor.DARK_GRAY));
            if (defaultConfig.whitelist_enabled()) {
                whitelistService.setProxyWhitelist(Whitelist.init(defaultConfig.whitelist_name()));
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
                services.put(MessageCacheService.class, new MessageCacheService(dataTransitConfig.cache_size(), dataTransitConfig.cache_ignoredStatuses(), dataTransitConfig.cache_ignoredTypes()));
                logger.send(Component.text(" | Message cache size set to: "+dataTransitConfig.cache_size(), NamedTextColor.YELLOW));
                logger.send(Component.text(" | Finished building message cache service.", NamedTextColor.GREEN));
            }

            DataTransitService dataTransitService = new DataTransitService(
                    dataTransitConfig.denylist_enabled(),
                    dataTransitConfig.whitelist_enabled(),
                    dataTransitConfig.maxPacketLength()
            );
            services.put(DataTransitService.class, dataTransitService);

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

            MagicLinkService magicLinkService = new MagicLinkService(3, defaultConfig.services_serverLifecycle_serverPingInterval());
            services.put(MagicLinkService.class, magicLinkService);

            logger.send(Component.text("Finished building magic link service.", NamedTextColor.GREEN));
        }

        {
            logger.send(Component.text("Building server service...", NamedTextColor.DARK_GRAY));

            ServerService.Builder serverServiceBuilder = new ServerService.Builder()
                    .setServerTimeout(defaultConfig.services_serverLifecycle_serverTimeout())
                    .setServerInterval(defaultConfig.services_serverLifecycle_serverPingInterval());

            services.put(ServerService.class, serverServiceBuilder.build());

            logger.send(Component.text("Finished building server service.", NamedTextColor.GREEN));
        }

        {
            WebhooksConfig webhooksConfig = WebhooksConfig.newConfig(new File(String.valueOf(api.dataFolder()), "webhooks.yml"), "velocity_webhooks_template.yml");
            if(!webhooksConfig.generate())
                throw new IllegalStateException("Unable to load or create webhooks.yml!");
            webhooksConfig.register();
        }

        // Verify Connectors
        {
            logger.send(Component.text("Validating Connector service...", NamedTextColor.DARK_GRAY));
            ConnectorsService connectorsService = ((ConnectorsService) services.get(ConnectorsService.class));

            /*
             * Make sure that configs aren't trying to access connectors which don't exist.
             * Also makes sure that, if there are excess connectors defined, we only load and attempt to boot the ones that are actually being called.
             */
            for (String name : requestedConnectors) {
                logger.send(Component.text(" | Checking and building connector ["+name+"]...", NamedTextColor.DARK_GRAY));

                if(!connectorsService.containsKey(name))
                    throw new RuntimeException("No connector with the name '"+name+"' was found!");

                Connector connector = connectorsService.get(name);
                try {
                    connector.connect();
                } catch (ConnectException e) {
                    throw new RuntimeException(e);
                }
            }
            logger.send(Component.text("Finished validating Connector service.", NamedTextColor.GREEN));
        }

        return new Core(services, defaultConfig.messenger());
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
        public static Optional<ViewportService> buildViewportService() {
            VelocityAPI api = VelocityAPI.get();
            PluginLogger logger = api.logger();
            try {
                logger.send(Component.text("Building viewport service...", NamedTextColor.DARK_GRAY));

                ViewportConfig viewportConfig = ViewportConfig.newConfig(new File(String.valueOf(api.dataFolder()), "viewport.yml"), "velocity_viewport_template.yml");
                if (!viewportConfig.generate())
                    throw new IllegalStateException("Unable to load or create viewport.yml!");
                viewportConfig.register();

                if(!viewportConfig.isEnabled()) return Optional.empty();

                logger.send(Component.text(" | Building viewport MySQL...", NamedTextColor.DARK_GRAY));
                StorageConnector<?> connector = (StorageConnector<?>) api.services().connectorsService().get(viewportConfig.storage());
                if(connector == null) throw new NullPointerException("You must define a storage method for viewport!");
                logger.send(Component.text(" | Finished building viewport MySQL.", NamedTextColor.GREEN));

                GatewayService gatewayService = new GatewayService(viewportConfig.getWebsocket_address(), viewportConfig.getRest_address());

                ViewportService service = new ViewportService.Builder()
                        .setStorageConnector(connector)
                        .setGatewayService(gatewayService)
                        .build();

                return Optional.of(service);
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

                logger.send(Component.text(" | Building friends MySQL...", NamedTextColor.DARK_GRAY));
                StorageConnector<?> connector = (StorageConnector<?>) api.services().connectorsService().get(config.storage());
                if(connector == null) throw new NullPointerException("You must define a storage method for viewport!");
                logger.send(Component.text(" | Finished building friends MySQL.", NamedTextColor.GREEN));

                return Optional.of(new FriendsService(settings, (MySQLConnector) connector));
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

                logger.send(Component.text(" | Building friends MySQL...", NamedTextColor.DARK_GRAY));
                StorageConnector<?> connector = (StorageConnector<?>) api.services().connectorsService().get(config.storage());
                if(connector == null) throw new NullPointerException("You must define a storage method for viewport!");
                logger.send(Component.text(" | Finished building friends MySQL.", NamedTextColor.GREEN));

                return Optional.of(new PlayerService((MySQLConnector) connector));
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
}