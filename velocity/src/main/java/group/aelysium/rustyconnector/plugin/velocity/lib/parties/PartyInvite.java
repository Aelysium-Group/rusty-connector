package group.aelysium.rustyconnector.plugin.velocity.lib.parties;

import com.velocitypowered.api.proxy.Player;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class PartyInvite {
    private final WeakReference<Player> sender;
    private final Player target;
    private Boolean isAccepted = null;

    public PartyInvite(Player sender, Player target) {
        this.sender = new WeakReference<>(sender);
        this.target = target;
    }

    public Player getSender() {
        return this.sender.get();
    }
    public Player getTarget() {
        return this.target;
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