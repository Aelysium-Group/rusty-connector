package group.aelysium.rustyconnector.plugin.velocity.central;

import com.sun.jdi.request.DuplicateRequestException;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.scheduler.Scheduler;
import group.aelysium.rustyconnector.core.central.PluginAPI;
import group.aelysium.rustyconnector.core.lib.database.MySQL;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.processor.VirtualProxyProcessor;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.SyncFailedException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

public class VelocityAPI extends PluginAPI<Scheduler> {
    private final VelocityRustyConnector plugin;
    private final ProxyServer server;
    private VirtualProxyProcessor virtualProcessor = null;
    private final Path dataFolder;
    private final PluginLogger pluginLogger;
    private MySQL mySQL = null;

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

    @Override
    public VirtualProxyProcessor getVirtualProcessor() {
        return this.virtualProcessor;
    }

    public void configureProcessor(DefaultConfig config) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        if(this.virtualProcessor != null) throw new IllegalAccessException("Attempted to configure the processor while it's already running!");
        this.virtualProcessor = VirtualProxyProcessor.init(config);
        this.virtualProcessor.startServices();
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
     * Set the MySQL database.
     * @throws DuplicateRequestException If the MySQL database is already set.
     */
    public void setMySQL(MySQL mySQL) {
        if(this.mySQL != null) throw new DuplicateRequestException("You can't set the MySQL database twice!");
        this.mySQL = mySQL;
    }

    /**
     * Get the MySQL database.
     * @return The MySQL database.
     */
    public MySQL getMySQL() {
        return this.mySQL;
    }
}
