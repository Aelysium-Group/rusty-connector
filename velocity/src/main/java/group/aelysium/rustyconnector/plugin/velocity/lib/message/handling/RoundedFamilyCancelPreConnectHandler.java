package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageHandler;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageRoundedCancelPreConnect;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.RoundedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.central.Processor;

public class RoundedFamilyCancelPreConnectHandler implements MessageHandler {
    private final RedisMessageRoundedCancelPreConnect message;

    public RoundedFamilyCancelPreConnectHandler(GenericRedisMessage message) {
        this.message = (RedisMessageRoundedCancelPreConnect) message;
    }

    @Override
    public void execute() throws Exception {
        VelocityAPI api = VelocityRustyConnector.getAPI();

        BaseServerFamily family = api.getService(FamilyService.class).find(this.message.getFamilyName());
        if(family == null) throw new Exception("The requested family doesn't exist!");
        if(!(family instanceof RoundedServerFamily)) throw new Exception("The requested family must be a Rounded Family!");

        Player player = api.getServer().getPlayer(this.message.getUUID()).orElse(null);
        if(player == null) throw new Exception("The requested player doesn't exist!");
        if(player.isActive()) throw new Exception("The requested player isn't online!");

        ((RoundedServerFamily) family).cancelPreConnection(player);
    }
}
