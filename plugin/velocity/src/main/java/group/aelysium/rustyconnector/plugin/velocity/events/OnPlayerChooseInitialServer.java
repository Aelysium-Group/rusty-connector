package group.aelysium.rustyconnector.plugin.velocity.events;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import group.aelysium.rustyconnector.toolkit.core.logger.PluginLogger;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.RootFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendRequest;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;

public class OnPlayerChooseInitialServer {
    /**
     * Runs when a player first joins the proxy
     */
    @Subscribe(order = PostOrder.LAST)
    public EventTask onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();
        Player player = Player.from(event.getPlayer());
        com.velocitypowered.api.proxy.Player eventPlayer = event.getPlayer();

        return EventTask.async(() -> {
            try {
                try {
                    Whitelist whitelist = api.services().whitelist().proxyWhitelist();
                    if (!whitelist.validate(eventPlayer)) {
                        logger.log("Player isn't whitelisted on the proxy whitelist! Kicking...");
                        eventPlayer.disconnect(Component.text(whitelist.message()));
                        return;
                    }
                } catch (Exception ignore) {}

                RootFamily rootFamily = api.services().family().rootFamily();

                MCLoader server = rootFamily.connect(event);

                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN, DiscordWebhookMessage.PROXY__PLAYER_JOIN.build(player, server));
                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN_FAMILY, DiscordWebhookMessage.PROXY__PLAYER_JOIN_FAMILY.build(player, server));
                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN, server.family().id(), DiscordWebhookMessage.FAMILY__PLAYER_JOIN.build(player, server));
            } catch (Exception e) {
                eventPlayer.disconnect(Component.text("Disconnected. "+e.getMessage()));
                e.printStackTrace();
            }

            // Check for active friend requests
            try {
                FriendsService friendsService = api.services().friends().orElseThrow();
                List<FriendRequest> requests = friendsService.findRequestsToTarget(player);

                if(requests.size() == 0) throw new NoOutputException();

                eventPlayer.sendMessage(VelocityLang.FRIENDS_JOIN_MESSAGE.build(requests));
            } catch (Exception ignore) {}

            // Check for online friends
            try {
                FriendsService friendsService = api.services().friends().orElseThrow();
                List<Player> friends = friendsService.findFriends(player).orElseThrow();

                if(friends.size() == 0) throw new NoOutputException();

                List<com.velocitypowered.api.proxy.Player> onlineFriends = new ArrayList<>();
                friends.forEach(friend -> {
                    try {
                        com.velocitypowered.api.proxy.Player onlineFriend = friend.resolve().orElseThrow();

                        if (onlineFriend.isActive()) onlineFriends.add(onlineFriend);
                    } catch (Exception ignore) {}
                });

                if(friends.size() == 0 || onlineFriends.size() == 0) {
                    eventPlayer.sendMessage(VelocityLang.NO_ONLINE_FRIENDS);
                    throw new NoOutputException();
                }

                eventPlayer.sendMessage(VelocityLang.ONLINE_FRIENDS);
                final Component[] friendsList = {Component.text("", NamedTextColor.WHITE)};
                onlineFriends.forEach(friend -> friendsList[0] = friendsList[0].append(Component.text(friend.getUsername())));
                eventPlayer.sendMessage(Component.join(JoinConfiguration.commas(true), friendsList));

                onlineFriends.forEach(friend -> friend.sendMessage(VelocityLang.FRIEND_JOIN.build(player)));
            } catch (Exception ignore) {}
        });
    }
}
