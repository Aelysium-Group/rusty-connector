package group.aelysium.rustyconnector.plugin.velocity.lib.parties;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.commands.CommandParty;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PartyService extends Service {
    private final Vector<Party> parties = new Vector<>();
    private final Vector<PartyInvite> invites = new Vector<>();
    private final PartySettings settings;
    private final ExecutorService connector;

    public PartyService(PartySettings settings) {
        this.settings = settings;

        this.connector = Executors.newFixedThreadPool(10);
    }

    public void initCommand() {
        CommandManager commandManager = VelocityAPI.get().velocityServer().getCommandManager();
        VelocityAPI.get().logger().send(Component.text("Building party service commands...", NamedTextColor.DARK_GRAY));

        if(!commandManager.hasCommand("party"))
            try {
                commandManager.register(
                        commandManager.metaBuilder("party").build(),
                        CommandParty.create()
                );

                VelocityAPI.get().logger().send(Component.text(" | Registered: /party", NamedTextColor.YELLOW));
            } catch (Exception e) {
                e.printStackTrace();
            }

        VelocityAPI.get().logger().send(Component.text("Finished building party service commands.", NamedTextColor.GREEN));
    }

    public void queueConnector(Runnable runnable) {
        this.connector.submit(runnable);
    }

    public PartySettings settings() {
        return this.settings;
    }

    public Party create(Player host, PlayerServer server) {
        Party party = new Party(this.settings.maxMembers, host, server);
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
        VelocityAPI api = VelocityAPI.get();

        if(party.leader() != sender && this.settings.onlyLeaderCanInvite)
            throw new IllegalStateException("Hey! Only the party leader can invite other players!");

        if(this.settings.friendsOnly())
            try {
                FriendsService friendsService = api.services().friendsService().orElse(null);
                if(friendsService == null) {
                    api.logger().send(Component.text("You have parties set to only allow players to invite their friends! But the Friends module is disabled! Ignoring...", NamedTextColor.YELLOW));
                    throw new NoOutputException();
                }

                if(friendsService.findFriends(sender, false).orElseThrow().contains(target))
                    throw new IllegalStateException("You are only allowed to invite friends to join your party!");
            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception ignore) {}

        PartyInvite invite = new PartyInvite(party, sender, target);
        this.invites.add(invite);

        sender.sendMessage(Component.text("You invited " + target.getUsername() + " to your party!", NamedTextColor.GREEN));

        target.sendMessage(VelocityLang.PARTY_INVITE_RECEIVED.build(sender));
        return invite;
    }

    public List<PartyInvite> findInvitesToTarget(Player target) {
        return this.invites.stream().filter(invite -> invite.target() == target).findAny().stream().toList();
    }
    public Optional<PartyInvite> findInvite(Player target, Player sender) {
        return this.invites.stream().filter(invite -> invite.target().equals(target) && invite.sender().equals(sender)).findFirst();
    }

    public void closeInvite(PartyInvite invite) {
        this.invites.remove(invite);
        invite.decompose();
    }

    public List<Party> dump() {
        return this.parties.stream().toList();
    }

    @Override
    public void kill() {
        this.parties.clear();
        this.invites.clear();
        this.connector.shutdown();
    }

    public record PartySettings(
                                int maxMembers,
                                boolean friendsOnly,
                                boolean localOnly,
                                boolean onlyLeaderCanInvite,
                                boolean onlyLeaderCanKick,
                                boolean onlyLeaderCanSwitchServers,
                                boolean disbandOnLeaderQuit,
                                SwitchPower switchPower
                                ) {}
}