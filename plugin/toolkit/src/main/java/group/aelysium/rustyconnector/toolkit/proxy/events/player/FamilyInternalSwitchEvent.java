package group.aelysium.rustyconnector.toolkit.proxy.events.player;

import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.common.events.Event;
import group.aelysium.rustyconnector.toolkit.proxy.family.Family;
import group.aelysium.rustyconnector.toolkit.proxy.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.proxy.family.mcloader.MCLoader;

/**
 * Represents a player switching from one MCLoader in a family to another MCLoader in that same family.
 */
public class FamilyInternalSwitchEvent implements Event {
    protected final Particle.Flux<Family> family;
    protected final MCLoader previousMCLoader;
    protected final MCLoader newMCLoader;
    protected final IPlayer player;

    public FamilyInternalSwitchEvent(Particle.Flux<Family> family, MCLoader previousMCLoader, MCLoader newMCLoader, IPlayer player) {
        this.family = family;
        this.previousMCLoader = previousMCLoader;
        this.newMCLoader = newMCLoader;
        this.player = player;
    }

    public Particle.Flux<Family> family() {
        return family;
    }
    public MCLoader previousMCLoader() {
        return previousMCLoader;
    }
    public MCLoader newMCLoader() {
        return newMCLoader;
    }
    public IPlayer player() {
        return player;
    }
}