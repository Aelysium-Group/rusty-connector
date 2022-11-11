package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.message.MessageHandler;
import group.aelysium.rustyconnector.core.lib.message.RedisMessage;
import group.aelysium.rustyconnector.core.lib.util.logger.GateKey;
import group.aelysium.rustyconnector.core.lib.util.logger.Lang;
import group.aelysium.rustyconnector.core.lib.util.logger.LangKey;
import group.aelysium.rustyconnector.core.lib.util.logger.LangMessage;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PaperServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.ServerFamily;

import java.net.InetSocketAddress;
import java.security.InvalidAlgorithmParameterException;

public class PongHandler implements MessageHandler {
    private final RedisMessage message;

    public PongHandler(RedisMessage message) {
        this.message = message;
    }

    @Override
    public void execute() throws InvalidAlgorithmParameterException {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        InetSocketAddress address = message.getAddress();

        ServerInfo serverInfo = new ServerInfo(
                message.getParameter("name"),
                address
        );

        try {
            plugin.getProxy().reviveServer(serverInfo);

            PaperServer server = plugin.getProxy().findServer(serverInfo);
            server.setPlayerCount(Integer.parseInt(message.getParameter("player-count")));

            if(plugin.logger().getGate().check(GateKey.PONG))
                (new LangMessage(plugin.logger()))
                        .insert(
                                "Proxy" +
                                        " "+ Lang.get(LangKey.ICON_PONG) +" " +
                                        "["+serverInfo.getName()+"]" +
                                        "("+serverInfo.getAddress().getHostName()+":"+serverInfo.getAddress().getPort()+")"
                        )
                        .print();
        } catch (Exception error) {
            if(plugin.logger().getGate().check(GateKey.PONG))
                (new LangMessage(plugin.logger()))
                        .insert(
                                "Proxy" +
                                        " "+ Lang.get(LangKey.ICON_CANCELED) +" " +
                                        "["+serverInfo.getName()+"]" +
                                        "("+serverInfo.getAddress().getHostName()+":"+serverInfo.getAddress().getPort()+")"
                        )
                        .insert(error.getMessage())
                        .print();
        }
    }
}
