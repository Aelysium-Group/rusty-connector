package group.aelysium.rustyconnector.api.velocity.central;

import group.aelysium.rustyconnector.api.core.logger.PluginLogger;
import group.aelysium.rustyconnector.api.velocity.log_gate.LoggerGate;
import net.kyori.adventure.text.Component;

public interface VelocityPluginLogger extends PluginLogger {
    LoggerGate loggerGate();
}