package group.aelysium.rustyconnector.plugin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import group.aelysium.rustyconnector.toolkit.RustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.nio.file.Path;

public class VelocityRustyConnector {
    private final Metrics.Factory metricsFactory;
    private final Tinder tinder;

    @Inject
    public VelocityRustyConnector(ProxyServer server, Logger logger, @DataDirectory Path dataFolder, Metrics.Factory metricsFactory) {
        this.tinder = Tinder.gather(this, server, logger, dataFolder);

        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onLoad(ProxyInitializeEvent event) {
        Tinder.get().logger().log("Initializing RustyConnector...");

        if(!Tinder.get().velocityServer().getConfiguration().isOnlineMode())
            Tinder.get().logger().log("Offline mode detected");

        try {
            this.tinder.ignite();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
            metricsFactory.make(this, 17972);
            Tinder.get().logger().log("Registered to bstats!");
        } catch (Exception e) {
            e.printStackTrace();
            Tinder.get().logger().log("Failed to register to bstats!");
        }

        ProxyLang.WORDMARK_RUSTY_CONNECTOR.send(Tinder.get().logger(), "v"+Tinder.get().flame().version().toString());
        RustyConnector.Toolkit.register(Tinder.get());

        if(!Tinder.get().velocityServer().getConfiguration().isOnlineMode())
            Tinder.get().logger().send(ProxyLang.BOXED_MESSAGE_COLORED.build("Your network is running in offline mode! YOU WILL RECEIVE NO SUPPORT AT ALL WITH RUSTYCONNECTOR!", NamedTextColor.RED));

        // Velocity requires that at least one server is always defined in velocity.toml
        if(Tinder.get().velocityServer().getConfiguration().getServers().size() > 1)
            Tinder.get().logger().send(ProxyLang.BOXED_COMPONENT_COLORED.build(
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
            RustyConnector.Toolkit.unregister();
            this.tinder.exhaust(this);
        } catch (Exception e) {
            Tinder.get().logger().log("RustyConnector: " + e.getMessage());
        }
    }
}
