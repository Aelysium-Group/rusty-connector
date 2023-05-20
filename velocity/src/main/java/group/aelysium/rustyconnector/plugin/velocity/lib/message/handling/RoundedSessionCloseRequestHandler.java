package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageHandler;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageRoundedSessionCloseRequest;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.RoundedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.rounded.RoundedServer;

public class RoundedSessionCloseRequestHandler implements MessageHandler {
    private final RedisMessageRoundedSessionCloseRequest message;

    public RoundedSessionCloseRequestHandler(GenericRedisMessage message) {
        this.message = (RedisMessageRoundedSessionCloseRequest) message;
    }

    @Override
    public void execute() throws Exception {
        VelocityAPI api = VelocityRustyConnector.getAPI();

        RoundedServerFamily family = (RoundedServerFamily) api.getVirtualProcessor().getFamilyManager().find(message.getFamilyName());

        RoundedServer server = family.getServer(new ServerInfo(message.getServerName(), message.getAddress()));
        if(server == null) throw new Exception("The server that was attempted to be found doesn't exist on this family!");

        server.closeSession();
    }
}
