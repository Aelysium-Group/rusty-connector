package group.aelysium.rustyconnector.plugin.velocity.events;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.Party;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.FakePlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.PlayerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;

import java.util.List;
import java.util.Optional;

public class OnPlayerDisconnect {
    /**
     * Runs when a player disconnects from the proxy
     * This event prevents Velocity from attempting to connect the player to a velocity.toml server upon disconnect.
     */
    @Subscribe(order = PostOrder.LAST)
    public EventTask onPlayerDisconnect(DisconnectEvent event) {
        VelocityAPI api = VelocityAPI.get();

        return EventTask.async(() -> {
            Player player = event.getPlayer();
            if(player == null) return;

            // Handle servers when player leaves
            try {
                if(player.getCurrentServer().isPresent()) {
                    PlayerServer server = api.services().serverService().search(player.getCurrentServer().get().getServerInfo());
                    server.playerLeft();
                    api.services().familyService().uncacheHomeServerMappings(player);

                    WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE, server.family().name(), DiscordWebhookMessage.FAMILY__PLAYER_LEAVE.build(player, server));
                    WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE_FAMILY, DiscordWebhookMessage.PROXY__PLAYER_LEAVE_FAMILY.build(player, server));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Handle party when player leaves
            try {
                PartyService partyService = api.services().partyService().orElseThrow();
                Party party = partyService.find(player).orElseThrow();
                try {
                    boolean wasPartyLeader = party.leader().equals(player);

                    if(wasPartyLeader)
                        if(partyService.settings().disbandOnLeaderQuit())
                            partyService.disband(party);

                    party.leave(player);
                } catch (Exception e) {}
            } catch (Exception ignore) {}

            // Handle uncaching friends when player leaves
            try {
                FriendsService friendsService = api.services().friendsService().orElseThrow();
                friendsService.services().dataEnclave().uncachePlayer(FakePlayer.from(player));
            } catch (Exception ignore) {}

            // Handle sending out friend messages when player leaves
            try {
                FriendsService friendsService = api.services().friendsService().orElseThrow();
                if(!friendsService.settings().allowMessaging()) throw new NoOutputException();

                List<FakePlayer> friends = friendsService.findFriends(player, true).orElseThrow();

                if(friends.size() == 0) throw new NoOutputException();

                friends.forEach(friend -> {
                    Optional<Player> resolvedPlayer = friend.resolve();
                    if(!resolvedPlayer.isPresent()) return;

                    resolvedPlayer.get().sendMessage(VelocityLang.FRIEND_LEAVE.build(player));
                });
            } catch (Exception ignore) {}

            // Handle caching player when they leave
            try {
                PlayerService playerService = api.services().playerService().orElseThrow();
                playerService.cachePlayer(player);
            } catch (Exception ignore) {}

            WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE, DiscordWebhookMessage.PROXY__PLAYER_LEAVE.build(player));
        });
    }
}