package group.aelysium.rustyconnector.toolkit.mc_loader.events.ranked_game;

import group.aelysium.rustyconnector.toolkit.core.events.Event;

import java.util.List;
import java.util.UUID;

/**
 * Represents a game ending.
 */
public class RankedGameEndEvent implements Event {
    private final UUID uuid;
    private final List<UUID> losers;
    private final List<UUID> winners;

    public RankedGameEndEvent(UUID uuid, List<UUID> losers, List<UUID> winners) {
        this.uuid = uuid;
        this.losers = losers;
        this.winners = winners;
    }

    public UUID gameUUID() {
        return uuid;
    }
    public List<UUID> losers() {
        return losers;
    }
    public List<UUID> winners() {
        return winners;
    }
}