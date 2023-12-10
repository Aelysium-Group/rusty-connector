package group.aelysium.rustyconnector.plugin.velocity.central;

import com.velocitypowered.api.command.CommandManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.velocitypowered.api.event.EventManager;
import group.aelysium.rustyconnector.core.mcloader.lib.dynamic_teleport.handlers.CoordinateRequestListener;
import group.aelysium.rustyconnector.core.mcloader.lib.magic_link.handlers.MagicLink_PingResponseListener;
import group.aelysium.rustyconnector.core.mcloader.lib.ranked_game_interface.handlers.RankedGameAssociateListener;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.RankedFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.config.MagicLinkConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.packet_handlers.RankedGameEndListener;
import group.aelysium.rustyconnector.toolkit.velocity.central.VelocityFlame;
import group.aelysium.rustyconnector.toolkit.velocity.friends.FriendsServiceSettings;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;
import group.aelysium.rustyconnector.toolkit.velocity.util.Version;
import group.aelysium.rustyconnector.core.lib.messenger.config.ConnectorsConfig;
import group.aelysium.rustyconnector.core.lib.messenger.implementors.redis.RedisConnection;
import group.aelysium.rustyconnector.core.lib.messenger.implementors.redis.RedisConnector;
import group.aelysium.rustyconnector.core.lib.data_transit.DataTransitService;
import group.aelysium.rustyconnector.core.lib.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.crypt.AESCryptor;
import group.aelysium.rustyconnector.core.lib.key.config.MemberKeyConfig;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketOrigin;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.config.DefaultConfig;
import group.aelysium.rustyconnector.core.lib.key.config.PrivateKeyConfig;
import group.aelysium.rustyconnector.plugin.velocity.central.config.LoggerConfig;
import group.aelysium.rustyconnector.plugin.velocity.events.OnPlayerChangeServer;
import group.aelysium.rustyconnector.plugin.velocity.events.OnPlayerChooseInitialServer;
import group.aelysium.rustyconnector.plugin.velocity.events.OnPlayerDisconnect;
import group.aelysium.rustyconnector.plugin.velocity.events.OnPlayerKicked;
import group.aelysium.rustyconnector.plugin.velocity.lib.data_transit.config.DataTransitConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.config.DynamicTeleportConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.TPAService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.config.FamiliesConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.RootFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.ScalarFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.static_family.StaticFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.config.FriendsConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancingService;
import group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.MagicLinkService;
import group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.packet_handlers.MagicLinkPingListener;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.packet_handlers.LockServerListener;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.packet_handlers.SendPlayerListener;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.packet_handlers.UnlockServerListener;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import group.aelysium.rustyconnector.toolkit.velocity.parties.PartyServiceSettings;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.config.PartyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.PlayerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.MySQLStorage;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.config.WebhooksConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.WhitelistService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.*;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector.inject;

/**
 * The core RustyConnector kernel.
 * All aspects of the plugin should be accessible from here.
 * If not, check {@link Tinder}.
 */
public class Flame extends VelocityFlame<CoreServiceHandler, RedisConnector> {
    private final int configVersion;
    private final Version version;
    private final List<Component> bootOutput;
    private final char[] memberKey;

    protected Flame(Version version, int configVersion, char[] memberKey, Map<Class<? extends Service>, Service> services, List<Component> bootOutput) {
        super(new CoreServiceHandler(services));
        this.version = version;
        this.configVersion = configVersion;
        this.bootOutput = bootOutput;
        this.memberKey = memberKey;
    }

    public Version version() {
        return this.version;
    }


    public int configVersion() { return this.configVersion; }
    private Optional<char[]> memberKey() {
        if (this.memberKey.length == 0) return Optional.empty();
        return Optional.of(this.memberKey);
    }
    public List<Component> bootLog() { return this.bootOutput; }

    public RedisConnector backbone() {
        return this.services().messenger();
    }

    @Override
    public void kill() {
        super.kill();
        this.bootOutput.clear();
    }

    /**
     * Fabricates a new RustyConnector core and returns it.
     * @return A new RustyConnector {@link Flame}.
     */
    public static Flame fabricateNew(VelocityRustyConnector plugin, LangService langService) throws RuntimeException {
        PluginLogger logger = Tinder.get().logger();
        Initialize initialize = new Initialize();
        logger.send(Component.text("Booting RustyConnector...", NamedTextColor.GREEN));
        logger.send(Component.text("Initializing 0%...", NamedTextColor.DARK_GRAY));

        try {
            String version = initialize.version();
            int configVersion = initialize.configVersion();
            AESCryptor cryptor = initialize.privateKey();
            Optional<char[]> memberKey = initialize.memberKey();

            logger.send(Component.text("Initializing 10%...", NamedTextColor.DARK_GRAY));
            DefaultConfig defaultConfig = initialize.defaultConfig(langService);
            initialize.loggerConfig(langService);

            MessageCacheService messageCacheService = initialize.dataTransit(langService);

            logger.send(Component.text("Initializing 20%...", NamedTextColor.DARK_GRAY));
            DependencyInjector.DI2<RedisConnector, MySQLStorage> connectors = initialize.connectors(inject(cryptor, messageCacheService, Tinder.get().logger(), langService));

            logger.send(Component.text("Initializing 30%...", NamedTextColor.DARK_GRAY));
            FamilyService familyService = initialize.families(inject(defaultConfig, langService, connectors.d2()));
            initialize.magicConfigs(inject(familyService, langService));
            logger.send(Component.text("Initializing 40%...", NamedTextColor.DARK_GRAY));
            ServerService serverService = initialize.servers(defaultConfig);
            logger.send(Component.text("Initializing 50%...", NamedTextColor.DARK_GRAY));
            initialize.networkWhitelist(inject(defaultConfig, langService));
            logger.send(Component.text("Initializing 60%...", NamedTextColor.DARK_GRAY));
            initialize.magicLink(inject(defaultConfig, serverService));
            initialize.webhooks(langService);

            logger.send(Component.text("Initializing 70%...", NamedTextColor.DARK_GRAY));
            initialize.friendsService(inject(connectors.d2(), langService));
            initialize.playerService(inject(connectors.d2(), langService));
            logger.send(Component.text("Initializing 80%...", NamedTextColor.DARK_GRAY));
            initialize.partyService(langService);
            initialize.dynamicTeleportService(inject(familyService, serverService, langService));

            logger.send(Component.text("Initializing 90%...", NamedTextColor.DARK_GRAY));

            Flame flame = new Flame(new Version(version), configVersion, memberKey.orElse(new char[0]), initialize.getServices(), initialize.getBootOutput());

            initialize.events(plugin);
            initialize.commands(inject(flame, logger, messageCacheService));
            logger.send(Component.text("Initializing 100%...", NamedTextColor.DARK_GRAY));

            return flame;
        } catch (Exception e) {
            logger.send(Component.text("A fatal error occurred! Sending boot output and then the error!").color(NamedTextColor.RED));
            logger.send(ProxyLang.BORDER.color(NamedTextColor.RED));

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
 * This class will mutate the provided services lists that are provided to it.
 */
class Initialize {
    private final Tinder api = Tinder.get();
    private final Map<Class<? extends Service>, Service> services = new HashMap<>();
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

    public void commands(DependencyInjector.DI3<Flame, PluginLogger, MessageCacheService> dependencies) {
        CommandManager commandManager = api.velocityServer().getCommandManager();

        commandManager.register(
                commandManager.metaBuilder("rc")
                        .aliases("/rc", "//") // Add slash variants so that they can be used in console as well
                        .build(),
                CommandRusty.create(dependencies)
        );

        commandManager.unregister("server");
    }

    public String version() {
        try {
            InputStream stream = Tinder.resourceAsStream("velocity-plugin.json");
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
            InputStream stream = Tinder.resourceAsStream("velocity-plugin.json");
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
        PrivateKeyConfig config = new PrivateKeyConfig(new File(api.dataFolder(), "private.key"));
        try {
            return config.get(bootOutput);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<char[]> memberKey() {
        MemberKeyConfig config = new MemberKeyConfig(new File(api.dataFolder(), "member.key"));
        if (!config.generateFilestream(bootOutput))
            throw new IllegalStateException("Unable to load or create private.key!");

        try {
            return Optional.of(config.get());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public DefaultConfig defaultConfig(LangService lang) throws IOException {
        DefaultConfig defaultConfig = new DefaultConfig(new File(api.dataFolder(), "config.yml"));

        if (!defaultConfig.generate(bootOutput, lang, LangFileMappings.VELOCITY_CONFIG_TEMPLATE))
            throw new IllegalStateException("Unable to load or create config.yml!");
        defaultConfig.register(this.configVersion());

        return defaultConfig;
    }

    public void loggerConfig(LangService lang) throws IOException {
        LoggerConfig loggerConfig = LoggerConfig.newConfig(new File(api.dataFolder(), "logger.yml"));

        if (!loggerConfig.generate(bootOutput, lang, LangFileMappings.VELOCITY_LOGGER_TEMPLATE))
            throw new IllegalStateException("Unable to load or create logger.yml!");
        loggerConfig.register();
        PluginLogger.init(loggerConfig);
    }

    public DependencyInjector.DI2<RedisConnector, MySQLStorage> connectors(DependencyInjector.DI4<AESCryptor, MessageCacheService, PluginLogger, LangService> dependencies) throws IOException, SQLException {
        bootOutput.add(Component.text("Building Connectors...", NamedTextColor.DARK_GRAY));

        ConnectorsConfig config = new ConnectorsConfig(new File(api.dataFolder(), "connectors.yml"));
        if (!config.generate(bootOutput, dependencies.d4(), LangFileMappings.VELOCITY_CONNECTORS_TEMPLATE))
            throw new IllegalStateException("Unable to load or create connectors.yml!");
        config.register(true, true);


        RedisConnector.RedisConnectorSpec spec = new RedisConnector.RedisConnectorSpec(
                PacketOrigin.PROXY,
                config.getRedis_address(),
                config.getRedis_user(),
                config.getRedis_protocol(),
                config.getRedis_dataChannel()
        );
        RedisConnector messenger = RedisConnector.create(dependencies.d1(), spec);
        services.put(RedisConnector.class, messenger);
        bootOutput.add(Component.text("Booting Messenger...", NamedTextColor.DARK_GRAY));

        messenger.connect();
        RedisConnection connection = messenger.connection().orElseThrow();

        connection.listen(new MagicLinkPingListener(this.api));
        connection.listen(new SendPlayerListener(this.api));
        connection.listen(new LockServerListener(this.api));
        connection.listen(new UnlockServerListener(this.api));

        connection.listen(new RankedGameEndListener(this.api));

        connection.startListening(dependencies.d2(), dependencies.d3(), null);
        bootOutput.add(Component.text("Finished booting Messenger.", NamedTextColor.GREEN));

        bootOutput.add(Component.text("Booting MicroStream MariaDB driver...", NamedTextColor.DARK_GRAY));
        MySQLStorage storage = MySQLStorage.create(
                config.getMysql_address(),
                config.getMysql_user(),
                config.getMysql_database()
        );
        services.put(MySQLStorage.class, storage);
        bootOutput.add(Component.text("Finished booting MariaDB driver.", NamedTextColor.GREEN));

        bootOutput.add(Component.text("Finished building Connectors.", NamedTextColor.GREEN));
        return DependencyInjector.inject(messenger, storage);
    }

    public FamilyService families(DependencyInjector.DI3<DefaultConfig, LangService, MySQLStorage> dependencies) throws Exception {
        bootOutput.add(Component.text("Building families service...", NamedTextColor.DARK_GRAY));

        FamiliesConfig familiesConfig = new FamiliesConfig(new File(api.dataFolder(), "families.yml"));
        if (!familiesConfig.generate(bootOutput, dependencies.d2(), LangFileMappings.VELOCITY_FAMILIES_TEMPLATE))
            throw new IllegalStateException("Unable to load or create families.yml!");
        familiesConfig.register();

        bootOutput.add(Component.text(" | Registering family service to the API...", NamedTextColor.DARK_GRAY));
        FamilyService familyService = new FamilyService(familiesConfig.shouldRootFamilyCatchDisconnectingPlayers());
        services.put(FamilyService.class, familyService);
        bootOutput.add(Component.text(" | Finished registering family service to API.", NamedTextColor.GREEN));

        WhitelistService whitelistService = new WhitelistService();
        services.put(WhitelistService.class, whitelistService);

        {
            bootOutput.add(Component.text(" | Building families...", NamedTextColor.DARK_GRAY));
            for (String familyName : familiesConfig.scalarFamilies()) {
                familyService.add(ScalarFamily.init(inject(bootOutput, dependencies.d2(), whitelistService), familyName));
                bootOutput.add(Component.text(" | Registered family: "+familyName, NamedTextColor.YELLOW));
            }
            for (String familyName : familiesConfig.staticFamilies()) {
                familyService.add(StaticFamily.init(inject(bootOutput, dependencies.d2(), dependencies.d3(), whitelistService), familyName));
                bootOutput.add(Component.text(" | Registered family: "+familyName, NamedTextColor.YELLOW));
            }
            for (String familyName : familiesConfig.rankedFamilies()) {
                RankedFamily family = RankedFamily.init(inject(bootOutput, dependencies.d2(), dependencies.d3(), whitelistService), familyName);
                familyService.add(family);
                bootOutput.add(Component.text(" | Registered family: "+familyName, NamedTextColor.YELLOW));
                bootOutput.add(Component.text("   | Starting "+familyName+"'s matchmaker...", NamedTextColor.DARK_GRAY));
                family.start();
                bootOutput.add(Component.text("   | Started "+familyName+"'s matchmaker.", NamedTextColor.YELLOW));
            }
            bootOutput.add(Component.text(" | Finished building families.", NamedTextColor.GREEN));
        }

        {
            bootOutput.add(Component.text(" | Building root family...", NamedTextColor.DARK_GRAY));

            RootFamily rootFamily = RootFamily.init(inject(bootOutput, dependencies.d2(), whitelistService), familiesConfig.rootFamilyName());
            familyService.setRootFamily(rootFamily);
            bootOutput.add(Component.text(" | Registered root family: "+rootFamily.id(), NamedTextColor.YELLOW));

            bootOutput.add(Component.text(" | Finished building root family.", NamedTextColor.GREEN));
        }

        {
            bootOutput.add(Component.text(" | Registering load balancing service to the API...", NamedTextColor.DARK_GRAY));
            LoadBalancingService clock = new LoadBalancingService(familyService.size(), LiquidTimestamp.from(20, TimeUnit.SECONDS));
            services.put(LoadBalancingService.class, clock);
            clock.init(inject(familyService, this.api.logger()));
            bootOutput.add(Component.text(" | Finished registering load balancing service to the API.", NamedTextColor.GREEN));
        }

        bootOutput.add(Component.text("Finished building families service.", NamedTextColor.GREEN));
        return familyService;
    }

    public void magicConfigs(DependencyInjector.DI2<FamilyService, LangService> dependencies) throws Exception {
        bootOutput.add(Component.text("Validating Magic Configs...", NamedTextColor.DARK_GRAY));

        File folder = new File(api.dataFolder(), "/magic_configs");
        if (!folder.exists() && !folder.isDirectory())
            folder.mkdirs();
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        try {
            if (files.length == 0) {
                files = new File[]{new File("default.yml")};
            }
        } catch (Exception ignore) {
            files = new File[]{new File("default.yml")};
        }

        for (File file : files) {
            MagicLinkConfig magicLinkConfig = new MagicLinkConfig(api.dataFolder(), file.getName());
            if (!magicLinkConfig.generate(bootOutput, dependencies.d2(), LangFileMappings.VELOCITY_MAGIC_CONFIG_TEMPLATE))
                throw new IllegalStateException("Unable to load or create magic_config.yml!");
            magicLinkConfig.register();

            if(dependencies.d1().dump().stream().noneMatch(family -> family.id().equals(magicLinkConfig.family())))
                throw new NullPointerException("The magic config `" + file.getName() + "` is pointing to a family: `" + magicLinkConfig.family() + "`, which doesn't exist!");
        }

        bootOutput.add(Component.text("Magic Configs have been validated!", NamedTextColor.GREEN));
    }

    public void networkWhitelist(DependencyInjector.DI2<DefaultConfig, LangService> dependencies) throws IOException {
        bootOutput.add(Component.text("Registering whitelist service to the API...", NamedTextColor.DARK_GRAY));
        WhitelistService whitelistService = new WhitelistService();
        services.put(WhitelistService.class, whitelistService);
        bootOutput.add(Component.text("Finished registering whitelist service to the API.", NamedTextColor.GREEN));

        bootOutput.add(Component.text("Building proxy whitelist...", NamedTextColor.DARK_GRAY));
        if (dependencies.d1().whitelist_enabled()) {
            whitelistService.setProxyWhitelist(Whitelist.init(inject(bootOutput, dependencies.d2(), whitelistService), dependencies.d1().whitelist_name()));
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

    public void magicLink(DependencyInjector.DI2<DefaultConfig, ServerService> dependencies) {
        bootOutput.add(Component.text("Building magic link service...", NamedTextColor.DARK_GRAY));

        MagicLinkService magicLinkService = new MagicLinkService(3, dependencies.d1().magicLink_serverPingInterval());
        services.put(MagicLinkService.class, magicLinkService);

        bootOutput.add(Component.text("Finished building magic link service.", NamedTextColor.GREEN));


        bootOutput.add(Component.text("Booting magic link service...", NamedTextColor.DARK_GRAY));
        magicLinkService.startHeartbeat(dependencies.d2());
        bootOutput.add(Component.text("Finished booting magic link service.", NamedTextColor.GREEN));
    }

    public ServerService servers(DefaultConfig defaultConfig) {
        bootOutput.add(Component.text("Building server service...", NamedTextColor.DARK_GRAY));

        ServerService.Builder serverServiceBuilder = new ServerService.Builder()
                .setServerTimeout(defaultConfig.magicLink_serverTimeout())
                .setServerInterval(defaultConfig.magicLink_serverPingInterval());

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

            PartyServiceSettings settings = new PartyServiceSettings(
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

            service.initCommand(bootOutput);

            services.put(PartyService.class, service);
            bootOutput.add(Component.text("Finished building party service.", NamedTextColor.GREEN));
        } catch (Exception e) {
            bootOutput.add(ProxyLang.BOXED_MESSAGE_COLORED.build(e.getMessage(), NamedTextColor.RED));
            bootOutput.add(Component.text("The party service wasn't enabled.", NamedTextColor.GRAY));
        }
    }

    public void friendsService(DependencyInjector.DI2<MySQLStorage, LangService> dependencies) {
        try {
            bootOutput.add(Component.text("Building friends service...", NamedTextColor.DARK_GRAY));

            FriendsConfig config = new FriendsConfig(new File(api.dataFolder(), "friends.yml"));
            if (!config.generate(bootOutput, dependencies.d2(), LangFileMappings.VELOCITY_FRIENDS_TEMPLATE))
                throw new IllegalStateException("Unable to load or create friends.yml!");
            config.register();

            if(!config.isEnabled()) {
                bootOutput.add(Component.text("The friends service wasn't enabled.", NamedTextColor.GRAY));
                return;
            }

            FriendsServiceSettings settings = new FriendsServiceSettings(
                    dependencies.d1(),
                    config.getMaxFriends(),
                    config.isSendNotifications(),
                    config.isShowFamilies(),
                    config.isAllowMessaging()
            );

            FriendsService service = new FriendsService(settings);

            service.initCommand(inject(bootOutput));

            services.put(FriendsService.class, service);
            bootOutput.add(Component.text("Finished building friends service.", NamedTextColor.GREEN));
        } catch (Exception e) {
            bootOutput.add(ProxyLang.BOXED_MESSAGE_COLORED.build(e.getMessage(), NamedTextColor.RED));
            bootOutput.add(Component.text("The friends service wasn't enabled.", NamedTextColor.GRAY));
        }
    }

    public void playerService(DependencyInjector.DI2<MySQLStorage, LangService> dependencies) throws Exception {
        bootOutput.add(Component.text(" | Building player logging service...", NamedTextColor.DARK_GRAY));

        services.put(PlayerService.class, new PlayerService(dependencies.d1()));

        bootOutput.add(Component.text(" | Finished building player logging service.", NamedTextColor.GREEN));
    }

    public void dynamicTeleportService(DependencyInjector.DI3<FamilyService, ServerService, LangService> dependencies) {
        bootOutput.add(Component.text("Building dynamic teleport service...", NamedTextColor.DARK_GRAY));
        try {
            DynamicTeleportConfig config = new DynamicTeleportConfig(new File(api.dataFolder(), "dynamic_teleport.yml"));
            if (!config.generate(bootOutput, dependencies.d3(), LangFileMappings.VELOCITY_DYNAMIC_TELEPORT_TEMPLATE))
                throw new IllegalStateException("Unable to load or create dynamic_teleport.yml!");
            config.register();

            if(!config.isEnabled()) {
                bootOutput.add(Component.text("The dynamic teleport service wasn't enabled.", NamedTextColor.GRAY));
                return;
            }

            DynamicTeleportService dynamicTeleportService = DynamicTeleportService.init(inject(bootOutput, dependencies.d1()), config);

            try {
                TPAService tpaService = dynamicTeleportService.services().tpa().orElseThrow();
                tpaService.cleaner().startHeartbeat(tpaService);
                tpaService.initCommand(inject(dependencies.d1(), dependencies.d2(), bootOutput));
                bootOutput.add(Component.text(" | The TPA module was started successfully!",NamedTextColor.GREEN));
            } catch (NoSuchElementException ignore) {
            } catch (Exception e) {
                bootOutput.add(Component.text(" | The TPA module couldn't be started.",NamedTextColor.RED));
                throw new RuntimeException(e);
            }

            try {
                dynamicTeleportService.services().hub().orElseThrow().initCommand(inject(dependencies.d1(), dependencies.d2(), bootOutput));
                bootOutput.add(Component.text(" | The HUB module was started successfully!",NamedTextColor.GREEN));
            } catch (NoSuchElementException ignore) {
            } catch (Exception e) {
                bootOutput.add(Component.text(" | The HUB module couldn't be started.",NamedTextColor.RED));
                throw new RuntimeException(e);
            }

            try {
                dynamicTeleportService.services().anchor().orElseThrow().initCommands(inject(dynamicTeleportService, dependencies.d2(), bootOutput));
                bootOutput.add(Component.text(" | The Anchor module was started successfully!",NamedTextColor.GREEN));
            } catch (NoSuchElementException ignore) {
            } catch (Exception e) {
                bootOutput.add(Component.text(" | The Anchor module couldn't be started.",NamedTextColor.RED));
                throw new RuntimeException(e);
            }

            services.put(DynamicTeleportService.class, dynamicTeleportService);

            bootOutput.add(Component.text("Finished building dynamic teleport service.", NamedTextColor.GREEN));
        } catch (Exception e) {
            bootOutput.add(ProxyLang.BOXED_MESSAGE_COLORED.build(e.getMessage(), NamedTextColor.RED));
            bootOutput.add(Component.text("The dynamic teleport service wasn't enabled.", NamedTextColor.GRAY));
        }
    }
}
