package group.aelysium.rustyconnector.core.mcloader.central;

import group.aelysium.rustyconnector.core.lib.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.crypt.AESCryptor;
import group.aelysium.rustyconnector.core.lib.key.config.PrivateKeyConfig;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.core.lib.messenger.config.ConnectorsConfig;
import group.aelysium.rustyconnector.core.lib.messenger.implementors.redis.RedisConnection;
import group.aelysium.rustyconnector.core.lib.messenger.implementors.redis.RedisConnector;
import group.aelysium.rustyconnector.core.mcloader.central.config.DefaultConfig;
import group.aelysium.rustyconnector.core.mcloader.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.core.mcloader.lib.dynamic_teleport.handlers.CoordinateRequestListener;
import group.aelysium.rustyconnector.core.mcloader.lib.magic_link.MagicLinkService;
import group.aelysium.rustyconnector.core.mcloader.lib.magic_link.handlers.MagicLink_PingResponseListener;
import group.aelysium.rustyconnector.core.mcloader.lib.packet_builder.PacketBuilderService;
import group.aelysium.rustyconnector.core.mcloader.lib.ranked_game_interface.handlers.RankedGameAssociateListener;
import group.aelysium.rustyconnector.core.mcloader.lib.server_info.ServerInfoService;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.toolkit.core.logger.PluginLogger;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketOrigin;
import group.aelysium.rustyconnector.toolkit.core.serviceable.ServiceableService;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.util.AddressUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.*;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MCLoaderFlame extends ServiceableService<CoreServiceHandler> implements group.aelysium.rustyconnector.toolkit.mc_loader.central.MCLoaderFlame<CoreServiceHandler, RedisConnector> {
    protected final int configVersion;
    protected final String version;
    private final RedisConnector backbone;

    public MCLoaderFlame(String version, int configVersion, CoreServiceHandler services, RedisConnector messenger) {
        super(services);
        this.version = version;
        this.configVersion = configVersion;
        this.backbone = messenger;
    }

    public String versionAsString() { return this.version; }
    public int configVersion() { return this.configVersion; }
    public RedisConnector backbone() {
        return (RedisConnector) this.backbone;
    }

    /**
     * Fabricates a new RustyConnector core and returns it.
     * @return A new RustyConnector {@link MCLoaderFlame}.
     */
    public static MCLoaderFlame fabricateNew(MCLoaderTinder api, LangService langService, PluginLogger logger) throws RuntimeException {
        Initialize initialize = new Initialize(api);

        try {
            String version = initialize.version();
            int configVersion = initialize.configVersion();
            AESCryptor cryptor = initialize.privateKey();
            DefaultConfig defaultConfig = initialize.defaultConfig(langService);
            ServerInfoService serverInfoService = initialize.serverInfo(defaultConfig);

            MessageCacheService messageCacheService = initialize.messageCache();
            RedisConnector messenger = initialize.connectors(cryptor, messageCacheService, logger, langService, AddressUtil.stringToAddress(serverInfoService.address()));

            initialize.messageCache();
            PacketBuilderService packetBuilderService = initialize.packetBuilder();
            initialize.dynamicTeleport();
            initialize.magicLink(packetBuilderService);

            return new MCLoaderFlame(version, configVersion, new CoreServiceHandler(initialize.getServices()), messenger);
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
    private final MCLoaderTinder api;
    private final PluginLogger logger;
    private final Map<Class<? extends Service>, Service> services = new HashMap<>();
    private final List<Component> bootOutput = new ArrayList<>();

    public Initialize(MCLoaderTinder tinder) {
        this.api = tinder;
        this.logger = api.logger();
    }

    public Map<Class<? extends Service>, Service> getServices() {
        return this.services;
    }

    public String version() {
        try {
            InputStream stream = group.aelysium.rustyconnector.toolkit.mc_loader.central.MCLoaderTinder.resourceAsStream("plugin.yml");
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

        connection.listen(new MagicLink_PingResponseListener(this.api));
        connection.listen(new CoordinateRequestListener(this.api));
        connection.listen(new RankedGameAssociateListener(this.api));

        connection.startListening(cacheService, logger, originAddress);

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
        InetSocketAddress address = null;
        try {
            address = AddressUtil.parseAddress(defaultConfig.address());
        } catch (Exception ignore) {}

        // TODO: Create a port resolver
        ServerInfoService serverInfoService = new ServerInfoService(
                address,
                defaultConfig.magicConfig(),
                defaultConfig.magicInterfaceResolver(),
                80 // Bukkit.getPort()
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