package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisService;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageHandler;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageServerPing;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageServerPingResponse;
import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.format.NamedTextColor;

import java.net.InetSocketAddress;

public class PingHandler implements MessageHandler {
    private final RedisMessageServerPing message;

    public PingHandler(GenericRedisMessage message) {
        this.message = (RedisMessageServerPing) message;
    }

    @Override
    public void execute() throws Exception {
        InetSocketAddress address = message.getAddress();
        VelocityAPI api = VelocityRustyConnector.getAPI();
        ServerService serverService = api.getService(ServerService.class);

        ServerInfo serverInfo = new ServerInfo(
                message.getServerName(),
                address
        );

        if(api.getLogger().getGate().check(GateKey.PING))
            api.getLogger().send(VelocityLang.PING.build(serverInfo));

        {
            PlayerServer server = serverService.findServer(serverInfo);
            if (server != null) {
                server.setTimeout(serverService.getServerTimeout());
                return;
            }
        }
        {
            try {
                PlayerServer server = new ServerService.ServerBuilder()
                        .setServerInfo(serverInfo)
                        .setFamilyName(message.getFamilyName())
                        .setSoftPlayerCap(message.getSoftCap())
                        .setHardPlayerCap(message.getHardCap())
                        .setWeight(message.getWeight())
                        .build();

                server.register(message.getFamilyName());

                RedisMessageServerPingResponse message = (RedisMessageServerPingResponse) new GenericRedisMessage.Builder()
                        .setType(RedisMessageType.PING_RESPONSE)
                        .setAddress(address)
                        .setOrigin(MessageOrigin.PROXY)
                        .setParameter(RedisMessageServerPingResponse.ValidParameters.STATUS, String.valueOf(RedisMessageServerPingResponse.PingResponseStatus.ACCEPTED))
                        .setParameter(RedisMessageServerPingResponse.ValidParameters.MESSAGE, "Connected to the proxy!")
                        .setParameter(RedisMessageServerPingResponse.ValidParameters.COLOR, NamedTextColor.GREEN.toString())
                        .setParameter(RedisMessageServerPingResponse.ValidParameters.INTERVAL_OPTIONAL, String.valueOf(serverService.getServerInterval()))
                        .buildSendable();
                VelocityRustyConnector.getAPI().getService(RedisService.class).publish(message);
            } catch(Exception e) {
                RedisMessageServerPingResponse message = (RedisMessageServerPingResponse) new GenericRedisMessage.Builder()
                        .setType(RedisMessageType.PING_RESPONSE)
                        .setAddress(address)
                        .setOrigin(MessageOrigin.PROXY)
                        .setParameter(RedisMessageServerPingResponse.ValidParameters.STATUS, String.valueOf(RedisMessageServerPingResponse.PingResponseStatus.DENIED))
                        .setParameter(RedisMessageServerPingResponse.ValidParameters.MESSAGE, "Attempt to connect to proxy failed! " + e.getMessage())
                        .setParameter(RedisMessageServerPingResponse.ValidParameters.COLOR, NamedTextColor.RED.toString())
                        .buildSendable();
                VelocityRustyConnector.getAPI().getService(RedisService.class).publish(message);
            }
        }
    }
}
