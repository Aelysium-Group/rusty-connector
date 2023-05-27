package group.aelysium.rustyconnector.plugin.velocity.lib.events;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
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
                    PlayerServer oldServer = api.getService(ServerService.class).findServer(player.getCurrentServer().orElseThrow().getServerInfo());
                    if (oldServer != null) {
                        oldServer.playerLeft();

                        boolean wasKickedFromRootFamily = api.getService(FamilyService.class).getRootFamily().getName().equals(oldServer.getFamilyName());

                        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE, oldServer.getFamilyName(), DiscordWebhookMessage.PROXY__PLAYER_LEAVE_FAMILY.build(player, oldServer));
                        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE_FAMILY, oldServer.getFamilyName(), DiscordWebhookMessage.FAMILY__PLAYER_LEAVE.build(player, oldServer));

                        if (!wasKickedFromRootFamily && api.getVirtualProcessor().catchDisconnectingPlayers) {
                            PlayerServer newServer = api.getService(FamilyService.class).getRootFamily().fetchAny(player);

                            try {
                                event.setResult(KickedFromServerEvent.RedirectPlayer.create(newServer.getRegisteredServer(), event.getServerKickReason().get()));
                            } catch (Exception ignore) {
                                event.setResult(KickedFromServerEvent.RedirectPlayer.create(newServer.getRegisteredServer()));
                            }

                            newServer.playerJoined();

                            WebhookEventManager.fire(WebhookAlertFlag.DISCONNECT_CATCH, api.getService(FamilyService.class).getRootFamily().getName(), DiscordWebhookMessage.PROXY__DISCONNECT_CATCH.build(player, oldServer, newServer));
                            WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN, api.getService(FamilyService.class).getRootFamily().getName(), DiscordWebhookMessage.PROXY__PLAYER_JOIN_FAMILY.build(player, newServer));
                            WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN_FAMILY, api.getService(FamilyService.class).getRootFamily().getName(), DiscordWebhookMessage.FAMILY__PLAYER_JOIN.build(player, newServer));

                            return;
                        }
                    }
                }

                if(event.getServerKickReason().isPresent())
                    event.setResult(KickedFromServerEvent.DisconnectPlayer.create(event.getServerKickReason().get()));
                else
                    event.setResult(KickedFromServerEvent.DisconnectPlayer.create(Component.text("Kicked by server.")));

                api.getService(FamilyService.class).uncacheHomeServerMappings(player);

                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE, DiscordWebhookMessage.PROXY__PLAYER_LEAVE.build(player));
            } catch (Exception e) {
                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE, DiscordWebhookMessage.PROXY__PLAYER_LEAVE.build(player));
                event.setResult(KickedFromServerEvent.DisconnectPlayer.create(Component.text("Kicked by server. "+e.getMessage())));
                e.printStackTrace();
            }
        });
    }
}