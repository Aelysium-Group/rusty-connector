package group.aelysium.rustyconnector.plugin.paper.central;

import group.aelysium.rustyconnector.api.mc_loader.central.MCLoaderFlame;
import group.aelysium.rustyconnector.core.lib.messenger.config.ConnectorsConfig;
import group.aelysium.rustyconnector.core.lib.messenger.implementors.redis.RedisConnection;
import group.aelysium.rustyconnector.core.lib.messenger.implementors.redis.RedisConnector;
import group.aelysium.rustyconnector.core.lib.messenger.MessengerConnection;
import group.aelysium.rustyconnector.core.lib.messenger.MessengerConnector;
import group.aelysium.rustyconnector.api.velocity.central.PluginLogger;
import group.aelysium.rustyconnector.api.mc_loader.central.MCLoaderTinder;
import group.aelysium.rustyconnector.core.lib.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.hash.AESCryptor;
import group.aelysium.rustyconnector.api.velocity.lang.config.LangFileMappings;
import group.aelysium.rustyconnector.api.velocity.lang.config.LangService;
import group.aelysium.rustyconnector.core.lib.packets.PacketHandler;
import group.aelysium.rustyconnector.core.lib.packets.PacketOrigin;
import group.aelysium.rustyconnector.core.lib.packets.PacketType;
import group.aelysium.rustyconnector.core.lib.key.config.PrivateKeyConfig;
import group.aelysium.rustyconnector.api.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.api.velocity.util.AddressUtil;
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
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * The core module of RustyConnector.
 * All aspects of the plugin should be accessible from here.
 * If not, check {@link MCLoaderTinder}.
 */
public class Flame extends MCLoaderFlame<CoreServiceHandler> {
    private final int configVersion;
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
    public static MCLoaderFlame get() {
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
    public static MCLoaderFlame<CoreServiceHandler> fabricateNew(PaperRustyConnector plugin, LangService langService) throws RuntimeException {
        Initialize initialize = new Initialize();

        try {
            String version = initialize.version();
            int configVersion = initialize.configVersion();
            AESCryptor cryptor = initialize.privateKey();
            DefaultConfig defaultConfig = initialize.defaultConfig(langService);
            ServerInfoService serverInfoService = initialize.serverInfo(defaultConfig);

            MessageCacheService messageCacheService = initialize.messageCache();
            RedisConnector messenger = initialize.connectors(cryptor, messageCacheService, Plugin.getAPI().logger(), langService, AddressUtil.stringToAddress(serverInfoService.address()));

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
    private final MCLoaderTinder api = Plugin.getAPI();
    private final PluginLogger logger = api.logger();
    private final Map<Class<? extends Service>, Service> services = new HashMap<>();
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
            InputStream stream = MCLoaderTinder.resourceAsStream("plugin.yml");
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
            InputStream stream = MCLoaderTinder.resourceAsStream("plugin.yml");
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

    public RedisConnector connectors(AESCryptor cryptor, MessageCacheService cacheService, PluginLogger logger, LangService lang, InetSocketAddress originAddress) throws IOException {
        logger.send(Component.text("Building Connectors...", NamedTextColor.DARK_GRAY));

        ConnectorsConfig config = new ConnectorsConfig(new File(api.dataFolder(), "connectors.yml"));
        if (!config.generate(bootOutput, lang, LangFileMappings.PAPER_CONNECTORS_TEMPLATE))
            throw new IllegalStateException("Unable to load or create connectorsConfig.yml!");
        config.register(true, false);

        RedisConnector.RedisConnectorSpec spec = new RedisConnector.RedisConnectorSpec(
                PacketOrigin.SERVER,
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
        connection.startListening(cacheService, logger, handlers, originAddress);

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

    public ServerInfoService serverInfo(DefaultConfig defaultConfig) {
        ServerInfoService serverInfoService = new ServerInfoService(
                defaultConfig.getServer_name(),
                AddressUtil.parseAddress(defaultConfig.getServer_address()),
                defaultConfig.getServer_family(),
                defaultConfig.getServer_playerCap_soft(),
                defaultConfig.getServer_playerCap_hard(),
                defaultConfig.getServer_weight()
        );
        services.put(ServerInfoService.class, serverInfoService);

        return serverInfoService;
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