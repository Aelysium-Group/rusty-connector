package group.aelysium.rustyconnector.plugin.velocity;

import group.aelysium.rustyconnector.core.lib.lang_messaging.*;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.LoggerConfig;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

public class PluginLogger implements group.aelysium.rustyconnector.core.lib.lang_messaging.Logger {
    private final LoggerGate gate = new LoggerGate();
    private final Logger logger;

    public PluginLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public LoggerGate getGate() {
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
        VelocityRustyConnector.getInstance().getVelocityServer().getConsoleCommandSource().sendMessage(message);
    }

    public static void init(LoggerConfig config) {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        LoggerGate gate = plugin.logger().getGate();

        gate.registerNode(
                GateKey.REGISTRATION_REQUEST,
                config.isMessaging_registrationRequest()
        );
        gate.registerNode(
                GateKey.UNREGISTRATION_REQUEST,
                config.isMessaging_unregistrationRequest()
        );
        gate.registerNode(
                GateKey.CALL_FOR_REGISTRATION,
                config.isMessaging_callForRegistration()
        );
        gate.registerNode(
                GateKey.PING,
                config.isMessaging_ping()
        );
        gate.registerNode(
                GateKey.PONG,
                config.isMessaging_pong()
        );
        gate.registerNode(
                GateKey.MESSAGE_PARSER_TRASH,
                config.isMessaging_messageParserTrash()
        );

        gate.registerNode(
                GateKey.BLACKLISTED_ADDRESS_MESSAGE,
                config.isSecurity_blacklistedAddressMessage()
        );
        gate.registerNode(
                GateKey.WHITELIST_DENIED_ADDRESS_MESSAGE,
                config.isSecurity_whitelistDeniedAddressMessage()
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
