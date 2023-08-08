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
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.nio.file.Path;

public class VelocityRustyConnector {
    private final Metrics.Factory metricsFactory;
    private static VelocityLifecycle lifecycle;
    public static VelocityLifecycle lifecycle() {
        return lifecycle;
    }

    @Inject
    public VelocityRustyConnector(ProxyServer server, Logger logger, @DataDirectory Path dataFolder, Metrics.Factory metricsFactory) {
        new VelocityAPI(this, server, logger, dataFolder);
        lifecycle = new VelocityLifecycle();
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onLoad(ProxyInitializeEvent event) throws DuplicateLifecycleException {
        if(!VelocityAPI.get().velocityServer().getConfiguration().isOnlineMode())
            VelocityAPI.get().logger().log("Offline mode detected");

        if(!lifecycle.start()) lifecycle.stop();
        try {
            metricsFactory.make(this, 17972);
            VelocityAPI.get().logger().log("Registered to bstats!");
        } catch (Exception e) {
            VelocityAPI.get().logger().log("Failed to register to bstats!");
        }

        if(!VelocityAPI.get().velocityServer().getConfiguration().isOnlineMode())
            VelocityAPI.get().logger().send(VelocityLang.BOXED_MESSAGE_COLORED.build(Component.text("Your network is running in offline mode! YOU WILL RECEIVE NO SUPPORT AT ALL WITH RUSTYCONNECTOR!"), NamedTextColor.RED));

        // Velocity requires that at least one server is always defined in velocity.toml
        if(VelocityAPI.get().velocityServer().getConfiguration().getServers().size() > 1)
            VelocityAPI.get().logger().send(VelocityLang.BOXED_MESSAGE_COLORED.build(
                    Component.join(
                            JoinConfiguration.newlines(),
                            Component.text("Your network is identified as having multiple, pre-defined, non-RC servers, in it!"),
                            Component.text("Please note that you will receive no help in regards to making RC work with predefined servers!")
                    )
                    , NamedTextColor.RED));
    }

    @Subscribe
    public void onUnload(ProxyShutdownEvent event) {
        try {
            lifecycle.stop();
        } catch (Exception e) {
            VelocityAPI.get().logger().log("RustyConnector: " + e.getMessage());
        }
    }
}
