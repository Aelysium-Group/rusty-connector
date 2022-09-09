package group.aelysium.rustyconnector.plugin.velocity.lib.parser.v001;


import group.aelysium.rustyconnector.core.lib.generic.Lang;
import group.aelysium.rustyconnector.core.lib.generic.parsing.YAML;
import group.aelysium.rustyconnector.core.lib.generic.util.logger.GateKey;
import group.aelysium.rustyconnector.core.lib.generic.util.logger.LoggerGate;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.generic.Config;
import ninja.leaping.configurate.ConfigurationNode;

public class LoggerParser {
    public static void parse(Config config) throws IllegalAccessException {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        plugin.logger().log("-----| Registering Logger Gateways...");

        if(!config.register()) throw new RuntimeException("Unable to register logger.yml");

        ConfigurationNode configData = config.getData();

        LoggerGate gate = plugin.logger().getGate();

        gate.registerNode(
                GateKey.REGISTRATION_REQUEST,
                YAML.get(configData,"messaging.registration-request").getBoolean()
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

        LoggerParser.parseLang(config);

        plugin.logger().log("-----| Finished!");
    }

    public static void parseLang(Config config) {
        ConfigurationNode configData = config.getData();

        Lang.add("request-registration_icon",YAML.get(configData,"console-icons.requesting-registration").getString());
        Lang.add("registered_icon",YAML.get(configData,"console-icons.registered").getString());
        Lang.add("requesting-unregistration_icon",YAML.get(configData,"console-icons.requesting-unregistration").getString());
        Lang.add("unregistered_icon",YAML.get(configData,"console-icons.unregistered").getString());
        Lang.add("canceled_icon",YAML.get(configData,"console-icons.canceled-request").getString());
        Lang.add("call-for-registration_icon",YAML.get(configData,"console-icons.call-for-registration").getString());
    }

}