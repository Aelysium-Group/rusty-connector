package group.aelysium.rustyconnector.plugin.paper_legacy;

import group.aelysium.rustyconnector.plugin.paper_legacy.central.Tinder;
import group.aelysium.rustyconnector.toolkit.core.log_gate.GateKey;
import group.aelysium.rustyconnector.toolkit.core.log_gate.LoggerGate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Server;

import java.util.logging.Logger;

public class PluginLogger implements group.aelysium.rustyconnector.toolkit.core.logger.PluginLogger {
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
    public void log(String message, Throwable e) {logger.info(message + " " + e);}
    @Override
    public void debug(String message) {log(message);}

    @Override
    public void debug(String message, Throwable e) {log(message, e);}

    @Override
    public void warn(String message) {log(message);}

    @Override
    public void warn(String message, Throwable e) {log(message, e);}

    @Override
    public void error(String message) {log(message);}

    @Override
    public void error(String message, Throwable e) {log(message, e);}

    @Override
    public void send(Component message) {
        try {
            ((Server) Tinder.get().server()).getConsoleSender().sendMessage(PlainTextComponentSerializer.plainText().serialize(message));
        } catch (Exception ignore) {}
    }
}
