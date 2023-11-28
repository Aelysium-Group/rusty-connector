package group.aelysium.rustyconnector.plugin.velocity.events;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.Party;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
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
        Tinder api = Tinder.get();
        Player player = Player.from(event.getPlayer());

        return EventTask.async(() -> {
            // Handle servers when player leaves
            try {
                com.velocitypowered.api.proxy.Player resolvedPlayer = player.resolve().get();
                if(resolvedPlayer.getCurrentServer().isPresent()) {
                    MCLoader server = new MCLoader.Reference(resolvedPlayer.getCurrentServer().orElseThrow().getServerInfo()).get();
                    server.playerLeft();

                    WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE, server.family().id(), DiscordWebhookMessage.FAMILY__PLAYER_LEAVE.build(player, server));
                    WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE_FAMILY, DiscordWebhookMessage.PROXY__PLAYER_LEAVE_FAMILY.build(player, server));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Handle party when player leaves
            try {
                com.velocitypowered.api.proxy.Player resolvedPlayer = player.resolve().get();

                PartyService partyService = api.services().party().orElseThrow();
                Party party = partyService.find(resolvedPlayer).orElseThrow();
                try {
                    boolean wasPartyLeader = party.leader().equals(player);

                    if(wasPartyLeader)
                        if(partyService.settings().disbandOnLeaderQuit())
                            partyService.disband(party);

                    party.leave(resolvedPlayer);
                } catch (Exception e) {}
            } catch (Exception ignore) {}

            // Handle sending out friend messages when player leaves
            try {
                FriendsService friendsService = api.services().friends().orElseThrow();
                if(!friendsService.settings().allowMessaging()) throw new NoOutputException();

                List<Player> friends = friendsService.findFriends(player).orElseThrow();

                if(friends.size() == 0) throw new NoOutputException();

                friends.forEach(friend -> {
                    Optional<com.velocitypowered.api.proxy.Player> resolvedPlayer = friend.resolve();
                    if(!resolvedPlayer.isPresent()) return;

                    resolvedPlayer.get().sendMessage(VelocityLang.FRIEND_LEAVE.build(player));
                });
            } catch (Exception ignore) {}

            WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE, DiscordWebhookMessage.PROXY__PLAYER_LEAVE.build(player));
        });
    }
}