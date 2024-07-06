package group.aelysium.rustyconnector.toolkit.proxy.events.player;

import group.aelysium.rustyconnector.toolkit.common.events.Event;
import group.aelysium.rustyconnector.toolkit.proxy.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.proxy.family.mcloader.MCLoader;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a player leaving an MCLoader.
 * This event will only fire once {@link FamilyLeaveEvent} has fired.
 * It can be assumed that if this event fires, the player has successfully acquired a new origin.
 * This event will also fire if a player leaves the family by logging out of the network.
 */
public class MCLoaderLeaveEvent implements Event {
    protected final MCLoader mcLoader;
    protected final IPlayer player;
    protected final boolean disconnected;

    public MCLoaderLeaveEvent(
            @NotNull MCLoader mcLoader,
            @NotNull IPlayer player,
            boolean disconnected
    ) {
        this.mcLoader = mcLoader;
        this.player = player;
        this.disconnected = disconnected;
    }

    public MCLoader mcLoader() {
        return mcLoader;
    }
    public IPlayer player() {
        return player;
    }
    public boolean disconnected() {
        return disconnected;
    }
}