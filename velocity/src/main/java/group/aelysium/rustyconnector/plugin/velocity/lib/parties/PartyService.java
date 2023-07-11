package group.aelysium.rustyconnector.plugin.velocity.lib.parties;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.model.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.Optional;
import java.util.Vector;

public class PartyService extends Service {
    private final Vector<Party> parties = new Vector<>();
    private final Vector<PartyInvite> invites = new Vector<>();
    private final PartySettings settings;

    public PartyService(PartySettings settings) {
        super(true);
        this.settings = settings;
    }

    public PartySettings getSettings() {
        return this.settings;
    }

    public Party create(Player host) {
        Party party = new Party(this.settings.maxMembers, host);
        this.parties.add(party);
        return party;
    }

    public void delete(Party party) {
        party.decompose();
        this.parties.remove(party);
    }

    /**
     * Find a party based on its member.
     * @return A party.
     */
    public Optional<Party> find(Player member) {
        return this.parties.stream().filter(party -> party.contains(member)).findFirst();
    }

    public void disband(Party party) {
        for (Player player : party.players()) {
            player.sendMessage(VelocityLang.PARTY_DISBANDED.build());
        }
        this.delete(party);
    }

    public PartyInvite invitePlayer(Party party, Player sender, Player target) {
        if(party.getLeader() != sender && this.settings.onlyLeaderCanInvite)
            sender.sendMessage(Component.text("Hey! Only the party leader can invite other players!", NamedTextColor.RED));

        PartyInvite invite = new PartyInvite(party, sender, target);
        this.invites.add(invite);
        return invite;
    }

    public List<PartyInvite> findInvitesToTarget(Player target) {
        return this.invites.stream().filter(invite -> invite.getTarget() == target).findAny().stream().toList();
    }
    public Optional<PartyInvite> findInvite(Player target, Player sender) {
        return this.invites.stream().filter(invite -> invite.getTarget().equals(target) && invite.getSender().equals(sender)).findFirst();
    }

    public void closeInvite(PartyInvite invite) {
        this.invites.remove(invite);
        invite.decompose();
    }

    @Override
    public void kill() {
        this.parties.clear();
        this.invites.clear();
    }

    public record PartySettings(
                                int maxMembers,
                                boolean friendsOnly,
                                boolean onlyLeaderCanInvite,
                                boolean onlyLeaderCanKick,
                                boolean onlyLeaderCanSwitchServers,
                                boolean disbandOnLeaderQuit,
                                SwitchPower switchPower
                                ) {}
}