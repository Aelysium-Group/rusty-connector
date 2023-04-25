package group.aelysium.rustyconnector.plugin.velocity.lib.events;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.VirtualProxyProcessor;
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
                VelocityAPI api = VelocityRustyConnector.getAPI();
                PluginLogger logger = api.getLogger();
                VirtualProxyProcessor virtualProcessor = api.getVirtualProcessor();

                try {
                    Player player = event.getPlayer();

                    RegisteredServer newRawServer = event.getServer();
                    RegisteredServer oldRawServer = event.getPreviousServer().orElse(null);

                    PlayerServer newServer = virtualProcessor.findServer(newRawServer.getServerInfo());

                    if(oldRawServer == null) return; // Player just connected to proxy. This isn't a server switch.
                    PlayerServer oldServer = virtualProcessor.findServer(oldRawServer.getServerInfo());

                    boolean isTheSameFamily = newServer.getFamilyName().equals(oldServer.getFamilyName());


                    newServer.playerJoined();

                    oldServer.playerLeft();

                    // These are all family alerts, if the player doesn't move between families at all, these don't need to fire.
                    if(!isTheSameFamily) {
                        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE_FAMILY, DiscordWebhookMessage.PROXY__PLAYER_LEAVE_FAMILY.build(player, oldServer));
                        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE, oldServer.getFamilyName(), DiscordWebhookMessage.FAMILY__PLAYER_LEAVE.build(player, oldServer));

                        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN_FAMILY, DiscordWebhookMessage.PROXY__PLAYER_JOIN_FAMILY.build(player, newServer));
                        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN, newServer.getFamilyName(), DiscordWebhookMessage.FAMILY__PLAYER_JOIN.build(player, newServer));

                        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_SWITCH_FAMILY, DiscordWebhookMessage.PROXY__PLAYER_SWITCH_FAMILY.build(player, oldServer, newServer));
                        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_SWITCH, newServer.getFamilyName(), DiscordWebhookMessage.FAMILY__PLAYER_SWITCH.build(player, oldServer, newServer));
                    }

                    WebhookEventManager.fire(WebhookAlertFlag.PLAYER_SWITCH_SERVER, DiscordWebhookMessage.PROXY__PLAYER_SWITCH_SERVER.build(player, oldServer, newServer));
                } catch (Exception e) {
                    logger.log(e.getMessage());
                }
            });
    }
}