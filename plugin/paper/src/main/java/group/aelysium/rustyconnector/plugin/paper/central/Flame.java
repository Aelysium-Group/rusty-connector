package group.aelysium.rustyconnector.plugin.paper.central;

import group.aelysium.rustyconnector.core.lib.messenger.config.ConnectorsConfig;
import group.aelysium.rustyconnector.core.lib.messenger.implementors.redis.RedisConnection;
import group.aelysium.rustyconnector.core.lib.messenger.implementors.redis.RedisConnector;
import group.aelysium.rustyconnector.core.lib.messenger.MessengerConnection;
import group.aelysium.rustyconnector.core.lib.messenger.MessengerConnector;
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
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.commands.CommandRusty;
import group.aelysium.rustyconnector.plugin.paper.central.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.paper.events.OnPlayerJoin;
import group.aelysium.rustyconnector.plugin.paper.events.OnPlayerLeave;
import group.aelysium.rustyconnector.plugin.paper.events.OnPlayerPreLogin;
import group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.handlers.CoordinateRequestHandler;
import group.aelysium.rustyconnector.plugin.paper.lib.magic_link.MagicLinkService;
import group.aelysium.rustyconnector.plugin.paper.lib.magic_link.handlers.MagicLink_PingResponseHandler;
import group.aelysium.rustyconnector.plugin.paper.lib.services.PacketBuilderService;
import group.aelysium.rustyconnector.plugin.paper.lib.services.ServerInfoService;
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
public class Flame extends ServiceableService<CoreServiceHandler> {
    private int configVersion;
    private final String version;

    /**
     * The core message backbone where all RC messages are sent through.
     */
    private final MessengerConnector<?> backbone;

    public Flame(String version, int configVersion, Map<Class<? extends Service>, Service> services, RedisConnector messenger) {
        super(new CoreServiceHandler(services));
        this.version = version;
        this.configVersion = configVersion;
        this.backbone = messenger;
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
    public static Flame fabricateNew(PaperRustyConnector plugin, LangService langService) throws RuntimeException {
        Initialize initialize = new Initialize();

        try {
            String version = initialize.version();
            int configVersion = initialize.configVersion();
            AESCryptor cryptor = initialize.privateKey();
            DefaultConfig defaultConfig = initialize.defaultConfig(langService);

            MessageCacheService messageCacheService = initialize.messageCache();
            RedisConnector messenger = initialize.connectors(cryptor, messageCacheService, Tinder.get().logger(), langService);

            initialize.serverInfo(defaultConfig);
            initialize.messageCache();
            PacketBuilderService packetBuilderService = initialize.packetBuilder();
            initialize.dynamicTeleport();
            initialize.magicLink(packetBuilderService);

            initialize.events(plugin);
            initialize.commands();

            return new Flame(version, configVersion, initialize.getServices(), messenger);
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
    private final Tinder api = Tinder.get();
    private final PluginLogger logger = Tinder.get().logger();
    private final Map<Class<? extends Service>, Service> services = new HashMap<>();
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

        return defaultConfig;
    }

    public RedisConnector connectors(AESCryptor cryptor, MessageCacheService cacheService, PluginLogger logger, LangService lang) throws IOException {
        logger.send(Component.text("Building Connectors...", NamedTextColor.DARK_GRAY));

        ConnectorsConfig config = new ConnectorsConfig(new File(api.dataFolder(), "connectors.yml"));
        if (!config.generate(bootOutput, lang, LangFileMappings.PAPER_CONNECTORS_TEMPLATE))
            throw new IllegalStateException("Unable to load or create connectorsConfig.yml!");
        config.register(true, false);

        RedisConnector.RedisConnectorSpec spec = new RedisConnector.RedisConnectorSpec(
                PacketOrigin.PROXY,
                config.getRedis_address(),
                config.getRedis_user(),
                config.getRedis_protocol(),
                config.getRedis_dataChannel()
        );
        RedisConnector messenger = RedisConnector.create(cryptor, spec);
        services.put(RedisConnector.class, messenger);


        messenger.connect();
        RedisConnection connection = messenger.connection().orElseThrow();

        Map<PacketType.Mapping, PacketHandler> handlers = new HashMap<>();
        handlers.put(PacketType.PING_RESPONSE, new MagicLink_PingResponseHandler());
        handlers.put(PacketType.COORDINATE_REQUEST_QUEUE, new CoordinateRequestHandler());
        connection.startListening(cacheService, logger, handlers);

        logger.send(Component.text("Finished building Connectors.", NamedTextColor.GREEN));

        return messenger;
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