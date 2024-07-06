package group.aelysium.rustyconnector.toolkit.proxy.events.player;

import group.aelysium.rustyconnector.toolkit.common.events.Event;
import group.aelysium.rustyconnector.toolkit.proxy.family.IFamily;
import group.aelysium.rustyconnector.toolkit.proxy.family.ranked_family.IRankedFamily;
import group.aelysium.rustyconnector.toolkit.proxy.player.IPlayer;

/**
 * Represents a player attempting to connect to a family.
 * <p>
 * Please note, if the family that fires this is a {@link IRankedFamily}, this will fire
 * when the player is added to the matchmaker.
 * In such a case, {@link FamilyPostJoinEvent} won't be called until the player actually joins a game.
 */
public class FamilyPreJoinEvent implements Event {
    protected IFamily family;
    protected IPlayer player;

    public FamilyPreJoinEvent(IFamily family, IPlayer player) {
        this.family = family;
        this.player = player;
    }

    public IFamily family() {
        return family;
    }
    public IPlayer player() {
        return player;
    }
}