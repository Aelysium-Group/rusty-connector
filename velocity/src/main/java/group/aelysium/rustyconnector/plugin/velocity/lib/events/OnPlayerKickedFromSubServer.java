package group.aelysium.rustyconnector.plugin.velocity.lib.events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PaperServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.ServerFamily;
import net.kyori.adventure.text.Component;

import java.net.MalformedURLException;

public class OnPlayerKickedFromSubServer {
    @Subscribe
    public void onPlayerJoin(KickedFromServerEvent event) {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        Player player = event.getPlayer();
        ServerConnection currentServer = player.getCurrentServer().orElse(null);

        try {
            if(currentServer == null) throw new NullPointerException();
            RegisteredServer registeredServer = currentServer.getServer();
            PaperServer server = plugin.getProxy().findServer(registeredServer.getServerInfo());

            ServerFamily family = server.getFamily();

            // If the player got kicked out of the root family. Disconnect them
            if(plugin.getProxy().getRootFamily() == family) {
                player.disconnect(Component.text("Disconnected..."));
                return;
            }

            // Otherwise; move them to the root family.
            family.connect(player);
        } catch (NullPointerException e) {
            plugin.logger().error("Unable to get the server that the player was kicked from! Booting them from the network...");
            player.disconnect(Component.text("Invalid server transfer packet!"));
        } catch (MalformedURLException e) {
            plugin.logger().error("Unable to connect the player to the root family! Booting them from the network...");
            player.disconnect(Component.text("Invalid server transfer packet!"));
        }
    }
}