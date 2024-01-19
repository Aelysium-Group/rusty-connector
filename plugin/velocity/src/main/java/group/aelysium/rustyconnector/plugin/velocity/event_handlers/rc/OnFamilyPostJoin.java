package group.aelysium.rustyconnector.plugin.velocity.event_handlers.rc;

import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import group.aelysium.rustyconnector.toolkit.core.events.Listener;
import group.aelysium.rustyconnector.toolkit.velocity.events.player.FamilyPostJoinEvent;

public class OnFamilyPostJoin implements Listener<FamilyPostJoinEvent> {
    public void handler(FamilyPostJoinEvent event) {
        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN, DiscordWebhookMessage.PROXY__PLAYER_JOIN.build(event.player(), event.mcLoader()));
        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN_FAMILY, DiscordWebhookMessage.PROXY__PLAYER_JOIN_FAMILY.build(event.player(), event.mcLoader()));
        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN, event.family().id(), DiscordWebhookMessage.FAMILY__PLAYER_JOIN.build(event.player(), event.mcLoader()));
    }
}