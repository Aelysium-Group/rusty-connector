package group.aelysium.rustyconnector.plugin.velocity.lib.parties;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.FakePlayer;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;

public class PartyInvite {
    private final WeakReference<Party> party;
    private FakePlayer sender;
    private FakePlayer target;
    private Boolean isAcknowledged = null;

    public PartyInvite(Party party, Player sender, Player target) {
        this.party = new WeakReference<>(party);
        this.sender = FakePlayer.from(sender);
        this.target = FakePlayer.from(target);
    }

    public FakePlayer sender() {
        return this.sender;
    }
    public FakePlayer target() {
        return this.target;
    }


    /**
     * Accept the party invite.
     * This will subsequently connect the player to the party's server and then decompose the invite and remove it from the PartyService that it belongs to.
     */
    public synchronized void accept() {
        Tinder api = Tinder.get();
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
        if(this.sender.resolve().isEmpty())
            throw new IllegalStateException("The sender is no-longer online!");

        if(partyService.settings().onlyLeaderCanInvite())
            if(!Objects.requireNonNull(party.get()).leader().equals(sender()))
                throw new IllegalStateException("The leader that invited you to their party is either no longer in it or isn't the leader anymore!");
        else
            if(!Objects.requireNonNull(party.get()).players().contains(sender))
                throw new IllegalStateException("The member that invited you to their party is no longer in it!");

        Optional<Player> player = this.target.resolve();
        if(player.isEmpty())
            throw new IllegalStateException("The target player isn't online!");

        Objects.requireNonNull(this.party.get()).join(player.orElseThrow());
        partyService.closeInvite(this);
        this.isAcknowledged = true;
    }

    /**
     * Deny the party invite.
     * This will subsequently decompose the invite and remove it from the PartyService that it belongs to.
     */
    public synchronized void ignore() {
        if(this.isAcknowledged != null) throw new IllegalStateException("This invite has already been acknowledged! You should close it using `PartyService#closeInvite`");

        PartyService partyService = Tinder.get().services().partyService().orElse(null);
        if(partyService == null) throw new IllegalStateException("The party module is disabled!");

        partyService.closeInvite(this);
        this.isAcknowledged = true;
    }

    public synchronized void decompose() {
        this.party.clear();
        this.target = null;
        this.sender = null;
    }
}