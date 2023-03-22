package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.data_messaging.MessageHandler;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessage;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;

import java.net.InetSocketAddress;

public class ServerUnRegHandler implements MessageHandler {
    private final RedisMessage message;

    public ServerUnRegHandler(RedisMessage message) {
        this.message = message;
    }

    @Override
    public void execute() throws Exception {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        String familyName = message.getParameter("family");

        InetSocketAddress address = message.getAddress();

        ServerInfo serverInfo = new ServerInfo(
                message.getParameter("name"),
                address
        );

        plugin.getProxy().unregisterServer(serverInfo, familyName, true);
    }
}
