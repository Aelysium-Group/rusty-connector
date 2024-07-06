package group.aelysium.rustyconnector.plugin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import group.aelysium.rustyconnector.common.lang.config.RootLanguageConfig;
import group.aelysium.rustyconnector.proxy.ProxyFlame;
import group.aelysium.rustyconnector.toolkit.proxy.lang.ProxyLang;
import group.aelysium.rustyconnector.toolkit.RustyConnector;
import group.aelysium.rustyconnector.toolkit.proxy.util.Version;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.UUID;

public class VelocityRustyConnector {
    private final Metrics.Factory metricsFactory;
    private final PluginLogger logger;
    private final ProxyServer server;
    private final Path dataFolder;

    @Inject
    public VelocityRustyConnector(ProxyServer server, Logger logger, @DataDirectory Path dataFolder, Metrics.Factory metricsFactory) {
        this.logger = new PluginLogger(logger);
        this.server = server;
        this.metricsFactory = metricsFactory;
        this.dataFolder = dataFolder;
    }

    @Subscribe
    public void onLoad(ProxyInitializeEvent event) {
        this.logger.log("Initializing RustyConnector...");

        if(!this.server.getConfiguration().isOnlineMode())
            logger.log("Offline mode detected");

        try {
            metricsFactory.make(this, 17972);
            this.logger.log("Registered to bstats!");
        } catch (Exception e) {
            e.printStackTrace();
            this.logger.log("Failed to registerProxy to bstats!");
        }

        RootLanguageConfig config = RootLanguageConfig.construct(dataFolder);

        RustyConnector.Toolkit.registerProxy(
            new ProxyFlame.Tinder(
                UUID.randomUUID(),
                new Version("0.0.0"),
                new VelocityProxyAdapter(this.server),
            ).flux()
        );

        try {
            RustyConnector.Toolkit.Proxy().orElseThrow().access().get();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        ProxyLang.WORDMARK_RUSTY_CONNECTOR.send(this.logger, "v"+Tinder.get().flame().version().toString());

        if(!this.server.getConfiguration().isOnlineMode())
            this.logger.send(ProxyLang.BOXED_MESSAGE_COLORED.build("Your network is running in offline mode! YOU WILL RECEIVE NO SUPPORT AT ALL WITH RUSTYCONNECTOR!", NamedTextColor.RED));

        // Velocity requires that at least one server is always defined in velocity.toml
        if(this.server.getConfiguration().getServers().size() > 1)
            this.logger.send(ProxyLang.BOXED_COMPONENT_COLORED.build(
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
            this.kernel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
