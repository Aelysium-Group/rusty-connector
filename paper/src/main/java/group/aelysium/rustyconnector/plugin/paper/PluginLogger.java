package group.aelysium.rustyconnector.plugin.paper;

import com.jcabi.log.MulticolorLayout;
import org.slf4j.Logger;

public class PluginLogger implements group.aelysium.rustyconnector.core.Logger {
    private final Logger logger;

    public PluginLogger(Logger logger) {
        this.logger = logger;
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
}
