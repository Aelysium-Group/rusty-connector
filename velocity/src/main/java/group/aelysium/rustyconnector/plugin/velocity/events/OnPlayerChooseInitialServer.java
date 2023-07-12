package group.aelysium.rustyconnector.plugin.velocity.events;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.central.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ScalarServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;
import net.kyori.adventure.text.Component;

import static group.aelysium.rustyconnector.plugin.velocity.central.Processor.ValidServices.FAMILY_SERVICE;
import static group.aelysium.rustyconnector.plugin.velocity.central.Processor.ValidServices.WHITELIST_SERVICE;

public class OnPlayerChooseInitialServer {
    /**
     * Runs when a player first joins the proxy
     */
    @Subscribe(order = PostOrder.LAST)
    public EventTask onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();
        Player player = event.getPlayer();

        return EventTask.async(() -> {
            try {
                Whitelist whitelist = api.getService(WHITELIST_SERVICE).orElseThrow().getProxyWhitelist();
                if(whitelist != null) {
                    if (!whitelist.validate(player)) {
                        logger.log("Player isn't whitelisted on the proxy whitelist! Kicking...");
                        player.disconnect(Component.text(whitelist.getMessage()));
                        return;
                    }
                }

                ScalarServerFamily rootFamily = api.getService(FAMILY_SERVICE).orElseThrow().getRootFamily();

                PlayerServer server = rootFamily.connect(player);
                if(server == null) return;
                event.setInitialServer(server.getRegisteredServer());

                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN, DiscordWebhookMessage.PROXY__PLAYER_JOIN.build(player, server));
                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN_FAMILY, DiscordWebhookMessage.PROXY__PLAYER_JOIN_FAMILY.build(player, server));
                WebhookEventManager.fire(WebhookAlertFlag.PLAYER_JOIN, server.getFamilyName(), DiscordWebhookMessage.FAMILY__PLAYER_JOIN.build(player, server));
            } catch (Exception e) {
                player.disconnect(Component.text("Disconnected. "+e.getMessage()));
                e.printStackTrace();
            }
        });
    }
}
