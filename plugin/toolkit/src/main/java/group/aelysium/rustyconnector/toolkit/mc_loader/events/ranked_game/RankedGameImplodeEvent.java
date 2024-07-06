package group.aelysium.rustyconnector.toolkit.mc_loader.events.ranked_game;

import group.aelysium.rustyconnector.toolkit.common.events.Event;

import java.util.UUID;

/**
 * Represents a game ending.
 */
public class RankedGameImplodeEvent implements Event {
    private final UUID uuid;
    private final String reason;

    public RankedGameImplodeEvent(UUID uuid, String reason) {
        this.uuid = uuid;
        this.reason = reason;
    }

    public UUID sessionUUID() {
        return uuid;
    }
    public String reason() {
        return reason;
    }
}