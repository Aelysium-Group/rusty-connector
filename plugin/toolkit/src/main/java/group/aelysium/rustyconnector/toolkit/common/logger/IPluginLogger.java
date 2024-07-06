package group.aelysium.rustyconnector.toolkit.common.logger;

import net.kyori.adventure.text.Component;

public interface IPluginLogger {
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