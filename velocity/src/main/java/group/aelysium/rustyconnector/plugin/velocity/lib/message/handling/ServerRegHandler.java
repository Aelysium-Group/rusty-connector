package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageHandler;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageServerRegisterRequest;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PlayerServer;

import java.net.InetSocketAddress;

public class ServerRegHandler implements MessageHandler {
    private final RedisMessageServerRegisterRequest message;

    public ServerRegHandler(GenericRedisMessage message) {
        this.message = (RedisMessageServerRegisterRequest) message;
    }

    @Override
    public void execute() throws Exception {
        InetSocketAddress address = message.getAddress();

        ServerInfo serverInfo = new ServerInfo(
                message.getServerName(),
                address
        );

        PlayerServer server = new PlayerServer(
                serverInfo,
                message.getSoftCap(),
                message.getHardCap(),
                message.getWeight()
        );

        server.register(message.getFamilyName());
    }
}
