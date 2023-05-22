package group.aelysium.rustyconnector.plugin.velocity.lib.tpa;

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisPublisher;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisService;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageTPAQueuePlayer;
import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.core.lib.model.ClockService;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.Processor;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.StaticServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Map;

public class TPAService extends ClockService {
    protected final long heartbeat;

    public TPAService(long heartbeat) {
        super(true, 3);
        this.heartbeat = heartbeat;
    }

    public TPAService() {
        super(false, 0);
        this.heartbeat = 0;
    }

    /**
     * Attempts to directly connect a player to a server and then teleport that player to another player.
     * @param source The player requesting to tpa.
     * @param target The player to tpa to.
     * @param targetServerInfo The server to send the player to.
     * @throws NullPointerException If the server doesn't exist in the family.
     */
    public void tpaSendPlayer(Player source, Player target, ServerInfo targetServerInfo) {
        this.throwIfDisabled();

        VelocityAPI api = VelocityRustyConnector.getAPI();

        ServerInfo senderServerInfo = source.getCurrentServer().orElseThrow().getServerInfo();

        PlayerServer targetServer = api.getService(ServerService.class).findServer(targetServerInfo);
        if(targetServer == null) throw new NullPointerException();


        RedisMessageTPAQueuePlayer message = (RedisMessageTPAQueuePlayer) new GenericRedisMessage.Builder()
                .setType(RedisMessageType.TPA_QUEUE_PLAYER)
                .setOrigin(MessageOrigin.PROXY)
                .setParameter(RedisMessageTPAQueuePlayer.ValidParameters.TARGET_SERVER, targetServer.getAddress())
                .setParameter(RedisMessageTPAQueuePlayer.ValidParameters.TARGET_USERNAME, target.getUsername())
                .setParameter(RedisMessageTPAQueuePlayer.ValidParameters.SOURCE_USERNAME, source.getUsername())
                .buildSendable();

        RedisPublisher publisher = api.getService(RedisService.class).getMessagePublisher();
        publisher.publish(message);


        if(senderServerInfo.equals(targetServerInfo)) return;

        ConnectionRequestBuilder connection = source.createConnectionRequest(targetServer.getRegisteredServer());
        try {
            connection.connect().get().isSuccessful();
        } catch (Exception e) {
            source.sendMessage(VelocityLang.TPA_FAILURE.build(target.getUsername()));
        }
    }

    public void startHeartbeat() {
        this.throwIfDisabled();

        VelocityAPI api = VelocityRustyConnector.getAPI();
        this.scheduleRecurring(() -> {
            for(BaseServerFamily family : api.getService(FamilyService.class).dump()) {
                if(!(family instanceof PlayerFocusedServerFamily)) continue;

                if(!((PlayerFocusedServerFamily) family).getTPAHandler().getSettings().isEnabled()) continue;
                ((PlayerFocusedServerFamily) family).getTPAHandler().clearExpired();
            }
        }, this.heartbeat);
    }
}
