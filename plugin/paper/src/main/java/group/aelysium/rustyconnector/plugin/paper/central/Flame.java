package group.aelysium.rustyconnector.plugin.paper.central;

import group.aelysium.rustyconnector.core.central.PluginLogger;
import group.aelysium.rustyconnector.core.central.Tinder;
import group.aelysium.rustyconnector.core.lib.Callable;
import group.aelysium.rustyconnector.core.lib.connectors.Connector;
import group.aelysium.rustyconnector.core.lib.connectors.ConnectorsService;
import group.aelysium.rustyconnector.core.lib.connectors.config.ConnectorsConfig;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnection;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnector;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.hash.AESCryptor;
import group.aelysium.rustyconnector.core.lib.lang.config.LangFileMappings;
import group.aelysium.rustyconnector.core.lib.lang.config.LangService;
import group.aelysium.rustyconnector.core.lib.packets.PacketHandler;
import group.aelysium.rustyconnector.core.lib.packets.PacketOrigin;
import group.aelysium.rustyconnector.core.lib.packets.PacketType;
import group.aelysium.rustyconnector.core.lib.key.config.PrivateKeyConfig;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.core.lib.serviceable.ServiceableService;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import group.aelysium.rustyconnector.core.plugin.Plugin;
import group.aelysium.rustyconnector.core.plugin.central.CoreServiceHandler;
import group.aelysium.rustyconnector.core.plugin.central.config.DefaultConfig;
import group.aelysium.rustyconnector.core.plugin.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.core.plugin.lib.dynamic_teleport.handlers.CoordinateRequestHandler;
import group.aelysium.rustyconnector.core.plugin.lib.magic_link.MagicLinkService;
import group.aelysium.rustyconnector.core.plugin.lib.magic_link.handlers.MagicLink_PingResponseHandler;
import group.aelysium.rustyconnector.core.plugin.lib.services.PacketBuilderService;
import group.aelysium.rustyconnector.core.plugin.lib.services.ServerInfoService;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.commands.CommandRusty;
import group.aelysium.rustyconnector.plugin.paper.events.OnPlayerJoin;
import group.aelysium.rustyconnector.plugin.paper.events.OnPlayerLeave;
import group.aelysium.rustyconnector.plugin.paper.events.OnPlayerPreLogin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * The core module of RustyConnector.
 * All aspects of the plugin should be accessible from here.
 * If not, check {@link Tinder}.
 */
public class Flame extends ServiceableService<CoreServiceHandler> implements group.aelysium.rustyconnector.core.central.Flame {
    private final int configVersion;
    private final String version;

    /**
     * The core message backbone where all RC messages are sent through.
     */
    private final MessengerConnector<?> backbone;

    public Flame(String version, int configVersion, Map<Class<? extends Service>, Service> services, String backboneConnector) {
        super(new CoreServiceHandler(services));
        this.version = version;
        this.configVersion = configVersion;
        this.backbone = this.services().connectors().getMessenger(backboneConnector);
    }

    public String versionAsString() { return this.version; }
    public int configVersion() { return this.configVersion; }

    public MessengerConnector<? extends MessengerConnection> backbone() {
        return this.backbone;
    }

    /**
     * Returns the currently active RustyConnector kernel.
     *
     * @return A {@link Flame}.
     */
    public static group.aelysium.rustyconnector.core.central.Flame get() {
        return Plugin.getAPI().flame();
    }

    /**
     * Kill the {@link Flame}.
     * Typically good for if you want to ignite a new one.
     */
    public void exhaust() {
        this.kill();
    }


    /**
     * Fabricates a new RustyConnector core and returns it.
     * @return A new RustyConnector {@link Flame}.
     */
    public static group.aelysium.rustyconnector.core.central.Flame fabricateNew(PaperRustyConnector plugin, LangService langService) throws RuntimeException {
        Initialize initialize = new Initialize();

        try {
            String version = initialize.version();
            int configVersion = initialize.configVersion();
            AESCryptor cryptor = initialize.privateKey();
            DefaultConfig defaultConfig = initialize.defaultConfig(langService);

            MessageCacheService messageCacheService = initialize.messageCache();
            Callable<Runnable> resolveConnectors = initialize.connectors(cryptor, messageCacheService, Plugin.getAPI().logger(), langService);

            initialize.serverInfo(defaultConfig);
            initialize.messageCache();
            PacketBuilderService packetBuilderService = initialize.packetBuilder();
            initialize.dynamicTeleport();
            initialize.magicLink(packetBuilderService);

            Runnable connectRemotes = resolveConnectors.execute();
            connectRemotes.run();

            initialize.events(plugin);
            initialize.commands();

            return new Flame(version, configVersion, initialize.getServices(), defaultConfig.getMessenger());
        } catch (Exception e) {
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
    private final Tinder api = Plugin.getAPI();
    private final PluginLogger logger = api.logger();
    private final Map<Class<? extends Service>, Service> services = new HashMap<>();
    private final List<String> requestedConnectors = new ArrayList<>();
    private final List<Component> bootOutput = new ArrayList<>();

    public Map<Class<? extends Service>, Service> getServices() {
        return this.services;
    }

    public void events(PaperRustyConnector plugin) {
        ((group.aelysium.rustyconnector.plugin.paper.central.Tinder) api).paperServer().getPluginManager().registerEvents(new OnPlayerJoin(), plugin);
        ((group.aelysium.rustyconnector.plugin.paper.central.Tinder) api).paperServer().getPluginManager().registerEvents(new OnPlayerLeave(), plugin);
        ((group.aelysium.rustyconnector.plugin.paper.central.Tinder) api).paperServer().getPluginManager().registerEvents(new OnPlayerPreLogin(), plugin);
    }

    public void commands() {
        CommandRusty.create(((group.aelysium.rustyconnector.plugin.paper.central.Tinder) api).commandManager());
    }

    public String version() {
        try {
            InputStream stream = Tinder.resourceAsStream("plugin.yml");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            ConfigurationNode node = YAMLConfigurationLoader.builder()
                    .setIndent(2)
                    .setSource(() -> reader)
                    .build().load();

            stream.close();
            reader.close();
            return node.getNode("version").getString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int configVersion() {
        try {
            InputStream stream = Tinder.resourceAsStream("plugin.yml");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            ConfigurationNode node = YAMLConfigurationLoader.builder()
                    .setIndent(2)
                    .setSource(() -> reader)
                    .build().load();

            stream.close();
            reader.close();
            return node.getNode("config-version").getInt();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public AESCryptor privateKey() throws NoSuchAlgorithmException {
        PrivateKeyConfig privateKeyConfig = new PrivateKeyConfig(new File(api.dataFolder(), "private.key"));
        try {
            return privateKeyConfig.get(bootOutput);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public DefaultConfig defaultConfig(LangService lang) throws IOException {
        DefaultConfig defaultConfig = new DefaultConfig(new File(api.dataFolder(), "config.yml"));
        if (!defaultConfig.generate(bootOutput, lang, LangFileMappings.PAPER_CONFIG_TEMPLATE))
            throw new IllegalStateException("Unable to load or create config.yml!");
        defaultConfig.register(this.configVersion());

        requestedConnectors.add(defaultConfig.getMessenger());

        return defaultConfig;
    }

    /**
     * Initializes the connectors service.
     * First returns a {@link Callable} which once run, will return a {@link Runnable}.
     * <p>
     * {@link Callable} - Runs linting to only build the connectors actually being referenced by the configs. Returns:
     * <p>
     * a {@link Runnable} - Starts up all connectors and connects them to their remote resources.
     * @return A runnable which will wrap up the connectors' initialization. Should be run after all other initialization logic has run.
     */
    public Callable<Runnable> connectors(AESCryptor cryptor, MessageCacheService cacheService, PluginLogger logger, LangService lang) throws IOException {
        logger.send(Component.text("Building Connectors...", NamedTextColor.DARK_GRAY));

        ConnectorsConfig connectorsConfig = new ConnectorsConfig(new File(api.dataFolder(), "connectors.yml"));
        if (!connectorsConfig.generate(bootOutput, lang, LangFileMappings.PAPER_CONNECTORS_TEMPLATE))
            throw new IllegalStateException("Unable to load or create connectorsConfig.yml!");
        ConnectorsService connectorsService = connectorsConfig.register(cryptor, true, false, PacketOrigin.PROXY, api.dataFolder());
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

                Connector<?> connector = connectorsService.getMessenger(name);
                try {
                    connector.connect();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            logger.send(Component.text("Finished validating Connector service.", NamedTextColor.GREEN));

            // Needs to run even later to actually boot all the connectors and connect them to their remote resources.
            return () -> {
                logger.send(Component.text("Booting Connectors service...", NamedTextColor.DARK_GRAY));

                Map<PacketType.Mapping, PacketHandler> handlers = new HashMap<>();
                handlers.put(PacketType.PING_RESPONSE, new MagicLink_PingResponseHandler());
                handlers.put(PacketType.COORDINATE_REQUEST_QUEUE, new CoordinateRequestHandler());

                connectorsService.messengers().forEach(connector -> {
                    if(connector.connection().isEmpty()) return;
                    MessengerConnection connection = connector.connection().orElseThrow();
                    connection.startListening(cacheService, logger, handlers);
                });
                logger.send(Component.text("Finished booting Connectors service.", NamedTextColor.GREEN));
            };
        };
    }

    public void magicLink(PacketBuilderService packetBuilderService) {
        logger.send(Component.text("Building magic link service...", NamedTextColor.DARK_GRAY));

        MagicLinkService magicLinkService = new MagicLinkService(3);
        services.put(MagicLinkService.class, magicLinkService);
        magicLinkService.startHeartbeat(packetBuilderService);

        logger.send(Component.text("Finished booting magic link service.", NamedTextColor.GREEN));
    }

    public void serverInfo(DefaultConfig defaultConfig) {
        ServerInfoService serverInfoService = new ServerInfoService(
                defaultConfig.getServer_name(),
                AddressUtil.parseAddress(defaultConfig.getServer_address()),
                defaultConfig.getServer_family(),
                defaultConfig.getServer_playerCap_soft(),
                defaultConfig.getServer_playerCap_hard(),
                defaultConfig.getServer_weight()
        );
        services.put(ServerInfoService.class, serverInfoService);
    }

    public MessageCacheService messageCache() {
        MessageCacheService service = new MessageCacheService(50);
        services.put(MessageCacheService.class, service);

        logger.log("Set message cache size to be: 50");
        return service;
    }

    public PacketBuilderService packetBuilder() {
        PacketBuilderService service = new PacketBuilderService();
        services.put(PacketBuilderService.class, service);
        return service;
    }

    public void dynamicTeleport() {
        services.put(DynamicTeleportService.class, new DynamicTeleportService());
    }

}