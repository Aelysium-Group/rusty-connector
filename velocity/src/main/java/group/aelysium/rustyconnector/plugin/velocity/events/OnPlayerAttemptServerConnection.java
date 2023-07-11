package group.aelysium.rustyconnector.plugin.velocity.events;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.StaticServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.Party;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhookMessage;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class OnPlayerAttemptServerConnection {
    /**
     * Also runs when a player first joins the proxy
     */
    @Subscribe(order = PostOrder.FIRST)
    public EventTask onPlayerAttemptServerConnection(ServerPreConnectEvent event) {
            return EventTask.async(() -> {
                VelocityAPI api = VelocityRustyConnector.getAPI();
                Player player = event.getPlayer();

                try {
                    PartyService partyService = api.getService(PartyService.class).orElse(null);
                    if(partyService == null) return;

                    Party party = partyService.find(player).orElse(null);
                    if(party == null) return;

                    if(partyService.getSettings().onlyLeaderCanSwitchServers())
                        if(!party.getLeader().equals(player) && !party.getServer().getRegisteredServer().equals(event.getOriginalServer())) {
                            player.sendMessage(Component.text("Only the party leader can connect to other servers!", NamedTextColor.RED));
                            event.setResult(ServerPreConnectEvent.ServerResult.denied());
                        }
                } catch (Exception ignore) {}
            });
    }
}