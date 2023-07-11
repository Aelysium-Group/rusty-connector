package group.aelysium.rustyconnector.plugin.velocity.lib.parties;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
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

    public Player getSender() {
        return this.sender.get();
    }
    public Player getTarget() {
        return this.target;
    }


    /**
     * Accept the party invite.
     * This will subsequently connect the player to the party's server and then decompose the invite and remove it from the PartyService that it belongs to.
     */
    public synchronized void accept() {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PartyService partyService = api.getService(PartyService.class).orElse(null);
        if(partyService == null)
            throw new IllegalStateException("The party module is disabled!");

        if(this.isAcknowledged != null)
            throw new IllegalStateException("This invite has already been acknowledged! You should close it using `PartyService#closeInvite`");
        if(this.party.get() == null)
            throw new IllegalStateException("This party no longer exists!");
        if(this.sender.get() == null)
            throw new IllegalStateException("The sender is no-longer online!");
        if(!Objects.requireNonNull(this.sender.get()).isActive())
            throw new IllegalStateException("The sender is no-longer online!");

        if(partyService.getSettings().onlyLeaderCanInvite())
            if(!Objects.requireNonNull(party.get()).getLeader().equals(getSender()))
                throw new IllegalStateException("The leader that invited you to their party is either no longer in it or isn't the leader anymore!");
        else
            if(!Objects.requireNonNull(party.get()).players().contains(sender.get()))
                throw new IllegalStateException("The member that invited you to their party is no longer in it!");

        Objects.requireNonNull(this.party.get()).getServer().connect(this.target);
        Objects.requireNonNull(this.party.get()).getServer().playerJoined();
        partyService.closeInvite(this);
        this.isAcknowledged = true;
    }

    /**
     * Deny the party invite.
     * This will subsequently decompose the invite and remove it from the PartyService that it belongs to.
     */
    public synchronized void deny() {
        if(this.isAcknowledged != null) throw new IllegalStateException("This invite has already been acknowledged! You should close it using `PartyService#closeInvite`");

        PartyService partyService = VelocityRustyConnector.getAPI().getService(PartyService.class).orElse(null);
        if(partyService == null)
            throw new IllegalStateException("The party module is disabled!");

        partyService.closeInvite(this);
        this.isAcknowledged = true;
    }

    public synchronized void decompose() {
        this.sender.clear();
        this.party.clear();
        this.target = null;
    }
}