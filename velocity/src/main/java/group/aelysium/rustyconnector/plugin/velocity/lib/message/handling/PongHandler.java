package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.data_messaging.MessageHandler;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessage;
import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.core.lib.lang_messaging.LangKey;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.LoggerConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import net.kyori.adventure.text.Component;

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

            if(plugin.logger().getGate().check(GateKey.PONG))
                VelocityLang.PONG.send(plugin.logger(), serverInfo);
        } catch (Exception error) {
            if(plugin.logger().getGate().check(GateKey.PONG))
                VelocityLang.PONG_CANCELED.send(plugin.logger(), serverInfo);
        }
    }
}
