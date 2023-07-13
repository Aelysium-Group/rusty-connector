package group.aelysium.rustyconnector.plugin.paper.lib.services;

import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageSendPlayer;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageServerPing;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.core.lib.model.Service;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import static group.aelysium.rustyconnector.plugin.paper.central.Processor.ValidServices.REDIS_SERVICE;
import static group.aelysium.rustyconnector.plugin.paper.central.Processor.ValidServices.SERVER_INFO_SERVICE;

public class RedisMessagerService extends Service {

    public void pingProxy(RedisMessageServerPing.ConnectionIntent intent) {
        PaperAPI api = PaperRustyConnector.getAPI();

        try {
            ServerInfoService serverInfoService = api.getService(SERVER_INFO_SERVICE).orElseThrow();
            RedisMessageServerPing message = (RedisMessageServerPing) new GenericRedisMessage.Builder()
                    .setType(RedisMessageType.PING)
                    .setOrigin(MessageOrigin.SERVER)
                    .setAddress(serverInfoService.getAddress())
                    .setParameter(RedisMessageServerPing.ValidParameters.INTENT, intent.toString())
                    .setParameter(RedisMessageServerPing.ValidParameters.FAMILY_NAME, serverInfoService.getFamily())
                    .setParameter(RedisMessageServerPing.ValidParameters.SERVER_NAME, serverInfoService.getName())
                    .setParameter(RedisMessageServerPing.ValidParameters.SOFT_CAP, String.valueOf(serverInfoService.getSoftPlayerCap()))
                    .setParameter(RedisMessageServerPing.ValidParameters.HARD_CAP, String.valueOf(serverInfoService.getHardPlayerCap()))
                    .setParameter(RedisMessageServerPing.ValidParameters.WEIGHT, String.valueOf(serverInfoService.getWeight()))
                    .buildSendable();
            api.getService(REDIS_SERVICE).orElseThrow().publish(message);
        } catch (Exception e) {
            Lang.BOXED_MESSAGE_COLORED.send(PaperRustyConnector.getAPI().getLogger(), Component.text(e.toString()), NamedTextColor.RED);
        }
    }

    /**
     * Requests that the proxy moves this player to another server.
     * @param player The player to send.
     * @param familyName The name of the family to send to.
     */
    public void sendToOtherFamily(Player player, String familyName) {
        PaperAPI api = PaperRustyConnector.getAPI();
        ServerInfoService serverInfoService = api.getService(SERVER_INFO_SERVICE).orElseThrow();

        RedisMessageSendPlayer message = (RedisMessageSendPlayer) new GenericRedisMessage.Builder()
                .setType(RedisMessageType.SEND_PLAYER)
                .setOrigin(MessageOrigin.SERVER)
                .setAddress(serverInfoService.getAddress())
                .setParameter(RedisMessageSendPlayer.ValidParameters.TARGET_FAMILY_NAME, familyName)
                .setParameter(RedisMessageSendPlayer.ValidParameters.PLAYER_UUID, player.getUniqueId().toString())
                .buildSendable();

        api.getService(REDIS_SERVICE).orElseThrow().publish(message);
    }

    @Override
    public void kill() {}
}
