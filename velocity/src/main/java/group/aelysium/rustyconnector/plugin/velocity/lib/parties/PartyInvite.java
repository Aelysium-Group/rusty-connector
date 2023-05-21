public class PartyInvite {
    private final WeakReference<Party> party;
    private final Player player;
    private Boolean isAccepted = null;

    public PartyInvite(Party party, Player player) {
        this.party = new WeakReference<>(party);
        this.player = player;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void accept() {
        if(this.isAccepted != null) throw new IllegalStateException("This invite has already been acknowledged! You should close it using `ParyService#closeInvite`");

        this.isAccepted = true;
        this.party.get().getServer().connect(this.player);
    }

    public void deny() {
        if(this.isAccepted != null) throw new IllegalStateException("This invite has already been acknowledged! You should close it using `ParyService#closeInvite`");

        this.isAccepted = false;
    }
}