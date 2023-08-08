package group.aelysium.rustyconnector.plugin.paper;

import group.aelysium.rustyconnector.core.lib.lang_messaging.LoggerGate;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

public class PluginLogger implements group.aelysium.rustyconnector.core.central.PluginLogger {
    private final LoggerGate gate = new LoggerGate();
    private final Logger logger;

    public PluginLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public LoggerGate loggerGate() {
        return this.gate;
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

    @Override
    public void send(Component message) {
        PaperAPI.get().paperServer().getConsoleSender().sendMessage(message);
    }
}
