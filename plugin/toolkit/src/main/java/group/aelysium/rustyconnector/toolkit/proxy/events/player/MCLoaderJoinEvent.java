package group.aelysium.rustyconnector.toolkit.proxy.events.player;

import group.aelysium.rustyconnector.toolkit.common.events.Event;
import group.aelysium.rustyconnector.toolkit.proxy.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.proxy.family.mcloader.MCLoader;

/**
 * Represents a player joining an MCLoader.
 * This event will only fire once {@link FamilyPostJoinEvent} has fired.
 */
public class MCLoaderJoinEvent implements Event {
    protected final MCLoader mcLoader;
    protected final IPlayer player;

    public MCLoaderJoinEvent(MCLoader mcLoader, IPlayer player) {
        this.mcLoader = mcLoader;
        this.player = player;
    }

    public MCLoader mcLoader() {
        return mcLoader;
    }
    public IPlayer player() {
        return player;
    }
}