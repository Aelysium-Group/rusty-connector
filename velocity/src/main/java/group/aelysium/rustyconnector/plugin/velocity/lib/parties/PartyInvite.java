package group.aelysium.rustyconnector.plugin.velocity.lib.parties;

import com.velocitypowered.api.proxy.Player;

import java.lang.ref.WeakReference;
import java.util.Objects;

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
        if(this.isAccepted != null) throw new IllegalStateException("This invite has already been acknowledged! You should close it using `PartyService#closeInvite`");

        this.isAccepted = true;
        Objects.requireNonNull(this.party.get()).getServer().connect(this.player);
    }

    public void deny() {
        if(this.isAccepted != null) throw new IllegalStateException("This invite has already been acknowledged! You should close it using `PartyService#closeInvite`");

        this.isAccepted = false;
    }
}