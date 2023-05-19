package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageHandler;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageRoundedFamilyCancelPreConnect;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageRoundedSessionEnd;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.RoundedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.rounded.RoundedServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.processor.VirtualProxyProcessor;

public class RoundedSessionEndHandler implements MessageHandler {
    private final RedisMessageRoundedSessionEnd message;

    public RoundedSessionEndHandler(GenericRedisMessage message) {
        this.message = (RedisMessageRoundedSessionEnd) message;
    }

    @Override
    public void execute() throws Exception {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        VirtualProxyProcessor processor = api.getVirtualProcessor();

        BaseServerFamily family = processor.getFamilyManager().find(this.message.getFamilyName());
        if(family == null) throw new Exception("The requested family doesn't exist!");
        if(!(family instanceof RoundedServerFamily)) throw new Exception("The requested family must be a Rounded Family!");

        RoundedServer server = ((RoundedServerFamily) family).getServer(new ServerInfo(this.message.getServerName(), this.message.getAddress()));
        if(server == null) throw new Exception("The requested server doesn't exist!");
        server.endSession();
    }
}
