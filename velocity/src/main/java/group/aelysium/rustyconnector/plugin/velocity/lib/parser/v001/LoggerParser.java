package group.aelysium.rustyconnector.plugin.velocity.lib.parser.v001;


import group.aelysium.rustyconnector.core.lib.util.logger.*;
import group.aelysium.rustyconnector.core.lib.parsing.YAML;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigFileLoader;
import ninja.leaping.configurate.ConfigurationNode;

public class LoggerParser {
    public static void parse(ConfigFileLoader configFileLoader) throws IllegalAccessException {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        plugin.logger().log("-----| Registering Logger Gateways...");

        if(!configFileLoader.register()) throw new RuntimeException("Unable to register logger.yml");

        ConfigurationNode configData = configFileLoader.getData();

        LoggerGate gate = plugin.logger().getGate();

        gate.registerNode(
                GateKey.REGISTRATION_REQUEST,
                YAML.get(configData,"messaging.registration-request").getBoolean()
        );
        gate.registerNode(
                GateKey.UNREGISTRATION_REQUEST,
                YAML.get(configData,"messaging.unregistration-request").getBoolean()
        );
        gate.registerNode(
                GateKey.CALL_FOR_REGISTRATION,
                YAML.get(configData,"messaging.call-for-registration").getBoolean()
        );
        gate.registerNode(
                GateKey.PING,
                YAML.get(configData,"messaging.ping").getBoolean()
        );
        gate.registerNode(
                GateKey.PONG,
                YAML.get(configData,"messaging.pong").getBoolean()
        );
        gate.registerNode(
                GateKey.PLAYER_COUNT_UPDATE,
                YAML.get(configData,"messaging.player-count-update").getBoolean()
        );
        gate.registerNode(
                GateKey.MESSAGE_PARSER_TRASH,
                YAML.get(configData,"messaging.message-parser-trash").getBoolean()
        );

        gate.registerNode(
                GateKey.INVALID_PRIVATE_KEY,
                YAML.get(configData,"security.invalid-private-key").getBoolean()
        );
        gate.registerNode(
                GateKey.BLACKLISTED_ADDRESS_MESSAGE,
                YAML.get(configData,"security.blacklisted-address-message").getBoolean()
        );
        gate.registerNode(
                GateKey.WHITELIST_DENIED_ADDRESS_MESSAGE,
                YAML.get(configData,"security.whitelist-denied-address-message").getBoolean()
        );

        gate.registerNode(
                GateKey.PLAYER_JOIN,
                YAML.get(configData,"log.player-join").getBoolean()
        );
        gate.registerNode(
                GateKey.PLAYER_LEAVE,
                YAML.get(configData,"log.player-leave").getBoolean()
        );
        gate.registerNode(
                GateKey.PLAYER_MOVE,
                YAML.get(configData,"log.player-move").getBoolean()
        );
        gate.registerNode(
                GateKey.FAMILY_BALANCING,
                YAML.get(configData,"log.family-balancing").getBoolean()
        );

        LoggerParser.parseLang(configFileLoader);

        plugin.logger().log("-----| Finished!");
    }

    public static void parseLang(ConfigFileLoader configFileLoader) {
        ConfigurationNode configData = configFileLoader.getData();

        Lang.add(
                LangKey.ICON_REQUEST_REGISTRATION,
                new LangEntry(YAML.get(configData,"console-icons.requesting-registration").getString())
        );
        Lang.add(
                LangKey.ICON_REGISTERED,
                new LangEntry(YAML.get(configData,"console-icons.registered").getString())
        );
        Lang.add(
                LangKey.ICON_REQUESTING_UNREGISTRATION,
                new LangEntry(YAML.get(configData,"console-icons.requesting-unregistration").getString())
        );
        Lang.add(
                LangKey.ICON_UNREGISTERED,
                new LangEntry(YAML.get(configData,"console-icons.unregistered").getString())
        );
        Lang.add(
                LangKey.ICON_CANCELED,
                new LangEntry(YAML.get(configData,"console-icons.canceled-request").getString())
        );
        Lang.add(
                LangKey.ICON_CALL_FOR_REGISTRATION,
                new LangEntry(YAML.get(configData,"console-icons.call-for-registration").getString())
        );
        Lang.add(
                LangKey.ICON_FAMILY_BALANCING,
                new LangEntry(YAML.get(configData,"console-icons.family-balancing").getString())
        );
        Lang.add(
                LangKey.ICON_PING,
                new LangEntry(YAML.get(configData,"console-icons.ping").getString())
        );
        Lang.add(
                LangKey.ICON_PONG,
                new LangEntry(YAML.get(configData,"console-icons.pong").getString())
        );
    }

}