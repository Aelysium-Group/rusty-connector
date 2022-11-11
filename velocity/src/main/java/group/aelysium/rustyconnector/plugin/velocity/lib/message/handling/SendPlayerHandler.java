package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.message.MessageHandler;
import group.aelysium.rustyconnector.core.lib.message.RedisMessage;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.ServerFamily;
import net.kyori.adventure.text.Component;

import java.net.MalformedURLException;
import java.security.InvalidAlgorithmParameterException;
import java.util.UUID;

public class SendPlayerHandler implements MessageHandler {
    private final RedisMessage message;

    public SendPlayerHandler(RedisMessage message) {
        this.message = message;
    }

    @Override
    public void execute() throws InvalidAlgorithmParameterException {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        String familyName = message.getParameter("family");
        UUID uuid = UUID.fromString(message.getParameter("uuid"));

        ServerFamily family = plugin.getProxy().getFamilyManager().find(familyName);
        if (family == null) throw new InvalidAlgorithmParameterException("A family with the name `"+familyName+"` doesn't exist!");

        Player player = VelocityRustyConnector.getInstance().getVelocityServer().getPlayer(uuid).stream().findFirst().orElse(null);
        if(player == null) return;

        try {
            family.connect(player);
        } catch (MalformedURLException e) {
            player.disconnect(Component.text("Unable to connect you to the network! There are no default servers available!"));
            plugin.logger().log("There are no servers registered in the root family! Player's will be unable to join your network if there are no servers here!");
        }
    }
}
