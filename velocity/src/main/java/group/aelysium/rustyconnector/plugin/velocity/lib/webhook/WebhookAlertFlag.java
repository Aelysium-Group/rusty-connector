package group.aelysium.rustyconnector.plugin.velocity.lib.webhook;

public enum WebhookAlertFlag {
    REGISTER_ALL,           // PROXY
    SERVER_REGISTER,        // PROXY, FAMILY
    SERVER_UNREGISTER,      // PROXY, FAMILY
    PLAYER_JOIN,            // PROXY, FAMILY
    PLAYER_LEAVE,           // PROXY, FAMILY
    PLAYER_JOIN_FAMILY,     // PROXY
    PLAYER_LEAVE_FAMILY,    // PROXY
    PLAYER_SWITCH_SERVER,   // PROXY
    PLAYER_SWITCH_FAMILY,   // PROXY
    PLAYER_SWITCH,          // FAMILY
    DISCONNECT_CATCH        // FAMILY
}