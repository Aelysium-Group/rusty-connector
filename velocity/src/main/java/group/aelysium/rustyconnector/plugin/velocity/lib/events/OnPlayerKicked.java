package group.aelysium.rustyconnector.plugin.velocity.lib.events;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PlayerServer;
import net.kyori.adventure.text.Component;

public class OnPlayerKicked {
    /**
     * Runs when a player disconnects from a paper server
     */
    @Subscribe(order = PostOrder.FIRST)
    public EventTask onPlayerKicked(KickedFromServerEvent event) {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        Player player = event.getPlayer();

        return EventTask.async(() -> {
            try {
                if(player.getCurrentServer().isPresent()) {
                    PlayerServer server = api.getVirtualProcessor().findServer(player.getCurrentServer().orElseThrow().getServerInfo());
                    if (server == null) return;
                    server.playerLeft();
                }
                if(event.getServerKickReason().isPresent())
                    player.disconnect(event.getServerKickReason().get());
                else
                    player.disconnect(Component.text("Kicked by server."));
            } catch (Exception e) {
                player.disconnect(Component.text("Kicked by server. "+e.getMessage()));
                e.printStackTrace();
            }
        });
    }
}