package group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.handlers;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisService;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageHandler;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageServerPing;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageServerPingResponse;
import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.format.NamedTextColor;

import java.net.InetSocketAddress;

public class MagicLinkPingHandler implements MessageHandler {
    private final RedisMessageServerPing message;

    public MagicLinkPingHandler(GenericRedisMessage message) {
        this.message = (RedisMessageServerPing) message;
    }

    @Override
    public void execute() throws Exception {
        InetSocketAddress address = message.address();
        VelocityAPI api = VelocityAPI.get();

        ServerInfo serverInfo = new ServerInfo(
                message.serverName(),
                address
        );

        if(api.logger().loggerGate().check(GateKey.PING))
            api.logger().send(VelocityLang.PING.build(serverInfo));

        if(message.intent() == RedisMessageServerPing.ConnectionIntent.CONNECT)
            this.reviveOrConnectServer(serverInfo);
        if(message.intent() == RedisMessageServerPing.ConnectionIntent.DISCONNECT)
            this.disconnectServer(serverInfo);
    }

    private boolean connectServer(ServerInfo serverInfo) {
        VelocityAPI api = VelocityAPI.get();
        ServerService serverService = api.services().serverService();
        RedisService redisService = api.services().redisService();

        try {
            PlayerServer server = new ServerService.ServerBuilder()
                    .setServerInfo(serverInfo)
                    .setFamilyName(message.familyName())
                    .setSoftPlayerCap(message.softCap())
                    .setHardPlayerCap(message.hardCap())
                    .setWeight(message.weight())
                    .build();

            server.register(message.familyName());

            RedisMessageServerPingResponse message = (RedisMessageServerPingResponse) new GenericRedisMessage.Builder()
                    .setType(RedisMessageType.PING_RESPONSE)
                    .setAddress(serverInfo.getAddress())
                    .setOrigin(MessageOrigin.PROXY)
                    .setParameter(RedisMessageServerPingResponse.ValidParameters.STATUS, String.valueOf(RedisMessageServerPingResponse.PingResponseStatus.ACCEPTED))
                    .setParameter(RedisMessageServerPingResponse.ValidParameters.MESSAGE, "Connected to the proxy!")
                    .setParameter(RedisMessageServerPingResponse.ValidParameters.COLOR, NamedTextColor.GREEN.toString())
                    .setParameter(RedisMessageServerPingResponse.ValidParameters.INTERVAL_OPTIONAL, String.valueOf(serverService.serverInterval()))
                    .buildSendable();
            redisService.publish(message);

            return true;
        } catch(Exception e) {
            RedisMessageServerPingResponse message = (RedisMessageServerPingResponse) new GenericRedisMessage.Builder()
                    .setType(RedisMessageType.PING_RESPONSE)
                    .setAddress(serverInfo.getAddress())
                    .setOrigin(MessageOrigin.PROXY)
                    .setParameter(RedisMessageServerPingResponse.ValidParameters.STATUS, String.valueOf(RedisMessageServerPingResponse.PingResponseStatus.DENIED))
                    .setParameter(RedisMessageServerPingResponse.ValidParameters.MESSAGE, "Attempt to connect to proxy failed! " + e.getMessage())
                    .setParameter(RedisMessageServerPingResponse.ValidParameters.COLOR, NamedTextColor.RED.toString())
                    .buildSendable();
            redisService.publish(message);
        }
        return false;
    }

    private boolean disconnectServer(ServerInfo serverInfo) throws Exception {
        VelocityAPI api = VelocityAPI.get();
        api.services().serverService().unregisterServer(serverInfo, message.familyName(), true);

        return true;
    }

    private boolean reviveOrConnectServer(ServerInfo serverInfo) {
        VelocityAPI api = VelocityAPI.get();
        ServerService serverService = api.services().serverService();

        PlayerServer server = serverService.search(serverInfo);
        if (server == null) {
            return this.connectServer(serverInfo);
        }

        server.setTimeout(serverService.serverTimeout());
        server.setPlayerCount(this.message.playerCount());
        return true;
    }
}
