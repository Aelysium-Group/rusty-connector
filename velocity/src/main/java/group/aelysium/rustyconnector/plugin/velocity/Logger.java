package group.aelysium.rustyconnector.plugin.velocity;

import java.util.logging.Level;
import org.slf4j.Logger;

public class PluginLogger implements rustyconnector.Logger {
    private final Logger logger;

    public PluginLogger(Logger logger) {
        this.logger = logger;
    }
    @Override
    public void log(String message) {
        logger.debug(message);
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
}
