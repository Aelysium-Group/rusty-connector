package group.aelysium.rustyconnector.plugin.velocity.lib.parties;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class PartyInvite {
    private final WeakReference<Party> party;
    private final WeakReference<Player> sender;
    private Player target;
    private Boolean isAcknowledged = null;

    public PartyInvite(Party party, Player sender, Player target) {
        this.party = new WeakReference<>(party);
        this.sender = new WeakReference<>(sender);
        this.target = target;
    }

    public Player sender() {
        return this.sender.get();
    }
    public Player target() {
        return this.target;
    }


    /**
     * Accept the party invite.
     * This will subsequently connect the player to the party's server and then decompose the invite and remove it from the PartyService that it belongs to.
     */
    public synchronized void accept() {
        VelocityAPI api = VelocityAPI.get();
        PartyService partyService = api.services().partyService().orElse(null);
        if(partyService == null)
            throw new IllegalStateException("The party module is disabled!");

        if(this.isAcknowledged != null)
            throw new IllegalStateException("This invite has already been acknowledged! You should close it using `PartyService#closeInvite`");
        try {
            if (this.party.get() == null || Objects.requireNonNull(this.party.get()).isEmpty())
                throw new IllegalStateException("This invite has expired!");
        } catch (NullPointerException ignore) {
            throw new IllegalStateException("This invite has expired!");
        }
        if(this.sender.get() == null)
            throw new IllegalStateException("The sender is no-longer online!");
        if(!Objects.requireNonNull(this.sender.get()).isActive())
            throw new IllegalStateException("The sender is no-longer online!");

        if(partyService.settings().onlyLeaderCanInvite())
            if(!Objects.requireNonNull(party.get()).leader().equals(sender()))
                throw new IllegalStateException("The leader that invited you to their party is either no longer in it or isn't the leader anymore!");
        else
            if(!Objects.requireNonNull(party.get()).players().contains(sender.get()))
                throw new IllegalStateException("The member that invited you to their party is no longer in it!");

        Objects.requireNonNull(this.party.get()).join(this.target);
        partyService.closeInvite(this);
        this.isAcknowledged = true;
    }

    /**
     * Deny the party invite.
     * This will subsequently decompose the invite and remove it from the PartyService that it belongs to.
     */
    public synchronized void ignore() {
        if(this.isAcknowledged != null) throw new IllegalStateException("This invite has already been acknowledged! You should close it using `PartyService#closeInvite`");

        PartyService partyService = VelocityAPI.get().services().partyService().orElse(null);
        if(partyService == null) throw new IllegalStateException("The party module is disabled!");

        partyService.closeInvite(this);
        this.isAcknowledged = true;
    }

    public synchronized void decompose() {
        this.sender.clear();
        this.party.clear();
        this.target = null;
    }
}