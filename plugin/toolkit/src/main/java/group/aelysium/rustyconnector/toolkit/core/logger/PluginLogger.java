package group.aelysium.rustyconnector.toolkit.core.logger;

import group.aelysium.rustyconnector.toolkit.core.log_gate.LoggerGate;
import net.kyori.adventure.text.Component;

public interface PluginLogger {
    LoggerGate loggerGate();

    void log(String message);
    void log(String message, Throwable e);

    void debug(String message);
    void debug(String message, Throwable e);

    void warn(String message);
    void warn(String message, Throwable e);
    void error(String message);
    void error(String message, Throwable e);
    void send(Component message);
}