package group.aelysium.rustyconnector.plugin.velocity.events;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.StaticServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;

public class OnPlayerChangeServer {
    /**
     * Also runs when a player first joins the proxy
     */
    @Subscribe(order = PostOrder.FIRST)
    public EventTask onPlayerChangeServer(ServerConnectedEvent event) {
            return EventTask.async(() -> {
                VelocityAPI api = VelocityAPI.get();
                PluginLogger logger = api.logger();

                try {
                    Player player = event.getPlayer();

                    RegisteredServer newRawServer = event.getServer();
                    RegisteredServer oldRawServer = event.getPreviousServer().orElse(null);

                    PlayerServer newServer = api.services().serverService().search(newRawServer.getServerInfo());

                    if(oldRawServer == null) return; // Player just connected to proxy. This isn't a server switch.
                    PlayerServer oldServer = api.services().serverService().search(oldRawServer.getServerInfo());

                    boolean isTheSameFamily = newServer.family().equals(oldServer.family());

                    oldServer.playerLeft();

                    // These are all family alerts, if the player doesn't move between families at all, these don't need to fire.
                    if(!isTheSameFamily) {
                        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE_FAMILY, DiscordWebhookMessage.PROXY__PLAYER_LEAVE_FAMILY.build(player, oldServer));
                        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE, oldServer.family().name(), DiscordWebhookMessage.FAMILY__PLAYER_LEAVE.build(player, oldServer));

                        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN_FAMILY, DiscordWebhookMessage.PROXY__PLAYER_JOIN_FAMILY.build(player, newServer));
                        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN, newServer.family().name(), DiscordWebhookMessage.FAMILY__PLAYER_JOIN.build(player, newServer));

                        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_SWITCH_FAMILY, DiscordWebhookMessage.PROXY__PLAYER_SWITCH_FAMILY.build(player, oldServer, newServer));
                        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_SWITCH, newServer.family().name(), DiscordWebhookMessage.FAMILY__PLAYER_SWITCH.build(player, oldServer, newServer));
                    }

                    WebhookEventManager.fire(WebhookAlertFlag.PLAYER_SWITCH_SERVER, DiscordWebhookMessage.PROXY__PLAYER_SWITCH_SERVER.build(player, oldServer, newServer));

                    if(!isTheSameFamily) handleHomeServerCache(oldServer.family(), player);
                } catch (Exception e) {
                    logger.log(e.getMessage());
                }
            });
    }

    public void handleHomeServerCache(BaseServerFamily family, Player player) {
        PluginLogger logger = VelocityAPI.get().logger();

        try {
            if (!(family instanceof StaticServerFamily)) return;
            ((StaticServerFamily) family).uncacheHomeServer(player);
        } catch (Exception e) {
            logger.log(e.getMessage());
        }
    }
}