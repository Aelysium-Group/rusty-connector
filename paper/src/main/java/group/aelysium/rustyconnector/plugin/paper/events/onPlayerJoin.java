package group.aelysium.rustyconnector.plugin.paper.events;

import group.aelysium.rustyconnector.core.lib.generic.firewall.Whitelist;
import group.aelysium.rustyconnector.core.lib.generic.firewall.WhitelistPlayer;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.lib.generic.PaperServer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class onPlayerJoin implements Listener {
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PaperServer server = PaperRustyConnector.getInstance().getVirtualServer();

        if(!server.validatePlayer(player)) player.kick(Component.text("This server is full!"));

        if(server.hasWhitelist()) {
            Whitelist whitelist = server.getWhitelist();
            WhitelistPlayer whitelistPlayer = whitelist.findPlayer(player.getName());
            if(whitelistPlayer == null) {
                player.kick(Component.text("You aren't whitelisted on this server!"));
                return;
            }

            if(!whitelist.validate(whitelistPlayer)) {
                player.kick(Component.text("You aren't whitelisted on this server!"));
                return;
            }
        }

        server.setPlayerCount(PaperRustyConnector.getInstance().getServer().getOnlinePlayers().size());
    }
}
