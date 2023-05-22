package group.aelysium.rustyconnector.plugin.velocity.lib.events;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;

public class OnPlayerDisconnect {
    /**
     * Runs when a player disconnects from the proxy
     * This event prevents Velocity from attempting to connect the player to a velocity.toml server upon disconnect.
     */
    @Subscribe(order = PostOrder.LAST)
    public EventTask onPlayerDisconnect(DisconnectEvent event) {
        VelocityAPI api = VelocityRustyConnector.getAPI();

        return EventTask.async(() -> {
            Player player = event.getPlayer();

            try {
                if(player == null) return;

                if(player.getCurrentServer().isPresent()) {
                    PlayerServer server = api.getProcessor().findServer(player.getCurrentServer().get().getServerInfo());
                    server.playerLeft();
                    api.getProcessor().uncacheHomeServerMappings(player);

                    WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE, server.getFamilyName(), DiscordWebhookMessage.FAMILY__PLAYER_LEAVE.build(player, server));
                    WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE_FAMILY, DiscordWebhookMessage.PROXY__PLAYER_LEAVE_FAMILY.build(player, server));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE, DiscordWebhookMessage.PROXY__PLAYER_LEAVE.build(player));
        });
    }
}