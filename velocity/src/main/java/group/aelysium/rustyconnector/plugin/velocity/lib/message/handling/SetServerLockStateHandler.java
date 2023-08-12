package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageHandler;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageServerLockState;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;

import java.util.List;

public class SetServerLockStateHandler implements MessageHandler {
    private final RedisMessageServerLockState message;

    public SetServerLockStateHandler(GenericRedisMessage message) {
        this.message = (RedisMessageServerLockState) message;
    }

    @Override
    public void execute() throws Exception {
        VelocityAPI api = VelocityAPI.get();

        try {
            ServerInfo serverInfo = api.velocityServer().getServer(this.message.serverName()).get().getServerInfo();
            api.services().serverService().search(serverInfo).setLocked(this.message.lockState());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
