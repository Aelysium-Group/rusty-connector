package group.aelysium.rustyconnector.plugin.paper.central;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.core.lib.Callable;
import group.aelysium.rustyconnector.core.lib.connectors.Connection;
import group.aelysium.rustyconnector.core.lib.connectors.Connector;
import group.aelysium.rustyconnector.core.lib.connectors.ConnectorsService;
import group.aelysium.rustyconnector.core.lib.connectors.config.ConnectorsConfig;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnection;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnector;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.core.lib.serviceable.ServiceableService;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.commands.CommandRusty;
import group.aelysium.rustyconnector.plugin.paper.central.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.paper.central.config.PrivateKeyConfig;
import group.aelysium.rustyconnector.plugin.paper.events.OnPlayerJoin;
import group.aelysium.rustyconnector.plugin.paper.events.OnPlayerLeave;
import group.aelysium.rustyconnector.plugin.paper.events.OnPlayerPreLogin;
import group.aelysium.rustyconnector.plugin.paper.lib.connectors.PaperMessengerSubscriber;
import group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.paper.lib.magic_link.MagicLinkService;
import group.aelysium.rustyconnector.plugin.paper.lib.services.PacketBuilderService;
import group.aelysium.rustyconnector.plugin.paper.lib.services.ServerInfoService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.util.*;

/**
 * The core module of RustyConnector.
 * All aspects of the plugin should be accessible from here.
 * If not, check {@link Tinder}.
 */
public class Flame extends ServiceableService<CoreServiceHandler> {
    private int configVersion;
    private final String version;

    /**
     * The core message backbone where all RC messages are sent through.
     */
    private final MessengerConnector<?> backbone;

    public Flame(String version, int configVersion, Map<Class<? extends Service>, Service> services, String backboneConnector) {
        super(new CoreServiceHandler(services));
        this.version = version;
        this.configVersion = configVersion;
        this.backbone = (MessengerConnector<?>) this.services().connectors().get(backboneConnector);
    }

    public String version() { return this.version; }
    public int configVersion() { return this.configVersion; }

    public MessengerConnector<? extends MessengerConnection> backbone() {
        return this.backbone;
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
     * Kill the {@link Flame}.
     * Typically good for if you want to ignite a new one.
     */
    public void exhaust(PaperRustyConnector plugin) {
        this.kill();
    }


    /**
     * Fabricates a new RustyConnector core and returns it.
     * @return A new RustyConnector {@link Flame}.
     */
    public static Flame fabricateNew(PaperRustyConnector plugin) throws IllegalAccessException {
        Initialize initialize = new Initialize();

        String version = initialize.version();
        int configVersion = initialize.configVersion();
        char[] privateKey = initialize.privateKey();
        DefaultConfig defaultConfig = initialize.defaultConfig();

        MessageCacheService messageCacheService = initialize.messageCache();
        Callable<Runnable> resolveConnectors = initialize.connectors(privateKey, messageCacheService, Tinder.get().logger());

        initialize.serverInfo(defaultConfig);
        initialize.messageCache();
        PacketBuilderService packetBuilderService = initialize.packetBuilder();
        initialize.dynamicTeleport();
        initialize.magicLink(packetBuilderService);

        Runnable connectRemotes = resolveConnectors.execute();
        connectRemotes.run();

        initialize.events(plugin);
        initialize.commands();

        Flame flame = new Flame(version, configVersion, initialize.getServices(), defaultConfig.getMessenger());

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
    private final List<Component> bootOutput = new ArrayList<>();

    public Map<Class<? extends Service>, Service> getServices() {
        return this.services;
    }

    public void events(PaperRustyConnector plugin) {
        api.paperServer().getPluginManager().registerEvents(new OnPlayerJoin(), plugin);
        api.paperServer().getPluginManager().registerEvents(new OnPlayerLeave(), plugin);
        api.paperServer().getPluginManager().registerEvents(new OnPlayerPreLogin(), plugin);
    }

    public void commands() {
        CommandRusty.create(api.commandManager());
    }

    public String version() {
        try {
            InputStream stream = Tinder.get().resourceAsStream("plugin.yml");
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
            InputStream stream = Tinder.get().resourceAsStream("plugin.yml");
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

    public char[] privateKey() throws IllegalAccessException {
        PrivateKeyConfig privateKeyConfig = PrivateKeyConfig.newConfig(new File(api.dataFolder(), "private.key"));
        if (!privateKeyConfig.generateFilestream(bootOutput))
            throw new IllegalStateException("Unable to load or create private.key!");

        try {
            return privateKeyConfig.get();
        } catch (Exception ignore) {
            throw new IllegalAccessException("There was a fatal error while reading private.key!");
        }
    }

    public DefaultConfig defaultConfig() throws IllegalAccessException {
        DefaultConfig defaultConfig = DefaultConfig.newConfig(new File(api.dataFolder(), "config.yml"), "velocity_config_template.yml");
        if (!defaultConfig.generate(bootOutput))
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
     * @param privateKey The plugin's pricate key.
     * @return A runnable which will wrap up the connectors' initialization. Should be run after all other initialization logic has run.
     */
    public Callable<Runnable> connectors(char[] privateKey, MessageCacheService cacheService, PluginLogger logger) {
        logger.send(Component.text("Building Connectors...", NamedTextColor.DARK_GRAY));

        ConnectorsConfig connectorsConfig = ConnectorsConfig.newConfig(new File(api.dataFolder(), "connectors.yml"), "paper_connectors_template.yml");
        if (!connectorsConfig.generate(bootOutput))
            throw new IllegalStateException("Unable to load or create connectorsConfig.yml!");
        ConnectorsService connectorsService = connectorsConfig.register(privateKey, true, false);
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

                Connector<?> connector = connectorsService.get(name);
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
                    MessengerConnection<PaperMessengerSubscriber> connection = connector.connection().orElseThrow();
                    connection.startListening(PaperMessengerSubscriber.class, cacheService, logger);
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