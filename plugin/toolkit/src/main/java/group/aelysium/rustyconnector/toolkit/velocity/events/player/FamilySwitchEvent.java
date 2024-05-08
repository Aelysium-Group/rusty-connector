package group.aelysium.rustyconnector.toolkit.velocity.events.player;

import group.aelysium.rustyconnector.toolkit.core.events.Event;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

/**
 * Represents a player switching from one family to another family.
 * Specifically, this event will fire after {@link FamilyLeaveEvent} is fired on the previous family, and after {@link FamilyPostJoinEvent} fires on the new family.
 */
public class FamilySwitchEvent implements Event {
    protected final IFamily oldFamily;
    protected final IFamily newFamily;
    protected final IMCLoader oldMCLoader;
    protected final IMCLoader newMCLoader;
    protected final IPlayer player;

    public FamilySwitchEvent(IFamily oldFamily, IFamily newFamily, IMCLoader oldMCLoader, IMCLoader newMCLoader, IPlayer player) {
        this.oldFamily = oldFamily;
        this.newFamily = newFamily;
        this.oldMCLoader = oldMCLoader;
        this.newMCLoader = newMCLoader;
        this.player = player;
    }

    public IFamily oldFamily() {
        return oldFamily;
    }
    public IFamily newFamily() {
        return newFamily;
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