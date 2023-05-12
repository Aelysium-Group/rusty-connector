package group.aelysium.rustyconnector.plugin.velocity.lib.events;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;
import net.kyori.adventure.text.Component;

public class OnPlayerKicked {
    /**
     * Runs when a player disconnects from a player server
     */
    @Subscribe(order = PostOrder.FIRST)
    public EventTask onPlayerKicked(KickedFromServerEvent event) {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        Player player = event.getPlayer();

        return EventTask.async(() -> {
            try {
                if(player.getCurrentServer().isPresent()) {
                    PlayerServer oldServer = api.getVirtualProcessor().findServer(player.getCurrentServer().orElseThrow().getServerInfo());
                    if (oldServer == null) return;
                    oldServer.playerLeft();

                    boolean wasKickedFromRootFamily = api.getVirtualProcessor().getRootFamily().getName().equals(oldServer.getFamilyName());

                    WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE, oldServer.getFamilyName(), DiscordWebhookMessage.PROXY__PLAYER_LEAVE_FAMILY.build(player, oldServer));
                    WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE_FAMILY, oldServer.getFamilyName(), DiscordWebhookMessage.FAMILY__PLAYER_LEAVE.build(player, oldServer));

                    if(!wasKickedFromRootFamily) {
                        PlayerServer newServer = api.getVirtualProcessor().getRootFamily().connect(player);

                        WebhookEventManager.fire(WebhookAlertFlag.DISCONNECT_CATCH, api.getVirtualProcessor().getRootFamily().getName(), DiscordWebhookMessage.PROXY__DISCONNECT_CATCH.build(player, oldServer, newServer));
                        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN, api.getVirtualProcessor().getRootFamily().getName(), DiscordWebhookMessage.PROXY__PLAYER_JOIN_FAMILY.build(player, newServer));
                        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN_FAMILY, api.getVirtualProcessor().getRootFamily().getName(), DiscordWebhookMessage.FAMILY__PLAYER_JOIN.build(player, newServer));
                    }
                }

                if(event.getServerKickReason().isPresent())
                    player.disconnect(event.getServerKickReason().get());
                else
                    player.disconnect(Component.text("Kicked by server."));

                api.getVirtualProcessor().uncacheHomeServerMappings(player);

                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE, DiscordWebhookMessage.PROXY__PLAYER_LEAVE.build(player));
            } catch (Exception e) {
                player.disconnect(Component.text("Kicked by server. "+e.getMessage()));
                e.printStackTrace();
            }
        });
    }
}