package group.aelysium.rustyconnector.plugin.paper.lib.services;

import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageSendPlayer;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageServerPing;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class RedisMessagerService extends Service {

    public void pingProxy(RedisMessageServerPing.ConnectionIntent intent) {
        PaperAPI api = PaperAPI.get();

        try {
            ServerInfoService serverInfoService = api.services().serverInfoService();
            RedisMessageServerPing message = (RedisMessageServerPing) new GenericRedisMessage.Builder()
                    .setType(RedisMessageType.PING)
                    .setOrigin(MessageOrigin.SERVER)
                    .setAddress(serverInfoService.address())
                    .setParameter(RedisMessageServerPing.ValidParameters.INTENT, intent.toString())
                    .setParameter(RedisMessageServerPing.ValidParameters.FAMILY_NAME, serverInfoService.family())
                    .setParameter(RedisMessageServerPing.ValidParameters.SERVER_NAME, serverInfoService.name())
                    .setParameter(RedisMessageServerPing.ValidParameters.SOFT_CAP, String.valueOf(serverInfoService.softPlayerCap()))
                    .setParameter(RedisMessageServerPing.ValidParameters.HARD_CAP, String.valueOf(serverInfoService.hardPlayerCap()))
                    .setParameter(RedisMessageServerPing.ValidParameters.WEIGHT, String.valueOf(serverInfoService.weight()))
                    .setParameter(RedisMessageServerPing.ValidParameters.PLAYER_COUNT, String.valueOf(serverInfoService.playerCount()))
                    .buildSendable();
            api.services().redisService().publish(message);
        } catch (Exception e) {
            Lang.BOXED_MESSAGE_COLORED.send(PaperAPI.get().logger(), Component.text(e.toString()), NamedTextColor.RED);
        }
    }

    /**
     * Requests that the proxy moves this player to another server.
     * @param player The player to send.
     * @param familyName The name of the family to send to.
     */
    public void sendToOtherFamily(Player player, String familyName) {
        PaperAPI api = PaperAPI.get();
        ServerInfoService serverInfoService = api.services().serverInfoService();

        RedisMessageSendPlayer message = (RedisMessageSendPlayer) new GenericRedisMessage.Builder()
                .setType(RedisMessageType.SEND_PLAYER)
                .setOrigin(MessageOrigin.SERVER)
                .setAddress(serverInfoService.address())
                .setParameter(RedisMessageSendPlayer.ValidParameters.TARGET_FAMILY_NAME, familyName)
                .setParameter(RedisMessageSendPlayer.ValidParameters.PLAYER_UUID, player.getUniqueId().toString())
                .buildSendable();

        api.services().redisService().publish(message);
    }

    @Override
    public void kill() {}
}
