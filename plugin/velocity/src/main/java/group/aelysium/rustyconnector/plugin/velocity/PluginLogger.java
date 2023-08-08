package group.aelysium.rustyconnector.plugin.velocity;

import group.aelysium.rustyconnector.core.lib.lang_messaging.*;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.config.LoggerConfig;
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

    public void send(Component message) {
        VelocityAPI api = VelocityAPI.get();
        api.velocityServer().getConsoleCommandSource().sendMessage(message);
    }

    public static void init(LoggerConfig config) {
        PluginLogger pluginLogger = VelocityAPI.get().logger();

        LoggerGate gate = pluginLogger.loggerGate();

        gate.registerNode(
                GateKey.SAVE_TRASH_MESSAGES,
                config.shouldSaveTrashedMessages()
        );

        gate.registerNode(
                GateKey.REGISTRATION_ATTEMPT,
                config.isMessaging_registration()
        );
        gate.registerNode(
                GateKey.UNREGISTRATION_ATTEMPT,
                config.isMessaging_unregistration()
        );
        gate.registerNode(
                GateKey.PING,
                config.isMessaging_ping()
        );
        gate.registerNode(
                GateKey.MESSAGE_PARSER_TRASH,
                config.isMessaging_messageParserTrash()
        );

        gate.registerNode(
                GateKey.MESSAGE_TUNNEL_FAILED_MESSAGE,
                config.isSecurity_messageTunnelFailedMessage()
        );

        gate.registerNode(
                GateKey.PLAYER_JOIN,
                config.isLog_playerJoin()
        );
        gate.registerNode(
                GateKey.PLAYER_LEAVE,
                config.isLog_playerLeave()
        );
        gate.registerNode(
                GateKey.PLAYER_MOVE,
                config.isLog_playerMove()
        );
        gate.registerNode(
                GateKey.FAMILY_BALANCING,
                config.isLog_familyBalancing()
        );
    }
}
