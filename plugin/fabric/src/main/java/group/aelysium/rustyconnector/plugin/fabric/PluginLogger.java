package group.aelysium.rustyconnector.plugin.fabric;

import group.aelysium.rustyconnector.toolkit.common.log_gate.GateKey;
import group.aelysium.rustyconnector.toolkit.common.log_gate.LoggerGate;
import group.aelysium.rustyconnector.plugin.fabric.central.Tinder;
import net.kyori.adventure.text.Component;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

public class PluginLogger implements group.aelysium.rustyconnector.toolkit.common.logger.PluginLogger {
    private final LoggerGate gate = new LoggerGate();
    private final Logger logger;

    public PluginLogger(Logger logger) {
        this.logger = logger;
        this.gate.registerNode(GateKey.SAVE_TRASH_MESSAGES, true);
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
            ((MinecraftServer) Tinder.get().server()).sendMessage(message);
        } catch (Exception ignore) {}
    }
}
