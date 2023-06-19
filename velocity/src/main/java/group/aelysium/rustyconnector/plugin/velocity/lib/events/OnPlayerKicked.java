package group.aelysium.rustyconnector.plugin.velocity.lib.events;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ScalarServerFamily;
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
            boolean isFromRootFamily = false;

            try {
                if (!player.getCurrentServer().isPresent()) throw new NoOutputException();

                PlayerServer oldServer = api.getVirtualProcessor().findServer(player.getCurrentServer().orElseThrow().getServerInfo());
                if (oldServer == null) throw new NoOutputException();

                oldServer.playerLeft();

                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE, oldServer.getFamilyName(), DiscordWebhookMessage.PROXY__PLAYER_LEAVE_FAMILY.build(player, oldServer));
                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE_FAMILY, oldServer.getFamilyName(), DiscordWebhookMessage.FAMILY__PLAYER_LEAVE.build(player, oldServer));

                isFromRootFamily = oldServer.getFamily() == api.getVirtualProcessor().getRootFamily();
            } catch (Exception ignore) {}

            try {
                if (!api.getVirtualProcessor().catchDisconnectingPlayers) throw new NoOutputException();

                ScalarServerFamily rootFamily = api.getVirtualProcessor().getRootFamily();
                if(rootFamily.getRegisteredServers().isEmpty()) throw new RuntimeException("There are no available servers for you to connect to!");
                if(isFromRootFamily) throw new NoOutputException();

                PlayerServer newServer = rootFamily.fetchAny(player);
                if(newServer == null) throw new RuntimeException("Server closed.");

                try {
                    event.setResult(KickedFromServerEvent.RedirectPlayer.create(newServer.getRegisteredServer(), event.getServerKickReason().get()));
                } catch (Exception ignore) {
                    event.setResult(KickedFromServerEvent.RedirectPlayer.create(newServer.getRegisteredServer()));
                }

                newServer.playerJoined();

                WebhookEventManager.fire(WebhookAlertFlag.DISCONNECT_CATCH, api.getVirtualProcessor().getRootFamily().getName(), DiscordWebhookMessage.PROXY__DISCONNECT_CATCH.build(player, newServer));
                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN, api.getVirtualProcessor().getRootFamily().getName(), DiscordWebhookMessage.PROXY__PLAYER_JOIN_FAMILY.build(player, newServer));
                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN_FAMILY, api.getVirtualProcessor().getRootFamily().getName(), DiscordWebhookMessage.FAMILY__PLAYER_JOIN.build(player, newServer));

                return;
            }
            catch(NoOutputException ignore) {}
            catch (Exception e) {
                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE, DiscordWebhookMessage.PROXY__PLAYER_LEAVE.build(player));
                event.setResult(KickedFromServerEvent.DisconnectPlayer.create(Component.text("Kicked by server. "+e.getMessage())));
                e.printStackTrace();
            }

            try {
                if(event.getServerKickReason().isPresent())
                    event.setResult(KickedFromServerEvent.DisconnectPlayer.create(event.getServerKickReason().get()));
                else
                    event.setResult(KickedFromServerEvent.DisconnectPlayer.create(Component.text("Kicked by server.")));

                api.getVirtualProcessor().uncacheHomeServerMappings(player);

                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE, DiscordWebhookMessage.PROXY__PLAYER_LEAVE.build(player));
            } catch (Exception e) {
                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE, DiscordWebhookMessage.PROXY__PLAYER_LEAVE.build(player));
                event.setResult(KickedFromServerEvent.DisconnectPlayer.create(Component.text("Kicked by server. "+e.getMessage())));
                e.printStackTrace();
            }
        });
    }
}