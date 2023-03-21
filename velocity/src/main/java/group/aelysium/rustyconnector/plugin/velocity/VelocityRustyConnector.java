package group.aelysium.rustyconnector.plugin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.Proxy;
import group.aelysium.rustyconnector.core.RustyConnector;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

public class VelocityRustyConnector implements RustyConnector {
    private final Metrics.Factory metricsFactory;
    private static RustyConnector instance;
    private Proxy proxy;
    private final ProxyServer server;
    private final PluginLogger logger;
    private final File dataFolder;

    /**
     * Set the proxy for Velocity. Once this is set it cannot be changed.
     * @param proxy The proxy to set.
     */
    public void setProxy(Proxy proxy) throws IllegalStateException {
        if(this.proxy != null) throw new IllegalStateException("This has already been set! You can't set this twice!");
        this.proxy = proxy;
    }
    public void unsetProxy() {
        this.proxy = null;
    }

    public static VelocityRustyConnector getInstance() { return (VelocityRustyConnector) instance; }
    public Proxy getProxy() { return this.proxy; }
    public ProxyServer getVelocityServer() { return this.server; }

    @Inject
    public VelocityRustyConnector(ProxyServer server, Logger logger, @DataDirectory Path dataFolder, Metrics.Factory metricsFactory) {
        this.server = server;
        this.logger = new PluginLogger(logger);
        this.dataFolder = dataFolder.toFile();
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onLoad(ProxyInitializeEvent event) {
        this.init();
    }

    @Subscribe
    public void onUnload(ProxyShutdownEvent event) {
        this.uninit();
    }

    public void init() {
        instance = this;

        int pluginId = 17972;
        Metrics metrics = metricsFactory.make(this, pluginId);

        if(!Engine.start()) uninit();
    }

    public void uninit() {
        Engine.stop();
    }

    @Override
    public void reload() {
        this.uninit();
        this.init();
    }

    @Override
    public PluginLogger logger() {
        return this.logger;
    }

    @Override
    public File getDataFolder() {
        return this.dataFolder;
    }

    @Override
    public InputStream getResourceAsStream(String filename) {
        return getClass().getClassLoader().getResourceAsStream(filename);
    }
}
