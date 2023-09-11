package group.aelysium.rustyconnector.plugin.velocity.lib;

import com.velocitypowered.api.command.CommandManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.velocitypowered.api.event.EventManager;
import group.aelysium.rustyconnector.core.lib.Callable;
import group.aelysium.rustyconnector.core.lib.connectors.Connection;
import group.aelysium.rustyconnector.core.lib.connectors.Connector;
import group.aelysium.rustyconnector.core.lib.connectors.ConnectorsService;
import group.aelysium.rustyconnector.core.lib.connectors.implementors.storage.mysql.MySQLConnector;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnection;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnector;
import group.aelysium.rustyconnector.core.lib.connectors.storage.StorageConnector;
import group.aelysium.rustyconnector.core.lib.data_transit.DataTransitService;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.core.lib.serviceable.ServiceableService;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.CommandRusty;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.central.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.velocity.central.config.PrivateKeyConfig;
import group.aelysium.rustyconnector.plugin.velocity.config.LoggerConfig;
import group.aelysium.rustyconnector.plugin.velocity.config.MemberKeyConfig;
import group.aelysium.rustyconnector.plugin.velocity.events.OnPlayerChangeServer;
import group.aelysium.rustyconnector.plugin.velocity.events.OnPlayerChooseInitialServer;
import group.aelysium.rustyconnector.plugin.velocity.events.OnPlayerDisconnect;
import group.aelysium.rustyconnector.plugin.velocity.events.OnPlayerKicked;
import group.aelysium.rustyconnector.plugin.velocity.lib.connectors.VelocityMessengerSubscriber;
import group.aelysium.rustyconnector.plugin.velocity.lib.connectors.config.ConnectorsConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.data_transit.config.DataTransitConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.config.DynamicTeleportConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.config.FamiliesConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.RootServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.ScalarServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.static_family.StaticServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.config.FriendsConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancingService;
import group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.MagicLinkService;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.config.PartyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.PlayerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.ViewportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.config.ViewportConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.GatewayService;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.config.WebhooksConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.WhitelistService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.*;

/**
 * The core RustyConnector kernel.
 * All aspects of the plugin should be accessible from here.
 * If not, check {@link Tinder}.
 */
public class Flame extends ServiceableService<CoreServiceHandler> {
    private String version;

    /**
     * The core message backbone where all RC messages are sent through.
     */
    private final MessengerConnector<? extends MessengerConnection> backbone;

    protected Flame(String version, Map<Class<? extends Service>, Service> services, String backboneConnector) {
        super(new CoreServiceHandler(services));
        this.backbone = (MessengerConnector<? extends MessengerConnection>) this.services().connectorsService().get(backboneConnector);
        this.version = version;
    }

    public String version() { return this.version; }

    public MessengerConnector<? extends MessengerConnection> backbone() {
        return this.backbone;
    }

    /**
     * Kill the {@link Flame}.
     * Typically good for if you want to ignite a new one.
     */
    public void exhaust(VelocityRustyConnector plugin) {
        Tinder.get().velocityServer().getEventManager().unregisterListeners(plugin);
        this.kill();
    }

    /**
     * Returns the currently active RustyConnector kernel.
     * This is exactly identical to calling {@link Tinder#get()}{@link Tinder#flame() .flame()}.
     * @return A {@link Flame}.
     */
    public static Flame get() {
        return Tinder.get().flame();
    }

    /**
     * Fabricates a new RustyConnector core and returns it.
     * @return A new RustyConnector {@link Flame}.
     */
    public static Flame fabricateNew(VelocityRustyConnector plugin) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        Initialize initialize = new Initialize();

        String version = initialize.version();
        char[] privateKey = initialize.privateKey();
        DefaultConfig defaultConfig = initialize.defaultConfig();
        initialize.loggerConfig();

        Callable<Runnable> resolveConnectors = initialize.connectors(privateKey);

        ConnectorsService connectorsService = (ConnectorsService) initialize.getServices().get(ConnectorsService.class);
        initialize.families(defaultConfig, connectorsService);
        ServerService serverService = initialize.servers(defaultConfig);
        initialize.networkWhitelist(defaultConfig);
        initialize.dataTransit();
        initialize.magicLink(defaultConfig, serverService);
        initialize.webhooks();

        Runnable connectRemotes = resolveConnectors.execute();
        connectRemotes.run();

        initialize.friendsService();
        initialize.playerService();
        initialize.partyService();
        initialize.dynamicTeleportService();

        initialize.viewportService();

        initialize.events(plugin);
        initialize.commands();

        Flame flame = new Flame(version, initialize.getServices(), defaultConfig.messenger());

        return flame;
    }
}

/**
 * The master initializer class.
 * While some methods depend on resources from other methods,
 * assuming you follow the implementation of each method, it should always successfully build the specified service.
 * <p>
 * This class will mutate the provided services and requestedConnectors lists that are provided to it.
 */
class Initialize {
    private final Tinder api = Tinder.get();
    private final PluginLogger logger = Tinder.get().logger();
    private final Map<Class<? extends Service>, Service> services = new HashMap<>();
    private final List<String> requestedConnectors = new ArrayList<>();

    public Map<Class<? extends Service>, Service> getServices() {
        return this.services;
    }

    public void events(VelocityRustyConnector plugin) {
        EventManager eventManager = api.velocityServer().getEventManager();

        eventManager.register(plugin, new OnPlayerChooseInitialServer());
        eventManager.register(plugin, new OnPlayerChangeServer());
        eventManager.register(plugin, new OnPlayerKicked());
        eventManager.register(plugin, new OnPlayerDisconnect());
    }

    public void commands() {
        CommandManager commandManager = api.velocityServer().getCommandManager();

        commandManager.register(
                commandManager.metaBuilder("rc")
                        .aliases("/rc", "//") // Add slash variants so that they can be used in console as well
                        .build(),
                CommandRusty.create()
        );

        commandManager.unregister("server");
    }

    public String version() {
        try {
            InputStream stream = Tinder.get().resourceAsStream("velocity-plugin.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            JsonObject json = new Gson().fromJson(reader, JsonObject.class);

            stream.close();
            reader.close();
            return json.get("version").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public char[] privateKey() throws IllegalAccessException {
        PrivateKeyConfig privateKeyConfig = PrivateKeyConfig.newConfig(new File(String.valueOf(api.dataFolder()), "private.key"));
        if (!privateKeyConfig.generate())
            throw new IllegalStateException("Unable to load or create private.key!");

        try {
            return privateKeyConfig.get();
        } catch (Exception ignore) {
            throw new IllegalAccessException("There was a fatal error while reading private.key!");
        }
    }

    public char[] memberKey() {
        try {
            MemberKeyConfig memberKeyConfig = MemberKeyConfig.newConfig(new File(api.dataFolder(), "member.key"));
            if (!memberKeyConfig.generate())
                throw new IllegalStateException("Unable to load or create member.key!");
            try {
                char[] memberKey = memberKeyConfig.get();
                if(memberKey.length == 0) return null;
            } catch (Exception ignore) {
                throw new IllegalAccessException("There was a fatal error while reading member.key!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public DefaultConfig defaultConfig() throws IllegalAccessException {
        DefaultConfig defaultConfig = DefaultConfig.newConfig(new File(String.valueOf(api.dataFolder()), "config.yml"), "velocity_config_template.yml");
        if (!defaultConfig.generate())
            throw new IllegalStateException("Unable to load or create config.yml!");
        defaultConfig.register();

        requestedConnectors.add(defaultConfig.messenger());

        return defaultConfig;
    }

    public void loggerConfig() throws IllegalAccessException {
        LoggerConfig loggerConfig = LoggerConfig.newConfig(new File(String.valueOf(api.dataFolder()), "logger.yml"), "velocity_logger_template.yml");

        if (!loggerConfig.generate())
            throw new IllegalStateException("Unable to load or create logger.yml!");
        loggerConfig.register();
        PluginLogger.init(loggerConfig);
    }

    /**
     * Initializes the connectors service.
     * First returns a {@link Callable} which once run, will return a {@link Runnable}.
     * <p>
     * {@link Callable} - Runs linting to only build the connectors actually being referenced by the configs. Returns:
     * <p>
     * a {@link Runnable} - Starts up all connectors and connects them to their remote resources.
     * @param privateKey The plugin's pricate key.
     * @return A runnable which will wrap up the connectors' initialization. Should be run after all other initialization logic has run.
     */
    public Callable<Runnable> connectors(char[] privateKey) {
        logger.send(Component.text("Building Connectors...", NamedTextColor.DARK_GRAY));

        ConnectorsConfig connectorsConfig = ConnectorsConfig.newConfig(new File(String.valueOf(api.dataFolder()), "connectors.yml"), "velocity_connectors_template.yml");
        if (!connectorsConfig.generate())
            throw new IllegalStateException("Unable to load or create connectorsConfig.yml!");
        ConnectorsService connectorsService = connectorsConfig.register(privateKey);
        services.put(ConnectorsService.class, connectorsService);

        logger.send(Component.text("Finished building Connectors.", NamedTextColor.GREEN));

        // Needs to be run after all other services boot so that we can setup the connectors we actually need.
        return () -> {
            logger.send(Component.text("Validating Connector service...", NamedTextColor.DARK_GRAY));

            /*
             * Make sure that configs aren't trying to access connectors which don't exist.
             * Also makes sure that, if there are excess connectors defined, we only load and attempt to boot the ones that are actually being called.
             */
            for (String name : requestedConnectors) {
                logger.send(Component.text(" | Checking and building connector ["+name+"]...", NamedTextColor.DARK_GRAY));

                if(!connectorsService.containsKey(name))
                    throw new RuntimeException("No connector with the name '"+name+"' was found!");

                Connector<Connection> connector = connectorsService.get(name);
                try {
                    connector.connect();
                } catch (ConnectException e) {
                    throw new RuntimeException(e);
                }
            }
            logger.send(Component.text("Finished validating Connector service.", NamedTextColor.GREEN));

            // Needs to run even later to actually boot all the connectors and connect them to their remote resources.
            return () -> {
                logger.send(Component.text("Booting Connectors service...", NamedTextColor.DARK_GRAY));
                connectorsService.messengers().forEach(connector -> {
                    if(connector.connection().isEmpty()) return;
                    MessengerConnection<VelocityMessengerSubscriber> connection = connector.connection().orElseThrow();
                    connection.startListening(VelocityMessengerSubscriber.class);
                });
                connectorsService.storage().forEach(connector -> {
                    if(connector.connection().isPresent()) return;
                    try {
                        connector.connect();
                    } catch (ConnectException e) {
                        throw new RuntimeException(e);
                    }
                });
                logger.send(Component.text("Finished booting Connectors service.", NamedTextColor.GREEN));
            };
        };
    }

    public void families(DefaultConfig defaultConfig, ConnectorsService connectorsService) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
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
                    Connector<?> fetchedConnector = connectorsService.get(familiesConfig.staticFamilyStorage());
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

        {
            logger.send(Component.text(" | Registering load balancing service to the API...", NamedTextColor.DARK_GRAY));
            if (defaultConfig.services_loadBalancing_enabled())
                services.put(LoadBalancingService.class, new LoadBalancingService(familyService.size(), defaultConfig.services_loadBalancing_interval()));
            logger.send(Component.text(" | Finished registering load balancing service to the API.", NamedTextColor.GREEN));
        }

        {
            logger.send(Component.text(" | Resolving family parents...", NamedTextColor.DARK_GRAY));
            familyService.dump().forEach(baseServerFamily -> {
                try {
                    ((PlayerFocusedServerFamily) baseServerFamily).resolveParent();
                } catch (Exception e) {
                    logger.log("There was an issue resolving the parent for " + baseServerFamily.name() + ". " + e.getMessage());
                }
            });
            logger.send(Component.text(" | Finished resolving family parents.", NamedTextColor.GREEN));
        }

        logger.send(Component.text("Finished building families service.", NamedTextColor.GREEN));
    }

    public void networkWhitelist(DefaultConfig defaultConfig) {
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

    public void dataTransit() {
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

    public void magicLink(DefaultConfig defaultConfig, ServerService serverService) {
        logger.send(Component.text("Building magic link service...", NamedTextColor.DARK_GRAY));

        MagicLinkService magicLinkService = new MagicLinkService(3, defaultConfig.services_serverLifecycle_serverPingInterval());
        services.put(MagicLinkService.class, magicLinkService);

        logger.send(Component.text("Finished building magic link service.", NamedTextColor.GREEN));


        logger.send(Component.text("Booting magic link service...", NamedTextColor.DARK_GRAY));
        magicLinkService.startHeartbeat(serverService);
        logger.send(Component.text("Finished booting magic link service.", NamedTextColor.GREEN));
    }

    public ServerService servers(DefaultConfig defaultConfig) {
        logger.send(Component.text("Building server service...", NamedTextColor.DARK_GRAY));

        ServerService.Builder serverServiceBuilder = new ServerService.Builder()
                .setServerTimeout(defaultConfig.services_serverLifecycle_serverTimeout())
                .setServerInterval(defaultConfig.services_serverLifecycle_serverPingInterval());

        ServerService serverService = serverServiceBuilder.build();
        services.put(ServerService.class, serverService);

        logger.send(Component.text("Finished building server service.", NamedTextColor.GREEN));

        return serverService;
    }

    public void webhooks() {
        WebhooksConfig webhooksConfig = WebhooksConfig.newConfig(new File(String.valueOf(api.dataFolder()), "webhooks.yml"), "velocity_webhooks_template.yml");
        if(!webhooksConfig.generate())
            throw new IllegalStateException("Unable to load or create webhooks.yml!");
        webhooksConfig.register();
    }

    public void partyService() {
        try {
            Tinder.get().logger().send(Component.text("Building party service...", NamedTextColor.DARK_GRAY));
            PartyConfig config = PartyConfig.newConfig(new File(String.valueOf(api.dataFolder()), "party.yml"), "velocity_party_template.yml");
            if (!config.generate())
                throw new IllegalStateException("Unable to load or create party.yml!");
            config.register();

            if(!config.isEnabled()) {
                Tinder.get().logger().send(Component.text("The party service wasn't enabled.", NamedTextColor.GRAY));
                return;
            }

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

            PartyService service = new PartyService(settings);

            service.initCommand();

            services.put(PartyService.class, service);
            Tinder.get().logger().send(Component.text("Finished building party service.", NamedTextColor.GREEN));
        } catch (Exception e) {
            logger.send(VelocityLang.BOXED_MESSAGE_COLORED.build(e.getMessage(), NamedTextColor.RED));
            Tinder.get().logger().send(Component.text("The party service wasn't enabled.", NamedTextColor.GRAY));
        }
    }
    public void viewportService() {
        try {
            logger.send(Component.text("Building viewport service...", NamedTextColor.DARK_GRAY));

            ViewportConfig viewportConfig = ViewportConfig.newConfig(new File(String.valueOf(api.dataFolder()), "viewport.yml"), "velocity_viewport_template.yml");
            if (!viewportConfig.generate())
                throw new IllegalStateException("Unable to load or create viewport.yml!");
            viewportConfig.register();

            if(!viewportConfig.isEnabled()) {
                Tinder.get().logger().send(Component.text("The viewport service wasn't enabled.", NamedTextColor.GRAY));
                return;
            }

            logger.send(Component.text(" | Building viewport MySQL...", NamedTextColor.DARK_GRAY));
            StorageConnector<?> connector = (StorageConnector<?>) api.services().connectorsService().get(viewportConfig.storage());
            if(connector == null) throw new NullPointerException("You must define a storage method for viewport!");
            logger.send(Component.text(" | Finished building viewport MySQL.", NamedTextColor.GREEN));

            GatewayService gatewayService = new GatewayService(viewportConfig.getWebsocket_address(), viewportConfig.getRest_address());

            ViewportService service = new ViewportService.Builder()
                    .setStorageConnector(connector)
                    .setGatewayService(gatewayService)
                    .build();

            services.put(ViewportService.class, service);
        } catch (Exception e) {
            logger.send(VelocityLang.BOXED_MESSAGE_COLORED.build(e.getMessage(), NamedTextColor.RED));
            Tinder.get().logger().send(Component.text("The viewport service wasn't enabled.", NamedTextColor.GRAY));
        }
    }

    public void friendsService() {
        try {
            Tinder.get().logger().send(Component.text("Building friends service...", NamedTextColor.DARK_GRAY));

            FriendsConfig config = FriendsConfig.newConfig(new File(String.valueOf(api.dataFolder()), "friends.yml"), "velocity_friends_template.yml");
            if (!config.generate())
                throw new IllegalStateException("Unable to load or create friends.yml!");
            config.register();

            if(!config.isEnabled()) {
                Tinder.get().logger().send(Component.text("The friends service wasn't enabled.", NamedTextColor.GRAY));
                return;
            }

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

            FriendsService service = new FriendsService(settings, (MySQLConnector) connector);

            service.initCommand();

            services.put(FriendsService.class, service);
            Tinder.get().logger().send(Component.text("Finished building friends service.", NamedTextColor.GREEN));
        } catch (Exception e) {
            logger.send(VelocityLang.BOXED_MESSAGE_COLORED.build(e.getMessage(), NamedTextColor.RED));
            Tinder.get().logger().send(Component.text("The friends service wasn't enabled.", NamedTextColor.GRAY));
        }
    }

    public void playerService() {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();
        try {
            FriendsConfig config = FriendsConfig.newConfig(new File(String.valueOf(api.dataFolder()), "friends.yml"), "velocity_friends_template.yml");
            if (!config.generate())
                throw new IllegalStateException("Unable to load or create friends.yml!");
            config.register();

            if(!config.isEnabled()) return;

            logger.send(Component.text(" | Building friends MySQL...", NamedTextColor.DARK_GRAY));
            StorageConnector<?> connector = (StorageConnector<?>) api.services().connectorsService().get(config.storage());
            if(connector == null) throw new NullPointerException("You must define a storage method for viewport!");
            logger.send(Component.text(" | Finished building friends MySQL.", NamedTextColor.GREEN));

            services.put(PlayerService.class, new PlayerService((MySQLConnector) connector));
        } catch (Exception e) {
            logger.send(VelocityLang.BOXED_MESSAGE_COLORED.build(e.getMessage(), NamedTextColor.RED));
        }
    }

    public void dynamicTeleportService() {
        Tinder.get().logger().send(Component.text("Building dynamic teleport service...", NamedTextColor.DARK_GRAY));
        try {
            DynamicTeleportConfig config = DynamicTeleportConfig.newConfig(new File(String.valueOf(api.dataFolder()), "dynamic_teleport.yml"), "velocity_dynamic_teleport_template.yml");
            if (!config.generate())
                throw new IllegalStateException("Unable to load or create dynamic_teleport.yml!");
            config.register();

            if(!config.isEnabled()) {
                Tinder.get().logger().send(Component.text("The dynamic teleport service wasn't enabled.", NamedTextColor.GRAY));
                return;
            }

            DynamicTeleportService dynamicTeleportService = DynamicTeleportService.init(config);

            try {
                dynamicTeleportService.services().tpaService().orElseThrow()
                        .services().tpaCleaningService().startHeartbeat();
                dynamicTeleportService.services().tpaService().orElseThrow().initCommand();
            } catch (Exception ignore) {}

            try {
                dynamicTeleportService.services().hubService().orElseThrow().initCommand();
            } catch (Exception ignore) {}

            try {
                dynamicTeleportService.services().anchorService().orElseThrow().initCommands();
            } catch (Exception ignore) {}

            services.put(DynamicTeleportService.class, dynamicTeleportService);

            Tinder.get().logger().send(Component.text("Finished building dynamic teleport service.", NamedTextColor.GREEN));
        } catch (Exception e) {
            logger.send(VelocityLang.BOXED_MESSAGE_COLORED.build(e.getMessage(), NamedTextColor.RED));
            Tinder.get().logger().send(Component.text("The dynamic teleport service wasn't enabled.", NamedTextColor.GRAY));
        }
    }
}
