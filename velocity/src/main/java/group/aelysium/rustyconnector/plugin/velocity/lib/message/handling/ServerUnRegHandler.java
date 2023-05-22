package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageHandler;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageServerUnregisterRequest;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;

import java.net.InetSocketAddress;

public class ServerUnRegHandler implements MessageHandler {
    private final RedisMessageServerUnregisterRequest message;

    public ServerUnRegHandler(GenericRedisMessage message) {
        this.message = (RedisMessageServerUnregisterRequest) message;
    }

    @Override
    public void execute() throws Exception {
        VelocityAPI api = VelocityRustyConnector.getAPI();

        InetSocketAddress address = message.getAddress();

        ServerInfo serverInfo = new ServerInfo(
                message.getServerName(),
                address
        );

        api.getService(ServerService.class).unregisterServer(serverInfo, message.getFamilyName(), true);
    }
}
