package group.aelysium.rustyconnector.core.lib.util.logger;

public enum GateKey {
    // Messaging
    REGISTRATION_REQUEST,
    UNREGISTRATION_REQUEST,
    CALL_FOR_REGISTRATION,
    PING,
    PONG,
    PLAYER_COUNT_UPDATE,
    MESSAGE_PARSER_TRASH,


    // Security
    INVALID_PRIVATE_KEY,
    BLACKLISTED_ADDRESS_MESSAGE,
    WHITELIST_DENIED_ADDRESS_MESSAGE,

    // Log
    PLAYER_JOIN,
    PLAYER_LEAVE,
    PLAYER_MOVE,
    FAMILY_BALANCING
}
