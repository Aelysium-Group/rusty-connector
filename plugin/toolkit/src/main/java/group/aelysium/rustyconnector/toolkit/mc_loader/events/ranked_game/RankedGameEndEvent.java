package group.aelysium.rustyconnector.toolkit.mc_loader.events.ranked_game;

import group.aelysium.rustyconnector.toolkit.common.events.Event;

import java.util.List;
import java.util.UUID;

/**
 * Represents a game ending.
 */
public class RankedGameEndEvent implements Event {
    private final UUID uuid;
    private final List<UUID> losers;
    private final List<UUID> winners;
    private final boolean tied;

    public RankedGameEndEvent(UUID uuid, List<UUID> winners, List<UUID> losers, boolean tied) {
        this.uuid = uuid;
        this.losers = losers;
        this.winners = winners;
        this.tied = tied;
    }

    public UUID sessionUUID() {
        return uuid;
    }
    public List<UUID> losers() {
        return losers;
    }
    public List<UUID> winners() {
        return winners;
    }
    public boolean tied() {
        return tied;
    }
}