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
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;

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
                    Whitelist whitelist = api.services().whitelistService().getProxyWhitelist().orElseThrow();
                    if (!whitelist.validate(player)) {
                        logger.log("Player isn't whitelisted on the proxy whitelist! Kicking...");
                        player.disconnect(Component.text(whitelist.getMessage()));
                        return;
                    }
                } catch (Exception ignore) {}

                RootServerFamily rootFamily = api.services().familyService().getRootFamily();

                PlayerServer server = rootFamily.connect(player);
                if(server == null) return;
                event.setInitialServer(server.getRegisteredServer());

                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN, DiscordWebhookMessage.PROXY__PLAYER_JOIN.build(player, server));
                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN_FAMILY, DiscordWebhookMessage.PROXY__PLAYER_JOIN_FAMILY.build(player, server));
                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN, server.getFamily().getName(), DiscordWebhookMessage.FAMILY__PLAYER_JOIN.build(player, server));
            } catch (Exception e) {
                player.disconnect(Component.text("Disconnected. "+e.getMessage()));
                e.printStackTrace();
            }

            try {
                FriendsService friendsService = api.services().friendsService().orElseThrow();
                List<Player> friends = friendsService.findFriends(player, true).orElseThrow();

                if(friends.size() == 0) throw new NoOutputException();

                List<Player> onlineFriends = new ArrayList<>();
                friends.forEach(friend -> {
                    if(friend.isActive()) onlineFriends.add(player);
                });

                if(friends.size() != 0 && onlineFriends.size() == 0) {
                    player.sendMessage(Component.text("None of your friends are online right now.", NamedTextColor.GRAY));
                    throw new NoOutputException();
                }

                player.sendMessage(Component.text("You have friends online!", NamedTextColor.GRAY));
                final Component[] friendsList = {Component.text("", NamedTextColor.YELLOW)};
                onlineFriends.forEach(friend -> friendsList[0] = friendsList[0].append(Component.text(friend.getUsername() + " ")));

                friends.forEach(friend -> friend.sendMessage(VelocityLang.FRIEND_JOIN.build(player)));
            } catch (Exception ignore) {}
        });
    }
}
