package group.aelysium.rustyconnector.plugin.velocity.central;

import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.scheduler.Scheduler;
import group.aelysium.rustyconnector.core.central.PluginAPI;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisService;
import group.aelysium.rustyconnector.core.lib.model.Service;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.database.RedisSubscriber;
import group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.MagicLinkService;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.SyncFailedException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Optional;

import static group.aelysium.rustyconnector.plugin.velocity.central.Processor.ValidServices.REDIS_SERVICE;
import static group.aelysium.rustyconnector.plugin.velocity.central.Processor.ValidServices.SERVER_SERVICE;

public class VelocityAPI extends PluginAPI<Scheduler> {
    private final VelocityRustyConnector plugin;
    private final ProxyServer server;
    private Processor processor = null;
    private final Path dataFolder;
    private final PluginLogger pluginLogger;

    public VelocityAPI(VelocityRustyConnector plugin, ProxyServer server, Logger logger, @DataDirectory Path dataFolder) {
        this.plugin = plugin;
        this.server = server;
        this.pluginLogger = new PluginLogger(logger);
        this.dataFolder = dataFolder;
    }

    @Override
    public InputStream getResourceAsStream(String filename)  {
        return getClass().getClassLoader().getResourceAsStream(filename);
    }

    @Override
    public Scheduler getScheduler() {
        return getServer().getScheduler();
    }

    @Override
    public PluginLogger getLogger() {
        return this.pluginLogger;
    }

    @Override
    public String getDataFolder() {
        return String.valueOf(this.dataFolder);
    }

    public <S extends Service> Optional<S> getService(Class<S> type) {
        return this.processor.getService(type);
    }

    public void killServices() {
        this.processor.kill();
    }

    public void reloadServices() {
        this.processor.kill();
        this.processor = null;

        VelocityRustyConnector.getLifecycle().loadConfigs();
    }

    public void configureProcessor(DefaultConfig config) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException, SQLException {
        if(this.processor != null) throw new IllegalAccessException("Attempted to configure the processor while it's already running!");
        this.processor = Processor.init(config);
        this.processor.getService(REDIS_SERVICE).orElseThrow().start(RedisSubscriber.class);
        this.processor.getService(SERVER_SERVICE).orElseThrow().getService(MagicLinkService.class).startHeartbeat();
    }

    /**
     * Get the velocity server
     */
    public ProxyServer getServer() {
        return this.server;
    }

    /**
     * Registers a server with this proxy.` A server with this name should not already exist.
     *
     * @param serverInfo the server to register
     * @return the newly registered server
     */
    public RegisteredServer registerServer(ServerInfo serverInfo) {
        return getServer().registerServer(serverInfo);
    }

    /**
     * Unregisters this server from the proxy.
     *
     * @param serverInfo the server to unregister
     */
    public void unregisterServer(ServerInfo serverInfo) {
        getServer().unregisterServer(serverInfo);
    }

    /**
     * Attempt to access the plugin instance directly.
     * @return The plugin instance.
     * @throws SyncFailedException If the plugin is currently running.
     */
    public VelocityRustyConnector accessPlugin() throws SyncFailedException {
        if(VelocityRustyConnector.getLifecycle().isRunning()) throw new SyncFailedException("You can't get the plugin instance while the plugin is running!");
        return this.plugin;
    }

    /**
     * Attempt to dispatch a command as the Proxy
     * @param command The command to dispatch.
     */
    public void dispatchCommand(String command) {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        api.getServer().getCommandManager()
                .executeAsync((ConsoleCommandSource) permission -> null, command);
    }
}
