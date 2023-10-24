package group.aelysium.rustyconnector.plugin.fabric;

import group.aelysium.rustyconnector.api.velocity.log_gate.LoggerGate;
import group.aelysium.rustyconnector.core.plugin.Plugin;
import group.aelysium.rustyconnector.plugin.fabric.central.Tinder;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

public class PluginLogger implements group.aelysium.rustyconnector.api.velocity.central.PluginLogger {
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
        try {
            ((Tinder) Plugin.getAPI()).fabricServer().sendMessage(message);
        } catch (Exception ignore) {}
    }
}
