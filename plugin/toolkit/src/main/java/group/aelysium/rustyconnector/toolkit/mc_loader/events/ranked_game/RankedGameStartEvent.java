package group.aelysium.rustyconnector.toolkit.mc_loader.events.ranked_game;

import group.aelysium.rustyconnector.toolkit.core.events.Cancelable;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

import java.util.List;
import java.util.UUID;

/**
 * Represents a game starting.
 */
public class RankedGameStartEvent extends Cancelable {
    private final UUID uuid;
    private final List<UUID> players;

    public RankedGameStartEvent(UUID uuid, List<UUID> players) {
        this.uuid = uuid;
        this.players = players;
    }

    public UUID gameUUID() {
        return uuid;
    }
    public List<UUID> players() {
        return players;
    }
}