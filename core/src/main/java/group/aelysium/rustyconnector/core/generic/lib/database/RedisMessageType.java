package group.aelysium.rustyconnector.core.generic.lib.database;

public enum RedisMessageType {
    PING,
    PONG,
    REQ_REG, // Proxy > Server | An outbound request for servers to register themselves
    REG, // Server > Proxy | A server's response to the REG_OUT message. This is also used when a server boots up and needs to register itself.
    UNREG, // Server > Proxy | A server's message to the proxy when it needs to un-register itself.
    PLAYER_CNT, // Server > Proxy | A player count update from the server to the proxy
    PLAYER_DISCON, // Server > Proxy | When a player disconnects from the network

}
