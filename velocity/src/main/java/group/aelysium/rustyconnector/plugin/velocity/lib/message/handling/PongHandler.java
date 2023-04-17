package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.central.PluginLogger;
import group.aelysium.rustyconnector.core.central.PluginRuntime;
import group.aelysium.rustyconnector.core.lib.data_messaging.MessageHandler;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessage;
import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PlayerServer;

import java.net.InetSocketAddress;

public class PongHandler implements MessageHandler {
    private final RedisMessage message;

    public PongHandler(RedisMessage message) {
        this.message = message;
    }

    @Override
    public void execute() throws Exception {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        InetSocketAddress address = message.getAddress();

        ServerInfo serverInfo = new ServerInfo(
                message.getParameter("name"),
                address
        );

        try {
            PlayerServer server = api.getVirtualProcessor().findServer(serverInfo);
            if(server == null) return;
            api.getVirtualProcessor().reviveServer(serverInfo);
            server.setPlayerCount(Integer.parseInt(message.getParameter("player-count")));

            if(logger.getGate().check(GateKey.PONG))
                VelocityLang.PONG.send(logger, serverInfo);
        } catch (Exception e) {
            if(logger.getGate().check(GateKey.PONG))
                VelocityLang.PONG_CANCELED.send(logger, serverInfo);
            throw new Exception(e.getMessage());
        }
    }
}
