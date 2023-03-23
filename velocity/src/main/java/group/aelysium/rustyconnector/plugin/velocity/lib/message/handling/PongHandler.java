package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.data_messaging.MessageHandler;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessage;
import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PaperServer;

import java.net.InetSocketAddress;

public class PongHandler implements MessageHandler {
    private final RedisMessage message;

    public PongHandler(RedisMessage message) {
        this.message = message;
    }

    @Override
    public void execute() throws Exception {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        InetSocketAddress address = message.getAddress();

        ServerInfo serverInfo = new ServerInfo(
                message.getParameter("name"),
                address
        );

        try {
            PaperServer server = plugin.getVirtualServer().findServer(serverInfo);
            if(server == null) return;
            plugin.getVirtualServer().reviveServer(serverInfo);
            server.setPlayerCount(Integer.parseInt(message.getParameter("player-count")));

            if(plugin.logger().getGate().check(GateKey.PONG))
                VelocityLang.PONG.send(plugin.logger(), serverInfo);
        } catch (Exception e) {
            if(plugin.logger().getGate().check(GateKey.PONG))
                VelocityLang.PONG_CANCELED.send(plugin.logger(), serverInfo);
            throw new Exception(e.getMessage());
        }
    }
}
