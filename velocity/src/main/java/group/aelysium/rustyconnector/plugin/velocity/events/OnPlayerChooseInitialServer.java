package group.aelysium.rustyconnector.plugin.velocity.events;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.central.PluginLogger;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.RootServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendRequest;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.FakePlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.PlayerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class OnPlayerChooseInitialServer {
    /**
     * Runs when a player first joins the proxy
     */
    @Subscribe(order = PostOrder.LAST)
    public EventTask onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        VelocityAPI api = VelocityAPI.get();
        PluginLogger logger = api.logger();
        Player player = event.getPlayer();

        return EventTask.async(() -> {
            try {
                try {
                    Whitelist whitelist = api.services().whitelistService().proxyWhitelist().orElseThrow();
                    if (!whitelist.validate(player)) {
                        logger.log("Player isn't whitelisted on the proxy whitelist! Kicking...");
                        player.disconnect(Component.text(whitelist.message()));
                        return;
                    }
                } catch (Exception ignore) {}

                RootServerFamily rootFamily = api.services().familyService().rootFamily();

                PlayerServer server = rootFamily.connect(event);

                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN, DiscordWebhookMessage.PROXY__PLAYER_JOIN.build(player, server));
                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN_FAMILY, DiscordWebhookMessage.PROXY__PLAYER_JOIN_FAMILY.build(player, server));
                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN, server.family().name(), DiscordWebhookMessage.FAMILY__PLAYER_JOIN.build(player, server));
            } catch (Exception e) {
                player.disconnect(Component.text("Disconnected. "+e.getMessage()));
                e.printStackTrace();
            }

            // Save player if they haven't joined before
            try {
                PlayerService playerService = api.services().playerService().orElseThrow();
                playerService.savePlayer(player);
            } catch (Exception ignore) {}

            // Check for active friend requests
            try {
                FriendsService friendsService = api.services().friendsService().orElseThrow();
                List<FriendRequest> requests = friendsService.findRequestsToTarget(FakePlayer.from(player));

                if(requests.size() == 0) throw new NoOutputException();

                if(requests.size() > 10) {
                    player.sendMessage(Component.text("You have " + requests.size() + " pending friend requests!", NamedTextColor.GRAY));
                    player.sendMessage(Component.text("Address them using: ", NamedTextColor.GRAY)
                            .append(Component.text("/friends requests <username> accept/deny", NamedTextColor.BLUE, TextDecoration.UNDERLINED)
                                    .clickEvent(ClickEvent.suggestCommand("/friends requests "))));
                } else {
                    AtomicReference<String> from = new AtomicReference<>("");
                    requests.forEach(request -> {
                        try {
                            from.set(from + ", " + request.sender().username());
                        } catch (Exception ignore) {}
                    });

                    player.sendMessage(Component.text("You have " + requests.size() + " pending friend requests from: " + from + ".", NamedTextColor.GRAY));
                    player.sendMessage(Component.text("Address them using: ", NamedTextColor.GRAY)
                            .append(Component.text("/friends requests <username> accept/deny", NamedTextColor.BLUE, TextDecoration.UNDERLINED)
                                    .clickEvent(ClickEvent.suggestCommand("/friends requests "))));
                }
            } catch (Exception ignore) {}

            // Check for online friends
            try {
                FriendsService friendsService = api.services().friendsService().orElseThrow();
                List<FakePlayer> friends = friendsService.findFriends(player, true).orElseThrow();

                if(friends.size() == 0) throw new NoOutputException();

                List<Player> onlineFriends = new ArrayList<>();
                friends.forEach(friend -> {
                    try {
                        Player onlineFriend = friend.resolve().orElseThrow();

                        if (onlineFriend.isActive()) onlineFriends.add(onlineFriend);
                    } catch (Exception ignore) {}
                });

                if(friends.size() == 0 || onlineFriends.size() == 0) {
                    player.sendMessage(Component.text("None of your friends are online right now.", NamedTextColor.GRAY));
                    throw new NoOutputException();
                }

                player.sendMessage(Component.text("You have friends online!", NamedTextColor.GRAY));
                final Component[] friendsList = {Component.text("", NamedTextColor.WHITE)};
                onlineFriends.forEach(friend -> friendsList[0] = friendsList[0].append(Component.text(friend.getUsername())));
                player.sendMessage(Component.join(JoinConfiguration.commas(true), friendsList));

                onlineFriends.forEach(friend -> friend.sendMessage(VelocityLang.FRIEND_JOIN.build(player)));
            } catch (Exception ignore) {}
        });
    }
}
