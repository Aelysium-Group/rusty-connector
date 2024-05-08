package group.aelysium.rustyconnector.plugin.velocity.lib.parties;

import com.velocitypowered.api.command.CommandManager;
import group.aelysium.rustyconnector.toolkit.velocity.friends.PlayerPair;
import group.aelysium.rustyconnector.toolkit.velocity.parties.IParty;
import group.aelysium.rustyconnector.toolkit.velocity.parties.IPartyInvite;
import group.aelysium.rustyconnector.toolkit.velocity.parties.IPartyService;
import group.aelysium.rustyconnector.toolkit.velocity.parties.PartyServiceSettings;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.commands.CommandParty;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PartyService implements IPartyService {
    private final Vector<IParty> parties = new Vector<>();
    private final Map<PlayerPair, IPartyInvite> invites = new HashMap<>();
    private final PartyServiceSettings settings;
    private final ExecutorService connector;

    public PartyService(PartyServiceSettings settings) {
        this.settings = settings;

        this.connector = Executors.newFixedThreadPool(10);
    }

    public void initCommand(List<Component> bootOutput) {
        CommandManager commandManager = Tinder.get().velocityServer().getCommandManager();
        bootOutput.add(Component.text("Building party service commands...", NamedTextColor.DARK_GRAY));

        if(!commandManager.hasCommand("party"))
            try {
                commandManager.register(
                        commandManager.metaBuilder("party").build(),
                        CommandParty.create(this)
                );

                bootOutput.add(Component.text(" | Registered: /party", NamedTextColor.YELLOW));
            } catch (Exception e) {
                e.printStackTrace();
            }

        bootOutput.add(Component.text("Finished building party service commands.", NamedTextColor.GREEN));
    }

    public void queueConnector(Runnable runnable) {
        this.connector.submit(runnable);
    }

    public PartyServiceSettings settings() {
        return this.settings;
    }

    public Party create(IPlayer host, IMCLoader server) {
        Party party = new Party(this.settings.maxMembers(), host, server);
        this.parties.add(party);
        return party;
    }

    public void delete(IParty party) {
        party.decompose();
        this.parties.remove(party);
    }

    public Optional<IParty> find(IPlayer member) {
        return this.parties.stream().filter(party -> party.contains(member)).findFirst();
    }

    public void disband(IParty party) {
        for (IPlayer player : party.players()) {
            player.sendMessage(ProxyLang.PARTY_DISBANDED);
        }
        this.delete(party);
    }

    public IPartyInvite invitePlayer(IParty party, IPlayer sender, IPlayer target) {
        Tinder api = Tinder.get();

        if(!party.leader().equals(sender) && this.settings.onlyLeaderCanInvite())
            throw new IllegalStateException(ProxyLang.PARTY_INJECTED_ONLY_LEADER_CAN_INVITE);

        if(this.settings.friendsOnly())
            try {
                FriendsService friendsService = api.services().friends().orElseThrow(
                        () -> {
                            api.logger().send(Component.text(ProxyLang.PARTY_INJECTED_FRIENDS_RESTRICTION_CONFLICT, NamedTextColor.YELLOW));
                            return new NoOutputException();
                        }
                );

                Optional<Boolean> contains = friendsService.friendStorage().contains(sender, target);
                if(contains.isEmpty())
                    throw new IllegalStateException("There was an internal error while trying to do that.");
                if(contains.get())
                    throw new IllegalStateException(ProxyLang.PARTY_INJECTED_FRIENDS_RESTRICTION);
            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception ignore) {}

        PartyInvite invite = new PartyInvite(this, party, sender, target);
        this.invites.put(PlayerPair.from(invite.target(), invite.sender()), invite);

        sender.sendMessage(ProxyLang.PARTY_INVITE_SENT.build(target.username()));

        target.sendMessage(ProxyLang.PARTY_INVITE_RECEIVED.build(sender.username()));
        return invite;
    }

    public List<IPartyInvite> findInvitesToTarget(IPlayer target) {
        return this.invites.values().stream().filter(invite -> invite.target().equals(target)).findAny().stream().toList();
    }
    public Optional<IPartyInvite> findInvite(IPlayer target, IPlayer sender) {
        IPartyInvite invite = this.invites.get(PlayerPair.from(target, sender));
        if(invite == null) return Optional.empty();
        return Optional.of(invite);
    }

    public void closeInvite(IPartyInvite invite) {
        this.invites.remove(invite);
        invite.decompose();
    }

    public List<IParty> dump() {
        return this.parties.stream().toList();
    }

    @Override
    public void kill() {
        this.parties.clear();
        this.invites.clear();
        this.connector.shutdown();

        CommandManager commandManager = Tinder.get().velocityServer().getCommandManager();
        commandManager.unregister("party");
    }
}