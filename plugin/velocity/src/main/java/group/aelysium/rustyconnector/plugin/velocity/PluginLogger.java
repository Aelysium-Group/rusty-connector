package group.aelysium.rustyconnector.plugin.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import group.aelysium.rustyconnector.toolkit.common.logger.IPluginLogger;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

public class PluginLogger implements IPluginLogger {
    private final ProxyServer server;
    private final Logger logger;

    public PluginLogger(Logger logger, ProxyServer server) {
        this.logger = logger;
        this.server = server;
    }

    @Override
    public void log(String message) {
        logger.info(message);
    }

    @Override
    public void log(String message, Throwable e) {
        logger.info(message, e);
    }
    @Override
    public void debug(String message) {
        logger.info(message);
    }

    @Override
    public void debug(String message, Throwable e) {
        logger.debug(message, e);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void warn(String message, Throwable e) {
        logger.warn(message, e);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

    @Override
    public void error(String message, Throwable e) {
        logger.error(message, e);
    }

    public void send(Component message) {
        try {
            this.server.getConsoleCommandSource().sendMessage(message);
        } catch (Exception ignore) {}
    }
}
