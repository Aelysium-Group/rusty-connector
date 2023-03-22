package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.data_messaging.MessageHandler;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessage;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.PaperServerLoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.ServerFamily;
import net.kyori.adventure.text.Component;

import java.security.InvalidAlgorithmParameterException;
import java.util.UUID;

public class SendPlayerHandler implements MessageHandler {
    private final RedisMessage message;

    public SendPlayerHandler(RedisMessage message) {
        this.message = message;
    }

    @Override
    public void execute() throws Exception {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        String familyName = message.getParameter("family");
        UUID uuid = UUID.fromString(message.getParameter("uuid"));

        Player player = VelocityRustyConnector.getInstance().getVelocityServer().getPlayer(uuid).stream().findFirst().orElse(null);
        if(player == null) return;

        try {
            ServerFamily<? extends PaperServerLoadBalancer> family = plugin.getProxy().getFamilyManager().find(familyName);
            if (family == null) throw new InvalidAlgorithmParameterException("A family with the name `"+familyName+"` doesn't exist!");

            family.connect(player);
        } catch (Exception e) {
            player.disconnect(Component.text("There was an issue connecting you to that server!"));
            throw new Exception(e.getMessage());
        }
    }
}
