package group.aelysium.rustyconnector;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import group.aelysium.rustyconnector.lib.generic.server.Proxy;
import org.slf4j.Logger;

@Plugin(
        id = "plugin-velocity",
        name = "RustyConnector",
        version = "1.0",
        url = "https://aelysium.group/",
        authors = {"sivin"}
)
public class RustyConnector {
    private Proxy proxy;
    private ProxyServer server;
    private Logger logger;

    @Inject
    public RustyConnector(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        logger.info("Hello there, it's a test plugin I made!");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
    }
}
