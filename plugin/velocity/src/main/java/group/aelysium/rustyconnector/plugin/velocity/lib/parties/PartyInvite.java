package group.aelysium.rustyconnector.plugin.velocity.lib.parties;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.toolkit.velocity.parties.IPartyInvite;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.ResolvablePlayer;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;

public class PartyInvite implements IPartyInvite<ResolvablePlayer> {
    private final PartyService partyService;
    private final WeakReference<Party> party;
    private ResolvablePlayer sender;
    private ResolvablePlayer target;
    private Boolean isAcknowledged = null;

    public PartyInvite(PartyService partyService, Party party, Player sender, Player target) {
        this.partyService = partyService;
        this.party = new WeakReference<>(party);
        this.sender = ResolvablePlayer.from(sender);
        this.target = ResolvablePlayer.from(target);
    }

    public ResolvablePlayer sender() {
        return this.sender;
    }
    public ResolvablePlayer target() {
        return this.target;
    }


    /**
     * Accept the party invite.
     * This will subsequently connect the player to the party's server and then decompose the invite and remove it from the PartyService that it belongs to.
     */
    public synchronized void accept() {
        if(this.isAcknowledged != null)
            throw new IllegalStateException(VelocityLang.PARTY_INJECTED_ACKNOWLEDGED);
        try {
            if (this.party.get() == null || Objects.requireNonNull(this.party.get()).isEmpty())
                throw new IllegalStateException(VelocityLang.PARTY_INJECTED_EXPIRED_INVITE);
        } catch (NullPointerException ignore) {
            throw new IllegalStateException(VelocityLang.PARTY_INJECTED_EXPIRED_INVITE);
        }
        if(this.sender.resolve().isEmpty())
            throw new IllegalStateException(VelocityLang.PARTY_INJECTED_NO_SENDER);

        if(partyService.settings().onlyLeaderCanInvite())
            if(!Objects.requireNonNull(party.get()).leader().equals(sender.resolve().orElse(null)))
                throw new IllegalStateException(VelocityLang.PARTY_INJECTED_INVALID_LEADER_INVITE);
        else
            if(!Objects.requireNonNull(party.get()).players().contains(sender.resolve().orElse(null)) && sender.resolve().isPresent())
                throw new IllegalStateException(VelocityLang.PARTY_INJECTED_INVALID_MEMBER_INVITE);

        Optional<Player> player = this.target.resolve();
        if(player.isEmpty())
            throw new IllegalStateException(VelocityLang.PARTY_INJECTED_NO_TARGET);

        Objects.requireNonNull(this.party.get()).join(player.orElseThrow());
        partyService.closeInvite(this);
        this.isAcknowledged = true;
    }

    /**
     * Deny the party invite.
     * This will subsequently decompose the invite and remove it from the PartyService that it belongs to.
     */
    public synchronized void ignore() {
        if(this.isAcknowledged != null) throw new IllegalStateException(VelocityLang.PARTY_INJECTED_ACKNOWLEDGED);

        partyService.closeInvite(this);
        this.isAcknowledged = true;
    }

    public synchronized void decompose() {
        this.party.clear();
        this.target = null;
        this.sender = null;
    }
}