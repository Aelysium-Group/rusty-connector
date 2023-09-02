package group.aelysium.rustyconnector.plugin.velocity.central;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.scheduler.Scheduler;
import group.aelysium.rustyconnector.core.central.PluginAPI;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnection;
import group.aelysium.rustyconnector.core.lib.connectors.storage.StorageConnector;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisConnection;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.velocity.config.MemberKeyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.Core;
import group.aelysium.rustyconnector.plugin.velocity.lib.CoreServiceHandler;
import group.aelysium.rustyconnector.plugin.velocity.lib.database.RedisSubscriber;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.PlayerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.ViewportService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.nio.file.Path;
import java.sql.SQLException;

public class VelocityAPI extends PluginAPI<Scheduler> {
    private static VelocityAPI instance;
    public static VelocityAPI get() {
        return instance;
    }

    private String version;
    private final VelocityRustyConnector plugin;
    private final ProxyServer server;
    private Core core = BootManager.buildCore();
    private final Path dataFolder;
    private final PluginLogger pluginLogger;
    private final char[] memberKey;

    public VelocityAPI(VelocityRustyConnector plugin, ProxyServer server, Logger logger, @DataDirectory Path dataFolder) throws SQLException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        instance = this;

        try {
            InputStream stream = resourceAsStream("velocity-plugin.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            JsonObject json = new Gson().fromJson(reader, JsonObject.class);

            stream.close();
            reader.close();
            this.version = json.get("version").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
            this.version = null;
        }

        // Initialize member key
        char[] memberKey = null;
        try {
            MemberKeyConfig memberKeyConfig = MemberKeyConfig.newConfig(new File(String.valueOf(dataFolder), "member.key"));
            if (!memberKeyConfig.generate())
                throw new IllegalStateException("Unable to load or create member.key!");
            try {
                memberKey = memberKeyConfig.get();
            } catch (Exception ignore) {
                throw new IllegalAccessException("There was a fatal error while reading member.key!");
            }
            if(memberKey.length == 0) memberKey = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.memberKey = memberKey;

        this.plugin = plugin;
        this.server = server;
        this.pluginLogger = new PluginLogger(logger);
        this.dataFolder = dataFolder;
    }

    @Override
    public InputStream resourceAsStream(String filename)  {
        return getClass().getClassLoader().getResourceAsStream(filename);
    }

    @Override
    public String version() {
        return this.version;
    }

    @Override
    public Scheduler scheduler() {
        return velocityServer().getScheduler();
    }

    @Override
    public PluginLogger logger() {
        return this.pluginLogger;
    }

    @Override
    public String dataFolder() {
        return String.valueOf(this.dataFolder);
    }

    public void reloadRustyConnector() throws SQLException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        this.core.kill();
        this.core = null;

        this.core = BootManager.buildCore();
    }

    public void configureProcessor() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException, SQLException {
        PluginLogger logger = VelocityAPI.get().logger();
        if(this.core != null) throw new IllegalAccessException("Attempted to configure the processor while it's already running!");
        this.core = BootManager.buildCore();

        logger.send(Component.text("Booting Connectors service...", NamedTextColor.DARK_GRAY));
        this.services().connectorsService().messengers().forEach(connector -> {
            if(connector.connection().isEmpty()) return;
            MessengerConnection connection = connector.connection().orElseThrow();

            if(connection instanceof RedisConnection) connection.startListening(RedisSubscriber.class);
        });
        this.services().connectorsService().storage().forEach(connector -> {
            if(!connector.connection().isEmpty()) return;
            try {
                connector.connect();
            } catch (ConnectException e) {
                throw new RuntimeException(e);
            }
        });
        logger.send(Component.text("Finished booting Connectors service.", NamedTextColor.GREEN));

        logger.send(Component.text("Booting magic link service...", NamedTextColor.DARK_GRAY));
        this.core.services().magicLinkService().startHeartbeat();
        logger.send(Component.text("Finished booting magic link service.", NamedTextColor.GREEN));

        try {
            logger.send(Component.text("Booting family service...", NamedTextColor.DARK_GRAY));

            FamilyService familyService = this.core.services().familyService();

            familyService.dump().forEach(baseServerFamily -> {
                try {
                    ((PlayerFocusedServerFamily) baseServerFamily).resolveParent();
                } catch (Exception e) {
                    logger.log("There was an issue resolving the parent for "+baseServerFamily.name()+". "+e.getMessage());
                }
            });

            logger.send(Component.text("Finished booting family service.", NamedTextColor.GREEN));
        } catch (Exception ignore) {}

        try {
            VelocityAPI.get().logger().send(Component.text("Building friends service...", NamedTextColor.DARK_GRAY));
            FriendsService friendsService = BootManager.Initializer.buildFriendsService().orElseThrow();

            this.core.services().add(friendsService);

            friendsService.initCommand();
            VelocityAPI.get().logger().send(Component.text("Finished building friends service.", NamedTextColor.GREEN));
        } catch (Exception ignore) {
            VelocityAPI.get().logger().send(Component.text("The friends service wasn't enabled.", NamedTextColor.GRAY));
        }

        try {
            PlayerService playerService = BootManager.Initializer.buildPlayerService().orElseThrow();

            this.core.services().add(playerService);
        } catch (Exception ignore) {}
        try {
            VelocityAPI.get().logger().send(Component.text("Building party service...", NamedTextColor.DARK_GRAY));
            PartyService partyService = BootManager.Initializer.buildPartyService().orElseThrow();

            this.core.services().add(partyService);

            partyService.initCommand();
            VelocityAPI.get().logger().send(Component.text("Finished building party service.", NamedTextColor.GREEN));
        } catch (Exception ignore) {
            VelocityAPI.get().logger().send(Component.text("The party service wasn't enabled.", NamedTextColor.GRAY));
        }
        try {
            VelocityAPI.get().logger().send(Component.text("Building dynamic teleport service...", NamedTextColor.DARK_GRAY));
            DynamicTeleportService dynamicTeleportService = BootManager.Initializer.buildDynamicTeleportService().orElseThrow();

            this.core.services().add(dynamicTeleportService);

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

            VelocityAPI.get().logger().send(Component.text("Finished building dynamic teleport service.", NamedTextColor.GREEN));
        } catch (Exception ignore) {
            VelocityAPI.get().logger().send(Component.text("The dynamic teleport service wasn't enabled.", NamedTextColor.GRAY));
        }
        try {
            VelocityAPI.get().logger().send(Component.text("Building viewport service...", NamedTextColor.DARK_GRAY));
            ViewportService viewportService = BootManager.Initializer.buildViewportService().orElseThrow();

            this.core.services().add(viewportService);

            VelocityAPI.get().logger().send(Component.text("Finished building viewport service.", NamedTextColor.GREEN));
        } catch (Exception ignore) {
            VelocityAPI.get().logger().send(Component.text("The viewport service wasn't enabled.", NamedTextColor.GRAY));
        }
    }

    public CoreServiceHandler services() {
        return this.core.services();
    }

    public Core core() {
        return this.core;
    }

    /**
     * Get the velocity server
     */
    public ProxyServer velocityServer() {
        return this.server;
    }

    /**
     * Registers a server with this proxy.` A server with this name should not already exist.
     *
     * @param serverInfo the server to register
     * @return the newly registered server
     */
    public RegisteredServer registerServer(ServerInfo serverInfo) {
        return velocityServer().registerServer(serverInfo);
    }

    /**
     * Unregisters this server from the proxy.
     *
     * @param serverInfo the server to unregister
     */
    public void unregisterServer(ServerInfo serverInfo) {
        velocityServer().unregisterServer(serverInfo);
    }

    /**
     * Attempt to access the plugin instance directly.
     * @return The plugin instance.
     * @throws SyncFailedException If the plugin is currently running.
     */
    public VelocityRustyConnector accessPlugin() throws SyncFailedException {
        if(VelocityRustyConnector.lifecycle().isRunning()) throw new SyncFailedException("You can't get the plugin instance while the plugin is running!");
        return this.plugin;
    }
}
