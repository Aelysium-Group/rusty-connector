package group.aelysium.rustyconnector.plugin.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

public class PluginLogger {
    private final ProxyServer server;
    private final Logger logger;

    public PluginLogger(Logger logger, ProxyServer server) {
        this.logger = logger;
        this.server = server;
    }

    public void log(String message) {
        logger.info(message);
    }

    public void error(String message, Throwable e) {
        logger.error(message, e);
    }

    public void send(Component message) {
        try {
            this.server.getConsoleCommandSource().sendMessage(message);
        } catch (Exception ignore) {}
    }
}
