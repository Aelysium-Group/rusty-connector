package group.aelysium.rustyconnector.toolkit.core.server;

public enum Purpose {
    /**
     * Has no specific purpose other than allowing players to use it.
     */
    GENERIC,

    /**
     * Server is a member of a Ranked family and should provide additional interfaces for matchmaking specific needs.
     */
    MATCHED,
}
