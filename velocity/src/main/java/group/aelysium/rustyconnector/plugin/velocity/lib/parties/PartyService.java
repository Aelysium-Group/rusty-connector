package group.aelysium.rustyconnector.plugin.velocity.lib.parties;

import com.velocitypowered.api.proxy.Player;

import java.util.List;
import java.util.Vector;

public class PartyService {
    private final Vector<Party> parties = new Vector<>();
    private final Vector<PartyInvite> invites = new Vector<>();
    private final int partySize;

    public PartyService(int partySize) {
        this.partySize = partySize;
    }

    public Party create(Player host) {
        Party party = new Party(this.partySize, host);
        this.parties.add(party);
        return party;
    }

    public void delete(Party party) {
        this.parties.remove(party);
    }

    /**
     * Find a party based on it's member.
     * @return A party, or `null` if the player doesn't exist in any.
     */
    public Party find(Player member) {
        return this.parties.stream().filter(party -> party.contains(member)).findFirst().orElse(null);
    }

    public PartyInvite invitePlayer(Party party, Player player) {
        PartyInvite invite = new PartyInvite(party, player);
        this.invites.add(invite);
        return invite;
    }

    public List<PartyInvite> findInvites(Player player) {
        return this.invites.stream().filter(invite -> invite.getPlayer() == player).findAny().stream().toList();
    }

    public void closeInvite(PartyInvite invite) {
        this.invites.remove(invite);
    }
}