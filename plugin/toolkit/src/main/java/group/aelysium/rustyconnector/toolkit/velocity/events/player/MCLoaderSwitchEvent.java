package group.aelysium.rustyconnector.toolkit.velocity.events.player;

import group.aelysium.rustyconnector.toolkit.core.events.Event;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

/**
 * Represents a player switching from one mcloader to another.
 * This event doesn't care about what family the mcloaders are a part of.
 */
public class MCLoaderSwitchEvent implements Event {
    protected final IMCLoader oldMCLoader;
    protected final IMCLoader newMCLoader;
    protected final IPlayer player;

    public MCLoaderSwitchEvent(IMCLoader oldMCLoader, IMCLoader newMCLoader, IPlayer player) {
        this.oldMCLoader = oldMCLoader;
        this.newMCLoader = newMCLoader;
        this.player = player;
    }

    public IMCLoader oldMCLoader() {
        return oldMCLoader;
    }
    public IMCLoader newMCLoader() {
        return newMCLoader;
    }
    public IPlayer player() {
        return player;
    }
}