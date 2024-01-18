package group.aelysium.rustyconnector.plugin.velocity.event_handlers.rc;

import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import group.aelysium.rustyconnector.toolkit.core.events.Listener;
import group.aelysium.rustyconnector.toolkit.core.events.Priority;
import group.aelysium.rustyconnector.toolkit.velocity.events.player.FamilyPostJoinEvent;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;

public class OnFamilyPostJoin extends Listener<FamilyPostJoinEvent> {
    @Override
    @Handler(priority = 0, delivery = Invoke.Asynchronously) // Changes priority to {@link Priority.NATIVE}
    public void handler(FamilyPostJoinEvent event) {
        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN, DiscordWebhookMessage.PROXY__PLAYER_JOIN.build(event.player(), event.mcLoader()));
        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN_FAMILY, DiscordWebhookMessage.PROXY__PLAYER_JOIN_FAMILY.build(event.player(), event.mcLoader()));
        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN, event.family().id(), DiscordWebhookMessage.FAMILY__PLAYER_JOIN.build(event.player(), event.mcLoader()));
    }
}