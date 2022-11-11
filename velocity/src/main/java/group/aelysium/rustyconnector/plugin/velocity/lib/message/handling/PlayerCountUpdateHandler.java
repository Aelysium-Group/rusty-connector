package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.message.MessageHandler;
import group.aelysium.rustyconnector.core.lib.message.RedisMessage;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PaperServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.ServerFamily;

import java.net.InetSocketAddress;
import java.security.InvalidAlgorithmParameterException;

public class PlayerCountUpdateHandler implements MessageHandler {
    private final RedisMessage message;

    public PlayerCountUpdateHandler(RedisMessage message) {
        this.message = message;
    }

    @Override
    public void execute() throws InvalidAlgorithmParameterException {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        String familyName = message.getParameter("family-name");

        ServerFamily family = plugin.getProxy().getFamilyManager().find(familyName);

        if (family == null) throw new InvalidAlgorithmParameterException("A family with the name `"+familyName+"` doesn't exist!");

        InetSocketAddress address = message.getAddress();

        ServerInfo serverInfo = new ServerInfo(
                message.getParameter("name"),
                address
        );

        try {
            PaperServer server = family.getServer(serverInfo);

            family.setServerPlayerCount(Integer.parseInt(message.getParameter("player-count")), server);
        } catch (NullPointerException e) {
            throw new InvalidAlgorithmParameterException("The provided server doesn't exist in this family!");
        } catch (NumberFormatException e) {
            throw new InvalidAlgorithmParameterException("The player count provided wasn't valid!");
        }
    }
}
