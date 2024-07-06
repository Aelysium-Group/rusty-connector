package group.aelysium.rustyconnector.toolkit.proxy.events.player;

import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.common.events.Event;
import group.aelysium.rustyconnector.toolkit.proxy.family.Family;
import group.aelysium.rustyconnector.toolkit.proxy.family.mcloader.MCLoader;
import group.aelysium.rustyconnector.toolkit.proxy.player.IPlayer;

/**
 * Represents a player successfully connecting to a family.
 */
public class FamilyPostJoinEvent implements Event {
    protected final Particle.Flux<Family> family;
    protected final MCLoader mcLoader;
    protected final IPlayer player;

    public FamilyPostJoinEvent(Particle.Flux<Family> family, MCLoader mcLoader, IPlayer player) {
        this.family = family;
        this.mcLoader = mcLoader;
        this.player = player;
    }

    public Particle.Flux<Family> family() {
        return family;
    }
    public MCLoader mcLoader() {
        return mcLoader;
    }
    public IPlayer player() {
        return player;
    }
}