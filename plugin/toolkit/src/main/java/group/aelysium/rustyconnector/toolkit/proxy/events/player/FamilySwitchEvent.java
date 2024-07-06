package group.aelysium.rustyconnector.toolkit.proxy.events.player;

import group.aelysium.rustyconnector.toolkit.common.events.Event;
import group.aelysium.rustyconnector.toolkit.proxy.family.Family;
import group.aelysium.rustyconnector.toolkit.proxy.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.proxy.family.mcloader.MCLoader;
import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;

/**
 * Represents a player switching from one family to another family.
 * Specifically, this event will fire after {@link FamilyLeaveEvent} is fired on the previous family, and after {@link FamilyPostJoinEvent} fires on the new family.
 */
public class FamilySwitchEvent implements Event {
    protected final Particle.Flux<Family> oldFamily;
    protected final Particle.Flux<Family> newFamily;
    protected final MCLoader oldMCLoader;
    protected final MCLoader newMCLoader;
    protected final IPlayer player;

    public FamilySwitchEvent(Particle.Flux<Family> oldFamily, Particle.Flux<Family> newFamily, MCLoader oldMCLoader, MCLoader newMCLoader, IPlayer player) {
        this.oldFamily = oldFamily;
        this.newFamily = newFamily;
        this.oldMCLoader = oldMCLoader;
        this.newMCLoader = newMCLoader;
        this.player = player;
    }

    public Particle.Flux<Family> oldFamily() {
        return oldFamily;
    }
    public Particle.Flux<Family> newFamily() {
        return newFamily;
    }
    public MCLoader oldMCLoader() {
        return oldMCLoader;
    }
    public MCLoader newMCLoader() {
        return newMCLoader;
    }
    public IPlayer player() {
        return player;
    }
}