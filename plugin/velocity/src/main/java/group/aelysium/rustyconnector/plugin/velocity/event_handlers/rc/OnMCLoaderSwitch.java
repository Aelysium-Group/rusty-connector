package group.aelysium.rustyconnector.plugin.velocity.event_handlers.rc;

import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import group.aelysium.rustyconnector.toolkit.core.events.Listener;
import group.aelysium.rustyconnector.toolkit.velocity.events.player.MCLoaderSwitchEvent;

public class OnMCLoaderSwitch implements Listener<MCLoaderSwitchEvent> {
    public void handler(MCLoaderSwitchEvent event) {
        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_SWITCH_SERVER, DiscordWebhookMessage.PROXY__PLAYER_SWITCH_SERVER.build(event.player(), event.oldMCLoader(), event.newMCLoader()));
    }
}