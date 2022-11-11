package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.util.logger.Lang;
import group.aelysium.rustyconnector.core.lib.message.MessageHandler;
import group.aelysium.rustyconnector.core.lib.message.RedisMessage;
import group.aelysium.rustyconnector.core.lib.util.logger.GateKey;
import group.aelysium.rustyconnector.core.lib.util.logger.LangKey;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PaperServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.ServerFamily;

import java.net.InetSocketAddress;
import java.security.InvalidAlgorithmParameterException;

public class ServerUnRegHandler implements MessageHandler {
    private final RedisMessage message;

    public ServerUnRegHandler(RedisMessage message) {
        this.message = message;
    }

    @Override
    public void execute() throws InvalidAlgorithmParameterException {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        String familyName = message.getParameter("family");

        ServerFamily family = plugin.getProxy().getFamilyManager().find(familyName);

        InetSocketAddress address = message.getAddress();

        ServerInfo serverInfo = new ServerInfo(
                message.getParameter("name"),
                address
        );

        plugin.getProxy().unregisterServer(serverInfo,familyName);
    }
}
