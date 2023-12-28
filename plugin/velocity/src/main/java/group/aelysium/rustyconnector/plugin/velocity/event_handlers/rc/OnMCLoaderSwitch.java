package group.aelysium.rustyconnector.plugin.velocity.event_handlers.rc;

import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import group.aelysium.rustyconnector.toolkit.core.events.Listener;
import group.aelysium.rustyconnector.toolkit.velocity.events.player.FamilySwitchEvent;
import group.aelysium.rustyconnector.toolkit.velocity.events.player.MCLoaderSwitchEvent;
import net.engio.mbassy.listener.Handler;

public class OnMCLoaderSwitch extends Listener<MCLoaderSwitchEvent> {
    @Override
    @Handler() // Changes priority to {@link Priority.NATIVE}
    public void handler(MCLoaderSwitchEvent event) {
        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_SWITCH_SERVER, DiscordWebhookMessage.PROXY__PLAYER_SWITCH_SERVER.build(event.player(), event.oldMCLoader(), event.newMCLoader()));
    }
}