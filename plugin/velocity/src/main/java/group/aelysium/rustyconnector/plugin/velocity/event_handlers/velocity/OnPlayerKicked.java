package group.aelysium.rustyconnector.plugin.velocity.event_handlers.velocity;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.event_handlers.EventDispatch;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;
import group.aelysium.rustyconnector.toolkit.velocity.events.player.FamilyLeaveEvent;
import group.aelysium.rustyconnector.toolkit.velocity.events.player.MCLoaderLeaveEvent;
import group.aelysium.rustyconnector.toolkit.velocity.family.scalar_family.IRootFamily;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import net.kyori.adventure.text.Component;

public class OnPlayerKicked {
    /**
     * Runs when a player disconnects from a player server
     */
    @Subscribe(order = PostOrder.FIRST)
    public EventTask onPlayerKicked(KickedFromServerEvent event) {
        Tinder api = Tinder.get();
        Player player = new Player(event.getPlayer());

        return EventTask.async(() -> {
            boolean isFromRootFamily = false;

            try {
                IMCLoader oldServer = player.server().orElseThrow();

                EventDispatch.UnSafe.fireAndForget(new FamilyLeaveEvent(oldServer.family(), oldServer, player, true));
                EventDispatch.UnSafe.fireAndForget(new MCLoaderLeaveEvent(oldServer, player, true));

                isFromRootFamily = oldServer.family() == api.services().family().rootFamily();
            } catch (Exception ignore) {}

            try {
                if (!api.services().family().shouldCatchDisconnectingPlayers()) throw new NoOutputException();

                if(isFromRootFamily) throw new NoOutputException();

                IRootFamily family = api.services().family().rootFamily();

                IMCLoader server = family.smartFetch().orElseThrow();

                try {
                    event.setResult(KickedFromServerEvent.RedirectPlayer.create(server.registeredServer(), event.getServerKickReason().get()));
                } catch (Exception ignore) {
                    event.setResult(KickedFromServerEvent.RedirectPlayer.create(server.registeredServer()));
                }

                WebhookEventManager.fire(WebhookAlertFlag.DISCONNECT_CATCH, api.services().family().rootFamily().id(), DiscordWebhookMessage.PROXY__DISCONNECT_CATCH.build(player, server));
                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN, api.services().family().rootFamily().id(), DiscordWebhookMessage.PROXY__PLAYER_JOIN_FAMILY.build(player, server));
                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN_FAMILY, api.services().family().rootFamily().id(), DiscordWebhookMessage.FAMILY__PLAYER_JOIN.build(player, server));

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

                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE, DiscordWebhookMessage.PROXY__PLAYER_LEAVE.build(player));
            } catch (Exception e) {
                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE, DiscordWebhookMessage.PROXY__PLAYER_LEAVE.build(player));
                event.setResult(KickedFromServerEvent.DisconnectPlayer.create(Component.text("Kicked by server. "+e.getMessage())));
                e.printStackTrace();
            }
        });
    }
}