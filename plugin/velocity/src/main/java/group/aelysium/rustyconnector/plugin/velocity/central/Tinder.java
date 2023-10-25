package group.aelysium.rustyconnector.plugin.velocity.central;

import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.api.velocity.central.VelocityTinder;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.core.lib.lang.config.RootLanguageConfig;
import group.aelysium.rustyconnector.api.mc_loader.central.MCLoaderTinder;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import org.slf4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * The root api endpoint for the entire RustyConnector api.
 */
public class Tinder implements VelocityTinder {
    private static Tinder instance;
    public static Tinder get() {
        return instance;
    }

    private final VelocityRustyConnector plugin;
    private final ProxyServer server;
    private Flame flame;
    private final Path dataFolder;
    private final PluginLogger pluginLogger;
    private final LangService lang;

    private Tinder(VelocityRustyConnector plugin, ProxyServer server, PluginLogger logger, @DataDirectory Path dataFolder, LangService lang) {
        instance = this;

        this.plugin = plugin;
        this.server = server;
        this.pluginLogger = logger;
        this.dataFolder = dataFolder;
        this.lang = lang;
    }

    /**
     * Ignites a {@link Flame} which effectively starts the RustyConnector kernel.
     */
    public void ignite() throws RuntimeException {
        this.flame = Flame.fabricateNew(this.plugin, this.lang);
    }

    public PluginLogger logger() {
        return this.pluginLogger;
    }

    public String dataFolder() {
        return String.valueOf(this.dataFolder);
    }

    public LangService lang() {
        return this.lang;
    }

    /**
     * Restarts the entire RustyConnector kernel by exhausting the current {@link Flame} and igniting a new one.
     */
    public void rekindle() {
        try {
            this.flame.exhaust(this.plugin);
            this.flame = null;

            this.ignite();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
     * Gets a resource by name and returns it as a stream.
     * @param filename The name of the resource to get.
     * @return The resource as a stream.
     */
    public static InputStream resourceAsStream(String filename)  {
        return MCLoaderTinder.class.getClassLoader().getResourceAsStream(filename);
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
        try {
            PluginLogger pluginLogger = new PluginLogger(logger);
            RootLanguageConfig config = new RootLanguageConfig(new File(String.valueOf(dataFolder), "language.yml"));
            if (!config.generate(pluginLogger))
                throw new IllegalStateException("Unable to load or create language.yml!");
            config.register();

            LangService langService = LangService.resolveLanguageCode(config.getLanguage(), dataFolder);

            return new Tinder(plugin, server, pluginLogger, dataFolder, langService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
