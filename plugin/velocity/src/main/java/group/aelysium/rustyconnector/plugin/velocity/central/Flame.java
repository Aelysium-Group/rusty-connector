package group.aelysium.rustyconnector.plugin.velocity.central;

import com.velocitypowered.api.command.CommandManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.velocitypowered.api.event.EventManager;
import group.aelysium.rustyconnector.core.lib.Callable;
import group.aelysium.rustyconnector.core.lib.connectors.Connector;
import group.aelysium.rustyconnector.core.lib.connectors.ConnectorsService;
import group.aelysium.rustyconnector.core.lib.connectors.config.ConnectorsConfig;
import group.aelysium.rustyconnector.core.lib.connectors.implementors.storage.mysql.MySQLConnector;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnection;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnector;
import group.aelysium.rustyconnector.core.lib.connectors.storage.StorageConnector;
import group.aelysium.rustyconnector.core.lib.data_transit.DataTransitService;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.hash.AESCryptor;
import group.aelysium.rustyconnector.core.lib.lang.Lang;
import group.aelysium.rustyconnector.core.lib.lang.LangFileMappings;
import group.aelysium.rustyconnector.core.lib.lang.config.RootLanguageConfig;
import group.aelysium.rustyconnector.core.lib.lang.config.LangService;
import group.aelysium.rustyconnector.core.lib.packets.PacketHandler;
import group.aelysium.rustyconnector.core.lib.packets.PacketOrigin;
import group.aelysium.rustyconnector.core.lib.packets.PacketType;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.core.lib.serviceable.ServiceableService;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.command.CommandRusty;
import group.aelysium.rustyconnector.plugin.velocity.central.config.DefaultConfig;
import group.aelysium.rustyconnector.core.lib.private_key.config.PrivateKeyConfig;
import group.aelysium.rustyconnector.plugin.velocity.central.config.LoggerConfig;
import group.aelysium.rustyconnector.plugin.velocity.events.OnPlayerChangeServer;
import group.aelysium.rustyconnector.plugin.velocity.events.OnPlayerChooseInitialServer;
import group.aelysium.rustyconnector.plugin.velocity.events.OnPlayerDisconnect;
import group.aelysium.rustyconnector.plugin.velocity.events.OnPlayerKicked;
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
import group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.handlers.MagicLinkPingHandler;
import group.aelysium.rustyconnector.plugin.velocity.lib.message.handling.SendPlayerHandler;
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

import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.*;

/**
 * The core RustyConnector kernel.
 * All aspects of the plugin should be accessible from here.
 * If not, check {@link Tinder}.
 */
public class Flame extends ServiceableService<CoreServiceHandler> {
    private int configVersion;
    private String version;
    private List<Component> bootOutput;

    /**
     * The core message backbone where all RC messages are sent through.
     */
    private final MessengerConnector<?> backbone;

    protected Flame(String version, int configVersion, Map<Class<? extends Service>, Service> services, String backboneConnector, List<Component> bootOutput) {
        super(new CoreServiceHandler(services));
        this.backbone = (MessengerConnector<?>) this.services().connectorsService().get(backboneConnector);
        this.version = version;
        this.configVersion = configVersion;
        this.bootOutput = bootOutput;
    }

    public String version() { return this.version; }
    public int configVersion() { return this.configVersion; }

    public MessengerConnector<? extends MessengerConnection> backbone() {
        return this.backbone;
    }

    /**
     * Kill the {@link Flame}.
     * Typically good for if you want to ignite a new one.
     */
    public void exhaust(VelocityRustyConnector plugin) {
        Tinder.get().velocityServer().getEventManager().unregisterListeners(plugin);
        this.bootOutput.clear();
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
    public static Flame fabricateNew(VelocityRustyConnector plugin) throws RuntimeException {
        PluginLogger logger = Tinder.get().logger();
        Initialize initialize = new Initialize();
        try {
            logger.send(Component.text("Initializing 0%...", NamedTextColor.DARK_GRAY));
            String version = initialize.version();
            int configVersion = initialize.configVersion();
            AESCryptor cryptor = initialize.privateKey();
            LangService langService = initialize.lang();

            DefaultConfig defaultConfig = initialize.defaultConfig(langService);
            initialize.loggerConfig(langService);


            logger.send(Component.text("Initializing 10%...", NamedTextColor.DARK_GRAY));
            MessageCacheService messageCacheService = initialize.dataTransit(langService);
            Callable<Runnable> resolveConnectors = initialize.connectors(cryptor, messageCacheService, Tinder.get().logger(), langService);

            logger.send(Component.text("Initializing 20%...", NamedTextColor.DARK_GRAY));
            ConnectorsService connectorsService = (ConnectorsService) initialize.getServices().get(ConnectorsService.class);
            logger.send(Component.text("Initializing 30%...", NamedTextColor.DARK_GRAY));
            initialize.families(defaultConfig, connectorsService, langService);
            logger.send(Component.text("Initializing 40%...", NamedTextColor.DARK_GRAY));
            ServerService serverService = initialize.servers(defaultConfig);
            logger.send(Component.text("Initializing 50%...", NamedTextColor.DARK_GRAY));
            initialize.networkWhitelist(defaultConfig, langService);
            initialize.magicLink(defaultConfig, serverService);
            initialize.webhooks(langService);

            logger.send(Component.text("Initializing 60%...", NamedTextColor.DARK_GRAY));
            Runnable connectRemotes = resolveConnectors.execute();
            connectRemotes.run();
            logger.send(Component.text("Initializing 70%...", NamedTextColor.DARK_GRAY));

            initialize.friendsService(langService);
            initialize.playerService(langService);
            initialize.partyService(langService);
            initialize.dynamicTeleportService(langService);
            logger.send(Component.text("Initializing 80%...", NamedTextColor.DARK_GRAY));

            initialize.viewportService(langService);
            logger.send(Component.text("Initializing 90%...", NamedTextColor.DARK_GRAY));

            Flame flame = new Flame(version, configVersion, initialize.getServices(), defaultConfig.messenger(), initialize.getBootOutput());

            initialize.events(plugin);
            initialize.commands(flame, logger);
            logger.send(Component.text("Initializing 100%...", NamedTextColor.DARK_GRAY));

            return flame;
        } catch (Exception e) {
            logger.send(Component.text("A fatal error occurred! Sending boot output and then the error!").color(NamedTextColor.RED));
            logger.send(Lang.BORDER.color(NamedTextColor.RED));

            initialize.getBootOutput().forEach(logger::send);

            throw new RuntimeException(e);
        }
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
    private final Map<Class<? extends Service>, Service> services = new HashMap<>();
    private final List<String> requestedConnectors = new ArrayList<>();
    private final List<Component> bootOutput = new ArrayList<>();

    public Map<Class<? extends Service>, Service> getServices() {
        return this.services;
    }
    public List<Component> getBootOutput() {
        return this.bootOutput;
    }

    public void events(VelocityRustyConnector plugin) {
        EventManager eventManager = api.velocityServer().getEventManager();

        eventManager.register(plugin, new OnPlayerChooseInitialServer());
        eventManager.register(plugin, new OnPlayerChangeServer());
        eventManager.register(plugin, new OnPlayerKicked());
        eventManager.register(plugin, new OnPlayerDisconnect());
    }

    public void commands(Flame flame, PluginLogger logger) {
        CommandManager commandManager = api.velocityServer().getCommandManager();

        commandManager.register(
                commandManager.metaBuilder("rc")
                        .aliases("/rc", "//") // Add slash variants so that they can be used in console as well
                        .build(),
                CommandRusty.create(flame, logger)
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

    public int configVersion() {
        try {
            InputStream stream = Tinder.get().resourceAsStream("velocity-plugin.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            JsonObject json = new Gson().fromJson(reader, JsonObject.class);

            stream.close();
            reader.close();
            return json.get("config-version").getAsInt();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public AESCryptor privateKey() {
        PrivateKeyConfig privateKeyConfig = PrivateKeyConfig.newConfig(new File(api.dataFolder(), "private.key"));
        if (!privateKeyConfig.generateFilestream(bootOutput))
            throw new IllegalStateException("Unable to load or create private.key!");

        try {
            return privateKeyConfig.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public LangService lang() throws Exception {
        RootLanguageConfig config = new RootLanguageConfig(new File(api.dataFolder(), "language.yml"));
        if (!config.generate(bootOutput))
            throw new IllegalStateException("Unable to load or create language.yml!");
        config.register();

        return LangService.resolveLanguageCode(config.getLanguage(), api.dataFolderPath());
    }

    public DefaultConfig defaultConfig(LangService lang) throws IOException {
        DefaultConfig defaultConfig = new DefaultConfig(new File(api.dataFolder(), "config.yml"));

        if (!defaultConfig.generate(bootOutput, lang, LangFileMappings.VELOCITY_CONFIG_TEMPLATE))
            throw new IllegalStateException("Unable to load or create config.yml!");
        defaultConfig.register(this.configVersion());

        requestedConnectors.add(defaultConfig.messenger());

        return defaultConfig;
    }

    public void loggerConfig(LangService lang) throws IOException {
        LoggerConfig loggerConfig = LoggerConfig.newConfig(new File(api.dataFolder(), "logger.yml"));

        if (!loggerConfig.generate(bootOutput, lang, LangFileMappings.VELOCITY_LOGGER_TEMPLATE))
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
     * @param cryptor The plugin's cryptor.
     * @return A runnable which will wrap up the connectors' initialization. Should be run after all other initialization logic has run.
     */
    public Callable<Runnable> connectors(AESCryptor cryptor, MessageCacheService cacheService, PluginLogger logger, LangService lang) throws IOException {
        bootOutput.add(Component.text("Building Connectors...", NamedTextColor.DARK_GRAY));

        ConnectorsConfig connectorsConfig = new ConnectorsConfig(new File(api.dataFolder(), "connectors.yml"));
        if (!connectorsConfig.generate(bootOutput, lang, LangFileMappings.VELOCITY_CONNECTORS_TEMPLATE))
            throw new IllegalStateException("Unable to load or create connectors.yml!");
        ConnectorsService connectorsService = connectorsConfig.register(cryptor, true, true, PacketOrigin.PROXY);
        services.put(ConnectorsService.class, connectorsService);

        bootOutput.add(Component.text("Finished building Connectors.", NamedTextColor.GREEN));

        // Needs to be run after all other services boot so that we can set up the connectors that are actually called by code.
        return () -> {
            bootOutput.add(Component.text("Validating Connector service...", NamedTextColor.DARK_GRAY));

            /*
             * Make sure that configs aren't trying to access connectors which don't exist.
             * Also makes sure that, if there are excess connectors defined, we only load and attempt to boot the ones that are actually being called.
             */
            for (String name : requestedConnectors) {
                bootOutput.add(Component.text(" | Checking and building connector ["+name+"]...", NamedTextColor.DARK_GRAY));

                if(!connectorsService.containsKey(name))
                    throw new RuntimeException("No connector with the name '"+name+"' was found!");

                Connector<?> connector = connectorsService.get(name);
                try {
                    connector.connect();
                    bootOutput.add(Component.text(" | Finished building connector ["+name+"].", NamedTextColor.GREEN));
                } catch (ConnectException e) {
                    throw new RuntimeException(e);
                }
            }
            bootOutput.add(Component.text("Finished validating Connector service.", NamedTextColor.GREEN));

            // Needs to run even later to actually boot all the connectors and connect them to their remote resources.
            return () -> {
                bootOutput.add(Component.text("Booting Connectors service...", NamedTextColor.DARK_GRAY));

                Map<PacketType.Mapping, PacketHandler> handlers = new HashMap<>();
                handlers.put(PacketType.PING, new MagicLinkPingHandler());
                handlers.put(PacketType.SEND_PLAYER, new SendPlayerHandler());

                connectorsService.messengers().forEach(connector -> {
                    if(connector.connection().isEmpty()) return;
                    MessengerConnection connection = connector.connection().orElseThrow();
                    connection.startListening(cacheService, logger, handlers);
                });
                connectorsService.storage().forEach(connector -> {
                    if(connector.connection().isPresent()) return;
                    try {
                        connector.connect();
                    } catch (ConnectException e) {
                        throw new RuntimeException(e);
                    }
                });
                bootOutput.add(Component.text("Finished booting Connectors service.", NamedTextColor.GREEN));
            };
        };
    }

    public void families(DefaultConfig defaultConfig, ConnectorsService connectorsService, LangService lang) throws Exception {
        bootOutput.add(Component.text("Building families service...", NamedTextColor.DARK_GRAY));

        FamiliesConfig familiesConfig = new FamiliesConfig(new File(api.dataFolder(), "families.yml"));
        if (!familiesConfig.generate(bootOutput, lang, LangFileMappings.VELOCITY_FAMILIES_TEMPLATE))
            throw new IllegalStateException("Unable to load or create families.yml!");
        familiesConfig.register();

        bootOutput.add(Component.text(" | Registering family service to the API...", NamedTextColor.DARK_GRAY));
        FamilyService familyService = new FamilyService(familiesConfig.shouldRootFamilyCatchDisconnectingPlayers());
        services.put(FamilyService.class, familyService);
        bootOutput.add(Component.text(" | Finished registering family service to API.", NamedTextColor.GREEN));

        {
            bootOutput.add(Component.text(" | Building families...", NamedTextColor.DARK_GRAY));
            for (String familyName : familiesConfig.scalarFamilies()) {
                familyService.add(ScalarServerFamily.init(familyName, bootOutput, lang));
                bootOutput.add(Component.text(" | Registered family: "+familyName, NamedTextColor.YELLOW));
            }
            for (String familyName : familiesConfig.staticFamilies()) {
                familyService.add(StaticServerFamily.init(familyName, bootOutput, requestedConnectors, connectorsService, lang));
                bootOutput.add(Component.text(" | Registered family: "+familyName, NamedTextColor.YELLOW));
            }
            bootOutput.add(Component.text(" | Finished building families.", NamedTextColor.GREEN));
        }

        {
            bootOutput.add(Component.text(" | Building root family...", NamedTextColor.DARK_GRAY));

            RootServerFamily rootFamily = RootServerFamily.init(familiesConfig.rootFamilyName(), bootOutput, lang);
            familyService.setRootFamily(rootFamily);
            bootOutput.add(Component.text(" | Registered root family: "+rootFamily.name(), NamedTextColor.YELLOW));

            bootOutput.add(Component.text(" | Finished building root family.", NamedTextColor.GREEN));
        }

        {
            bootOutput.add(Component.text(" | Registering load balancing service to the API...", NamedTextColor.DARK_GRAY));
            if (defaultConfig.services_loadBalancing_enabled())
                services.put(LoadBalancingService.class, new LoadBalancingService(familyService.size(), defaultConfig.services_loadBalancing_interval()));
            bootOutput.add(Component.text(" | Finished registering load balancing service to the API.", NamedTextColor.GREEN));
        }

        {
            bootOutput.add(Component.text(" | Resolving family parents...", NamedTextColor.DARK_GRAY));
            familyService.dump().forEach(baseServerFamily -> {
                try {
                    ((PlayerFocusedServerFamily) baseServerFamily).resolveParent();
                } catch (Exception e) {
                    bootOutput.add(Component.text("There was an issue resolving the parent for " + baseServerFamily.name() + ". " + e.getMessage()));
                }
            });
            bootOutput.add(Component.text(" | Finished resolving family parents.", NamedTextColor.GREEN));
        }

        bootOutput.add(Component.text("Finished building families service.", NamedTextColor.GREEN));
    }

    public void networkWhitelist(DefaultConfig defaultConfig, LangService lang) throws IOException {
        bootOutput.add(Component.text("Registering whitelist service to the API...", NamedTextColor.DARK_GRAY));
        WhitelistService whitelistService = new WhitelistService();
        services.put(WhitelistService.class, whitelistService);
        bootOutput.add(Component.text("Finished registering whitelist service to the API.", NamedTextColor.GREEN));

        bootOutput.add(Component.text("Building proxy whitelist...", NamedTextColor.DARK_GRAY));
        if (defaultConfig.whitelist_enabled()) {
            whitelistService.setProxyWhitelist(Whitelist.init(defaultConfig.whitelist_name(), bootOutput, lang));
            bootOutput.add(Component.text("Finished building proxy whitelist.", NamedTextColor.GREEN));
        } else
            bootOutput.add(Component.text("Finished building proxy whitelist. No whitelist is enabled for the proxy.", NamedTextColor.GREEN));
    }

    public MessageCacheService dataTransit(LangService lang) throws IOException {
        bootOutput.add(Component.text("Building data transit service...", NamedTextColor.DARK_GRAY));
        // Setup Data Transit
        DataTransitConfig dataTransitConfig = new DataTransitConfig(new File(api.dataFolder(), "data_transit.yml"));
        if (!dataTransitConfig.generate(bootOutput, lang, LangFileMappings.VELOCITY_DATA_TRANSIT_TEMPLATE))
            throw new IllegalStateException("Unable to load or create data-transit.yml!");
        dataTransitConfig.register();



        bootOutput.add(Component.text(" | Building message cache service...", NamedTextColor.DARK_GRAY));
        MessageCacheService messageCacheService = new MessageCacheService(dataTransitConfig.cache_size(), dataTransitConfig.cache_ignoredStatuses(), dataTransitConfig.cache_ignoredTypes());
        services.put(MessageCacheService.class, messageCacheService);
        bootOutput.add(Component.text(" | Message cache size set to: "+dataTransitConfig.cache_size(), NamedTextColor.YELLOW));
        bootOutput.add(Component.text(" | Finished building message cache service.", NamedTextColor.GREEN));



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

        bootOutput.add(Component.text("Finished building data transit service.", NamedTextColor.GREEN));

        return messageCacheService;
    }

    public void magicLink(DefaultConfig defaultConfig, ServerService serverService) {
        bootOutput.add(Component.text("Building magic link service...", NamedTextColor.DARK_GRAY));

        MagicLinkService magicLinkService = new MagicLinkService(3, defaultConfig.services_serverLifecycle_serverPingInterval());
        services.put(MagicLinkService.class, magicLinkService);

        bootOutput.add(Component.text("Finished building magic link service.", NamedTextColor.GREEN));


        bootOutput.add(Component.text("Booting magic link service...", NamedTextColor.DARK_GRAY));
        magicLinkService.startHeartbeat(serverService);
        bootOutput.add(Component.text("Finished booting magic link service.", NamedTextColor.GREEN));
    }

    public ServerService servers(DefaultConfig defaultConfig) {
        bootOutput.add(Component.text("Building server service...", NamedTextColor.DARK_GRAY));

        ServerService.Builder serverServiceBuilder = new ServerService.Builder()
                .setServerTimeout(defaultConfig.services_serverLifecycle_serverTimeout())
                .setServerInterval(defaultConfig.services_serverLifecycle_serverPingInterval());

        ServerService serverService = serverServiceBuilder.build();
        services.put(ServerService.class, serverService);

        bootOutput.add(Component.text("Finished building server service.", NamedTextColor.GREEN));

        return serverService;
    }

    public void webhooks(LangService lang) throws IOException {
        WebhooksConfig webhooksConfig = new WebhooksConfig(new File(api.dataFolder(), "webhooks.yml"));
        if(!webhooksConfig.generate(bootOutput, lang, LangFileMappings.VELOCITY_WEBHOOKS_TEMPLATE))
            throw new IllegalStateException("Unable to load or create webhooks.yml!");
        webhooksConfig.register();
    }

    public void partyService(LangService lang) {
        try {
            bootOutput.add(Component.text("Building party service...", NamedTextColor.DARK_GRAY));
            PartyConfig config = new PartyConfig(new File(api.dataFolder(), "party.yml"));
            if (!config.generate(bootOutput, lang, LangFileMappings.VELOCITY_PARTY_TEMPLATE))
                throw new IllegalStateException("Unable to load or create party.yml!");
            config.register();

            if(!config.isEnabled()) {
                bootOutput.add(Component.text("The party service wasn't enabled.", NamedTextColor.GRAY));
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
            bootOutput.add(Component.text("Finished building party service.", NamedTextColor.GREEN));
        } catch (Exception e) {
            bootOutput.add(VelocityLang.BOXED_MESSAGE_COLORED.build(e.getMessage(), NamedTextColor.RED));
            bootOutput.add(Component.text("The party service wasn't enabled.", NamedTextColor.GRAY));
        }
    }
    public void viewportService(LangService lang) {
        try {
            bootOutput.add(Component.text("Building viewport service...", NamedTextColor.DARK_GRAY));

            ViewportConfig viewportConfig = new ViewportConfig(new File(api.dataFolder(), "viewport.yml"));
            if (!viewportConfig.generate(bootOutput, lang, LangFileMappings.VELOCITY_VIEWPORT_TEMPLATE))
                throw new IllegalStateException("Unable to load or create viewport.yml!");
            viewportConfig.register();

            if(!viewportConfig.isEnabled()) {
                bootOutput.add(Component.text("The viewport service wasn't enabled.", NamedTextColor.GRAY));
                return;
            }

            bootOutput.add(Component.text(" | Building viewport MySQL...", NamedTextColor.DARK_GRAY));
            StorageConnector<?> connector = (StorageConnector<?>) api.services().connectorsService().get(viewportConfig.storage());
            if(connector == null) throw new NullPointerException("You must define a storage method for viewport!");
            requestedConnectors.add(viewportConfig.storage());
            bootOutput.add(Component.text(" | Finished building viewport MySQL.", NamedTextColor.GREEN));

            GatewayService gatewayService = new GatewayService(viewportConfig.getWebsocket_address(), viewportConfig.getRest_address());

            ViewportService service = new ViewportService.Builder()
                    .setStorageConnector(connector)
                    .setGatewayService(gatewayService)
                    .build();

            services.put(ViewportService.class, service);
        } catch (Exception e) {
            bootOutput.add(VelocityLang.BOXED_MESSAGE_COLORED.build(e.getMessage(), NamedTextColor.RED));
            bootOutput.add(Component.text("The viewport service wasn't enabled.", NamedTextColor.GRAY));
        }
    }

    public void friendsService(LangService lang) {
        try {
            bootOutput.add(Component.text("Building friends service...", NamedTextColor.DARK_GRAY));

            FriendsConfig config = new FriendsConfig(new File(api.dataFolder(), "friends.yml"));
            if (!config.generate(bootOutput, lang, LangFileMappings.VELOCITY_FRIENDS_TEMPLATE))
                throw new IllegalStateException("Unable to load or create friends.yml!");
            config.register();

            if(!config.isEnabled()) {
                bootOutput.add(Component.text("The friends service wasn't enabled.", NamedTextColor.GRAY));
                return;
            }

            FriendsService.FriendsSettings settings = new FriendsService.FriendsSettings(
                    config.getMaxFriends(),
                    config.isSendNotifications(),
                    config.isShowFamilies(),
                    config.isAllowMessaging()
            );

            bootOutput.add(Component.text(" | Building friends connector...", NamedTextColor.DARK_GRAY));
            StorageConnector<?> connector = (StorageConnector<?>) api.services().connectorsService().get(config.storage());
            if(connector == null) throw new NullPointerException("You must define a storage method for the friends service!");
            requestedConnectors.add(config.storage());
            bootOutput.add(Component.text(" | Finished building friends connector.", NamedTextColor.GREEN));

            FriendsService service = new FriendsService(settings, (MySQLConnector) connector);

            service.initCommand();

            services.put(FriendsService.class, service);
            bootOutput.add(Component.text("Finished building friends service.", NamedTextColor.GREEN));
        } catch (Exception e) {
            bootOutput.add(VelocityLang.BOXED_MESSAGE_COLORED.build(e.getMessage(), NamedTextColor.RED));
            bootOutput.add(Component.text("The friends service wasn't enabled.", NamedTextColor.GRAY));
        }
    }

    public void playerService(LangService lang) throws Exception {
        FriendsConfig config = new FriendsConfig(new File(api.dataFolder(), "friends.yml"));
        if (!config.generate(bootOutput, lang, LangFileMappings.VELOCITY_FRIENDS_TEMPLATE))
            throw new IllegalStateException("Unable to load or create friends.yml!");
        config.register();

        if(!config.isEnabled()) return;

        bootOutput.add(Component.text(" | Building friends MySQL...", NamedTextColor.DARK_GRAY));
        StorageConnector<?> connector = (StorageConnector<?>) api.services().connectorsService().get(config.storage());
        if(connector == null) throw new NullPointerException("You must define a storage method for the friends service!");
        requestedConnectors.add(config.storage());
        bootOutput.add(Component.text(" | Finished building friends MySQL.", NamedTextColor.GREEN));

        services.put(PlayerService.class, new PlayerService((MySQLConnector) connector));
    }

    public void dynamicTeleportService(LangService lang) {
        bootOutput.add(Component.text("Building dynamic teleport service...", NamedTextColor.DARK_GRAY));
        try {
            DynamicTeleportConfig config = new DynamicTeleportConfig(new File(api.dataFolder(), "dynamic_teleport.yml"));
            if (!config.generate(bootOutput, lang, LangFileMappings.VELOCITY_DYNAMIC_TELEPORT_TEMPLATE))
                throw new IllegalStateException("Unable to load or create dynamic_teleport.yml!");
            config.register();

            if(!config.isEnabled()) {
                bootOutput.add(Component.text("The dynamic teleport service wasn't enabled.", NamedTextColor.GRAY));
                return;
            }

            DynamicTeleportService dynamicTeleportService = DynamicTeleportService.init(config, bootOutput);

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

            bootOutput.add(Component.text("Finished building dynamic teleport service.", NamedTextColor.GREEN));
        } catch (Exception e) {
            bootOutput.add(VelocityLang.BOXED_MESSAGE_COLORED.build(e.getMessage(), NamedTextColor.RED));
            bootOutput.add(Component.text("The dynamic teleport service wasn't enabled.", NamedTextColor.GRAY));
        }
    }
}
