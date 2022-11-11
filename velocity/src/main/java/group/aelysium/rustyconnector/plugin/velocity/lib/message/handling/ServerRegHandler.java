package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.message.MessageHandler;
import group.aelysium.rustyconnector.core.lib.message.RedisMessage;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PaperServer;

import java.net.InetSocketAddress;
import java.security.InvalidAlgorithmParameterException;

public class ServerRegHandler implements MessageHandler {
    private final RedisMessage message;

    public ServerRegHandler(RedisMessage message) {
        this.message = message;
    }

    @Override
    public void execute() throws InvalidAlgorithmParameterException {
        String familyName = message.getParameter("family");

        InetSocketAddress address = message.getAddress();

        String serverName = message.getParameter("name");

        ServerInfo serverInfo = new ServerInfo(
                serverName,
                address
        );

        PaperServer server = new PaperServer(
                serverInfo,
                Integer.parseInt(message.getParameter("soft-cap")),
                Integer.parseInt(message.getParameter("hard-cap")),
                Integer.parseInt(message.getParameter("priority"))
        );

        server.register(familyName);
        server.setPlayerCount(0);
    }
}
