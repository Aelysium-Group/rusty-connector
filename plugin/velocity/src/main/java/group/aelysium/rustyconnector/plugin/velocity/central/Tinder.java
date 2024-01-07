package group.aelysium.rustyconnector.plugin.velocity.central;

import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import group.aelysium.rustyconnector.toolkit.velocity.central.VelocityFlame;
import group.aelysium.rustyconnector.toolkit.velocity.central.VelocityTinder;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.core.lib.lang.config.RootLanguageConfig;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.IMCLoaderTinder;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Vector;
import java.util.function.Consumer;

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
    private final Vector<Consumer<Flame>> onStart = new Vector<>();
    private final Vector<Runnable> onStop = new Vector<>();

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

        this.onStart.forEach(callback -> callback.accept(this.flame));
    }

    public PluginLogger logger() {
        return this.pluginLogger;
    }

    public Path dataFolder() {
        return this.dataFolder;
    }

    public LangService lang() {
        return this.lang;
    }

    /**
     * Restarts the entire RustyConnector kernel by exhausting the current {@link Flame} and igniting a new one.
     */
    public void rekindle() {
        try {
            this.onStop.forEach(Runnable::run);

            this.exhaust(this.plugin);
            this.flame = null;

            this.ignite();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Kill the {@link Flame}.
     * Typically good for if you want to ignite a new one.
     */
    public void exhaust(VelocityRustyConnector plugin) {
        Tinder.get().velocityServer().getEventManager().unregisterListeners(plugin);
        if(this.flame == null) return;
        this.flame.kill();
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
     * Gets a resource by id and returns it as a stream.
     * @param filename The id of the resource to get.
     * @return The resource as a stream.
     */
    public static InputStream resourceAsStream(String filename)  {
        return IMCLoaderTinder.class.getClassLoader().getResourceAsStream(filename);
    }

    /**
     * Get the velocity server
     */
    public ProxyServer velocityServer() {
        return this.server;
    }

    /**
     * Creates new {@link Tinder} based on the gathered resources.
     */
    public static Tinder gather(VelocityRustyConnector plugin, ProxyServer server, Logger logger, @DataDirectory Path dataFolder) {
        try {
            PluginLogger pluginLogger = new PluginLogger(logger);
            RootLanguageConfig config = RootLanguageConfig.construct(dataFolder);
            LangService langService = LangService.resolveLanguageCode(config.getLanguage(), dataFolder);

            return new Tinder(plugin, server, pluginLogger, dataFolder, langService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <TFlame extends VelocityFlame<?>> void onStart(Consumer<TFlame> callback) {
        this.onStart.add((Consumer<Flame>) callback);
        if(this.flame != null) callback.accept((TFlame) this.flame);
    }

    @Override
    public void onStop(Runnable callback) {
        this.onStop.add(callback);
    }
}
