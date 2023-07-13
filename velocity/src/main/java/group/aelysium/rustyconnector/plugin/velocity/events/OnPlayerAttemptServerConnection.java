package group.aelysium.rustyconnector.plugin.velocity.events;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.Party;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import static group.aelysium.rustyconnector.plugin.velocity.central.Processor.ValidServices.PARTY_SERVICE;

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
                    PartyService partyService = api.getService(PARTY_SERVICE).orElseThrow();

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