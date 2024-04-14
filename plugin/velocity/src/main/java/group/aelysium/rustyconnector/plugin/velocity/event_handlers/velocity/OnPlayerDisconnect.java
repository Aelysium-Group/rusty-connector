package group.aelysium.rustyconnector.plugin.velocity.event_handlers.velocity;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.event_handlers.EventDispatch;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.RankedFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;
import group.aelysium.rustyconnector.toolkit.velocity.events.player.FamilyLeaveEvent;
import group.aelysium.rustyconnector.toolkit.velocity.events.player.MCLoaderLeaveEvent;
import group.aelysium.rustyconnector.toolkit.velocity.events.player.NetworkLeaveEvent;
import group.aelysium.rustyconnector.toolkit.velocity.parties.IParty;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OnPlayerDisconnect {
    /**
     * Runs when a player disconnects from the proxy
     * This event prevents Velocity from attempting to connect the player to a velocity.toml server upon disconnect.
     */
    @Subscribe(order = PostOrder.FIRST)
    public EventTask onPlayerDisconnect(DisconnectEvent event) {
        Tinder api = Tinder.get();
        IPlayer player = new Player(event.getPlayer());

        return EventTask.async(() -> {
            EventDispatch.UnSafe.fireAndForget(new NetworkLeaveEvent(player));

            Optional<IMCLoader> mcLoader = player.server();
            if(mcLoader.isPresent()) {
                EventDispatch.UnSafe.fireAndForget(new FamilyLeaveEvent(mcLoader.get().family(), mcLoader.get(), player, true));
                EventDispatch.UnSafe.fireAndForget(new MCLoaderLeaveEvent(mcLoader.get(), player, true));
            }
            WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE, DiscordWebhookMessage.PROXY__PLAYER_LEAVE.build(player));

            handleParty(api, player);
            messageFriends(api, player);
            handleMatchmakerSession(api, player);
        });
    }

    /**
     * Handles any Ranked Family game sessions that the player may be in.
     */
    private static void handleMatchmakerSession(Tinder api, IPlayer player) {
        // There are no ranked families, thus no sessions to potentially clean up.
        if(api.services().family().dump().stream().noneMatch(family -> family instanceof RankedFamily)) return;

        try {
            player.server().orElseThrow().leave(player);

            return;
        } catch (Exception ignore) {
            System.out.println("No MCLoader was found for the player! Attempting to hard search for player in matchmakers...");
        }
        try {
            List<RankedFamily> families = new ArrayList<>();
            api.services().family().dump().forEach(family -> {
                if(family instanceof RankedFamily) families.add((RankedFamily) family);
            });

            for (RankedFamily family : families)
                family.matchmaker().leave(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles any parties that the player might be in.
     */
    private static void handleParty(Tinder api, IPlayer player) {
        try {
            PartyService partyService = api.services().party().orElseThrow();
            IParty party = partyService.find(player).orElseThrow();

            boolean wasPartyLeader = party.leader().equals(player);

            if(wasPartyLeader)
                if(partyService.settings().disbandOnLeaderQuit())
                    partyService.disband(party);

            party.leave(player);
        } catch (Exception ignore) {}
    }

    /**
     * Messages the player's friends letting them know that this player has left.
     */
    private static void messageFriends(Tinder api, IPlayer player) {
        // Handle sending out friend messages when player leaves
        try {
            FriendsService friendsService = api.services().friends().orElseThrow();
            if(!friendsService.settings().allowMessaging()) return;

            List<IPlayer> friends = friendsService.friendStorage().get(player).orElseThrow();

            if(friends.size() == 0) return;

            friends.forEach(friend -> friend.sendMessage(ProxyLang.FRIEND_LEAVE.build(player)));
        } catch (Exception ignore) {}
    }
}