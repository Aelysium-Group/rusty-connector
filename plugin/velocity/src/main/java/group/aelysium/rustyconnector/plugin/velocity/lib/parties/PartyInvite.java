package group.aelysium.rustyconnector.plugin.velocity.lib.parties;

import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.toolkit.velocity.parties.IParty;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class PartyInvite implements group.aelysium.rustyconnector.toolkit.velocity.parties.IPartyInvite {
    private final PartyService partyService;
    private final WeakReference<IParty> party;
    private IPlayer sender;
    private IPlayer target;
    private Boolean isAcknowledged = null;

    public PartyInvite(PartyService partyService, IParty party, IPlayer sender, IPlayer target) {
        this.partyService = partyService;
        this.party = new WeakReference<>(party);
        this.sender = sender;
        this.target = target;
    }

    public IPlayer sender() {
        return this.sender;
    }
    public IPlayer target() {
        return this.target;
    }


    /**
     * Accept the party invite.
     * This will subsequently connect the player to the party's server and then decompose the invite and remove it from the PartyService that it belongs to.
     */
    public synchronized void accept() {
        if(this.isAcknowledged != null)
            throw new IllegalStateException(ProxyLang.PARTY_INJECTED_ACKNOWLEDGED);
        try {
            if (this.party.get() == null || Objects.requireNonNull(this.party.get()).isEmpty())
                throw new IllegalStateException(ProxyLang.PARTY_INJECTED_EXPIRED_INVITE);
        } catch (NullPointerException ignore) {
            throw new IllegalStateException(ProxyLang.PARTY_INJECTED_EXPIRED_INVITE);
        }
        if(this.sender.resolve().isEmpty())
            throw new IllegalStateException(ProxyLang.PARTY_INJECTED_NO_SENDER);

        if(partyService.settings().onlyLeaderCanInvite())
            if(!Objects.requireNonNull(party.get()).leader().equals(sender))
                throw new IllegalStateException(ProxyLang.PARTY_INJECTED_INVALID_LEADER_INVITE);
        else
            if(!Objects.requireNonNull(party.get()).players().contains(sender) && sender.resolve().isPresent())
                throw new IllegalStateException(ProxyLang.PARTY_INJECTED_INVALID_MEMBER_INVITE);

        Objects.requireNonNull(this.party.get()).join(this.target);
        Objects.requireNonNull(this.party.get()).server().connect(this.target);
        partyService.closeInvite(this);
        this.isAcknowledged = true;
    }

    /**
     * Deny the party invite.
     * This will subsequently decompose the invite and remove it from the PartyService that it belongs to.
     */
    public synchronized void ignore() {
        if(this.isAcknowledged != null) throw new IllegalStateException(ProxyLang.PARTY_INJECTED_ACKNOWLEDGED);

        partyService.closeInvite(this);
        this.isAcknowledged = true;
    }

    public synchronized void decompose() {
        this.party.clear();
        this.target = null;
        this.sender = null;
    }
}