package group.aelysium.rustyconnector.toolkit.mc_loader.events.ranked_game;

import group.aelysium.rustyconnector.toolkit.common.events.Event;
import group.aelysium.rustyconnector.toolkit.mc_loader.ranked_game_interface.MCLoaderMatchPlayer;

import java.util.Map;
import java.util.UUID;

/**
 * Represents a game starting.
 */
public class RankedGameReadyEvent implements Event {
    private final UUID uuid;
    private final Map<UUID, MCLoaderMatchPlayer> players;

    public RankedGameReadyEvent(UUID uuid, Map<UUID, MCLoaderMatchPlayer> players) {
        this.uuid = uuid;
        this.players = players;
    }

    public UUID gameUUID() {
        return uuid;
    }
    public Map<UUID, MCLoaderMatchPlayer> players() {
        return players;
    }
}