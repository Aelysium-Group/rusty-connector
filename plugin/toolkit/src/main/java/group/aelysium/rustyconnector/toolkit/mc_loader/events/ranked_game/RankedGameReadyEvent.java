package group.aelysium.rustyconnector.toolkit.mc_loader.events.ranked_game;

import group.aelysium.rustyconnector.toolkit.core.events.Event;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a game starting.
 */
public class RankedGameReadyEvent implements Event {
    private final UUID uuid;
    private final Map<UUID, String> players;

    public RankedGameReadyEvent(UUID uuid, Map<UUID, String> players) {
        this.uuid = uuid;
        this.players = players;
    }

    public UUID gameUUID() {
        return uuid;
    }
    public Map<UUID, String> players() {
        return players;
    }
}