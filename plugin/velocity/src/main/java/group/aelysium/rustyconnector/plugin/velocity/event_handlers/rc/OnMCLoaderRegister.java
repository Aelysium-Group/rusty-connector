package group.aelysium.rustyconnector.plugin.velocity.event_handlers.rc;

import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import group.aelysium.rustyconnector.toolkit.core.events.Listener;
import group.aelysium.rustyconnector.toolkit.core.log_gate.GateKey;
import group.aelysium.rustyconnector.toolkit.velocity.events.mc_loader.RegisterEvent;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;

public class OnMCLoaderRegister extends Listener<RegisterEvent> {
    @Override
    @Handler(delivery = Invoke.Asynchronously) // Changes priority to {@link Priority.NATIVE}
    public void handler(RegisterEvent event) {
        PluginLogger logger = Tinder.get().logger();

        System.out.println("MCLoader register event has fired!");

        // Fire console message
        if(logger.loggerGate().check(GateKey.REGISTRATION_ATTEMPT))
            ProxyLang.REGISTERED.send(logger, event.mcLoader().uuidOrDisplayName(), event.family().id());

        // Fire discord webhook
        WebhookEventManager.fire(WebhookAlertFlag.SERVER_REGISTER, DiscordWebhookMessage.PROXY__SERVER_REGISTER.build(event.mcLoader(), event.family().id()));
        WebhookEventManager.fire(WebhookAlertFlag.SERVER_REGISTER, event.family().id(), DiscordWebhookMessage.FAMILY__SERVER_REGISTER.build(event.mcLoader(), event.family().id()));
    }
}