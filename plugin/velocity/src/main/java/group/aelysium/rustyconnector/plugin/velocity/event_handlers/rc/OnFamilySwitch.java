package group.aelysium.rustyconnector.plugin.velocity.event_handlers.rc;

import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import group.aelysium.rustyconnector.toolkit.core.events.Listener;
import group.aelysium.rustyconnector.toolkit.velocity.events.player.FamilyPostJoinEvent;
import group.aelysium.rustyconnector.toolkit.velocity.events.player.FamilySwitchEvent;
import net.engio.mbassy.listener.Handler;

public class OnFamilySwitch extends Listener<FamilySwitchEvent> {
    @Override
    @Handler() // Changes priority to {@link Priority.NATIVE}
    public void handler(FamilySwitchEvent event) {
        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_SWITCH_FAMILY, DiscordWebhookMessage.PROXY__PLAYER_SWITCH_FAMILY.build(event.player(), event.oldMCLoader(), event.newMCLoader()));
        WebhookEventManager.fire(WebhookAlertFlag.PLAYER_SWITCH, event.newFamily().id(), DiscordWebhookMessage.FAMILY__PLAYER_SWITCH.build(event.player(), event.oldMCLoader(), event.newMCLoader()));
    }
}