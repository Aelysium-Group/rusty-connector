package group.aelysium.rustyconnector.toolkit.proxy.events.player;

import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.common.events.Event;
import group.aelysium.rustyconnector.toolkit.proxy.family.Family;
import group.aelysium.rustyconnector.toolkit.proxy.family.mcloader.MCLoader;
import group.aelysium.rustyconnector.toolkit.proxy.player.IPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a player leaving a family.
 * This event will only fire once {@link FamilyPostJoinEvent} has fired.
 * It can be assumed that if this event fires, the player has successfully acquired a new origin.
 * This event will also fire if a player leaves the family by logging out of the network.
 */
public class FamilyLeaveEvent implements Event {
    protected final Particle.Flux<Family> family;
    protected final MCLoader mcLoader;
    protected final IPlayer player;
    protected final boolean disconnected;

    public FamilyLeaveEvent(
            @NotNull Particle.Flux<Family> family,
            @NotNull MCLoader mcLoader,
            @NotNull IPlayer player,
            boolean disconnected
    ) {
        this.family = family;
        this.mcLoader = mcLoader;
        this.player = player;
        this.disconnected = disconnected;
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
    public boolean disconnected() {
        return disconnected;
    }
}