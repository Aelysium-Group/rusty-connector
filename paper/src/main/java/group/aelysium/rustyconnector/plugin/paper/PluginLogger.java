package group.aelysium.rustyconnector.plugin.paper;

import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.slf4j.Logger;

public class PluginLogger {
    private final Server server;
    private final Logger logger;

    public PluginLogger(Logger logger, Server server) {
        this.logger = logger;
        this.server = server;
    }

    public void log(String message) {
        logger.info(message);
    }

    public void log(String message, Throwable e) {
        logger.info(message, e);
    }

    public void debug(String message) {
        logger.info(message);
    }

    public void debug(String message, Throwable e) {
        logger.debug(message, e);
    }

    public void warn(String message) {
        logger.warn(message);
    }

    public void warn(String message, Throwable e) {
        logger.warn(message, e);
    }

    public void error(String message) {
        logger.error(message);
    }

    public void error(String message, Throwable e) {
        logger.error(message, e);
    }

    public void send(Component message) {
        try {
            server.getConsoleSender().sendMessage(message);
        } catch (Exception ignore) {}
    }
}
