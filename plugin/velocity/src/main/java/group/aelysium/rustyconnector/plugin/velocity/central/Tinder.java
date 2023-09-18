package group.aelysium.rustyconnector.plugin.velocity.central;

import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.scheduler.Scheduler;
import group.aelysium.rustyconnector.core.central.PluginAPI;
import group.aelysium.rustyconnector.core.lib.lang.Lang;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Path;

/**
 * The root api endpoint for the entire RustyConnector api.
 */
public class Tinder extends PluginAPI<Scheduler> {
    private static Tinder instance;
    public static Tinder get() {
        return instance;
    }

    private final VelocityRustyConnector plugin;
    private final ProxyServer server;
    private Flame flame;
    private final Path dataFolder;
    private final PluginLogger pluginLogger;

    private Tinder(VelocityRustyConnector plugin, ProxyServer server, Logger logger, @DataDirectory Path dataFolder) {
        instance = this;

        this.plugin = plugin;
        this.server = server;
        this.pluginLogger = new PluginLogger(logger);
        this.dataFolder = dataFolder;
    }

    /**
     * Ignites a {@link Flame} which effectively starts the RustyConnector kernel.
     * @return A {@link Flame}.
     */
    public Flame ignite() throws RuntimeException {
        try {
            this.flame = Flame.fabricateNew(this.plugin);
            return flame;
        } catch (Exception e) {
            this.logger().send(Lang.BOXED_MESSAGE_COLORED.build(e.getMessage(), NamedTextColor.RED));
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream resourceAsStream(String filename)  {
        return getClass().getClassLoader().getResourceAsStream(filename);
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

    public Path dataFolderPath() {
        return this.dataFolder;
    }

    /**
     * Restarts the entire RustyConnector kernel by exhausting the current {@link Flame} and igniting a new one.
     */
    public void rekindle() {
        this.flame.exhaust(this.plugin);
        this.flame = null;

        this.ignite();
    }

    public CoreServiceHandler services() {
        return this.flame.services();
    }

    /**
     * Returns the currently active RustyConnector kernel.
     * @return A {@link Flame}.
     */
    public Flame flame() {
        return this.flame;
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
     * Creates new {@link Tinder} based on the gathered resources.
     */
    public static Tinder gather(VelocityRustyConnector plugin, ProxyServer server, Logger logger, @DataDirectory Path dataFolder) {
        return new Tinder(plugin, server, logger, dataFolder);
    }
}
