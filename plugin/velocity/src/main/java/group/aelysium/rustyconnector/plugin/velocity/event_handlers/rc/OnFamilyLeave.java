package group.aelysium.rustyconnector.plugin.velocity.event_handlers.rc;

import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import group.aelysium.rustyconnector.toolkit.core.events.Listener;
import group.aelysium.rustyconnector.toolkit.velocity.events.player.FamilyLeaveEvent;
import group.aelysium.rustyconnector.toolkit.velocity.events.player.MCLoaderLeaveEvent;
import net.engio.mbassy.listener.Handler;

public class OnFamilyLeave extends Listener<FamilyLeaveEvent> {
    @Override
    @Handler() // Changes priority to {@link Priority.NATIVE}
    public void handler(FamilyLeaveEvent event) {
        try {
            event.family().leave(event.player());
        } catch (Exception ignore) {}

        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE, event.family().id(), DiscordWebhookMessage.FAMILY__PLAYER_LEAVE.build(event.player(), event.mcLoader()));

        if(!event.disconnected()) return;

        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE_FAMILY, DiscordWebhookMessage.PROXY__PLAYER_LEAVE_FAMILY.build(event.player(), event.mcLoader()));
        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_LEAVE, event.family().id(), DiscordWebhookMessage.FAMILY__PLAYER_LEAVE.build(event.player(), event.mcLoader()));
    }
}