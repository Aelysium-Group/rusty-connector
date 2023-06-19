public class PartyService {
    private final Vector<Party> parties = new Vector<>();
    private final Vector<PartyInvite> invites = new Vector<>();
    private final int partySize;

    public ParyService(int partySize) {
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
        PartyInvite invite = new ParyInvite(party, player);
        this.invites.add(invite);
        return invite;
    }

    public List<PartyInvite> findInvites(Player player) {
        return this.invites.stream().filter(invite -> invite.getPlayer() == player).findAll().toList();
    }

    public void closeInvite(ParyInvite invite) {
        this.invites.remove(invite);
    }
}