package group.aelysium.rustyconnector.plugin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import group.aelysium.rustyconnector.core.lib.exception.DuplicateLifecycleException;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityLifecycle;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.bstats.Metrics;
import group.aelysium.rustyconnector.core.central.PluginRuntime;
import org.slf4j.Logger;

import java.nio.file.Path;

public class VelocityRustyConnector implements PluginRuntime {
    private static VelocityLifecycle lifecycle;
    private static VelocityAPI api;
    public static VelocityAPI getAPI() {
        return api;
    }
    public static VelocityLifecycle getLifecycle() {
        return lifecycle;
    }

    @Inject
    public VelocityRustyConnector(ProxyServer server, Logger logger, @DataDirectory Path dataFolder, Metrics.Factory metricsFactory) {
        api = new VelocityAPI(this, server, logger, dataFolder);
        lifecycle = new VelocityLifecycle();
        try {
            metricsFactory.make(this, 17972);
        } catch (Exception e) {
            VelocityRustyConnector.getAPI().getLogger().log("Failed to register to bstats!");
        }
    }

    @Subscribe
    public void onLoad(ProxyInitializeEvent event) throws DuplicateLifecycleException {
        if(!lifecycle.start()) lifecycle.stop();
    }

    @Subscribe
    public void onUnload(ProxyShutdownEvent event) {
        try {
            lifecycle.stop();
        } catch (Exception e) {
            VelocityRustyConnector.getAPI().getLogger().log("RustyConnector: " + e.getMessage());
        }
    }
}
