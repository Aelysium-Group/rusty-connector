package group.aelysium.rustyconnector.plugin.paper.lib.services;

import group.aelysium.rustyconnector.core.lib.database.redis.RedisService;
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

public class RedisMessagerService extends Service {

    public RedisMessagerService() {
        super(true);
    }

    public void pingProxy(RedisMessageServerPing.ConnectionIntent intent) {
        PaperAPI api = PaperRustyConnector.getAPI();

        try {
            RedisMessageServerPing message = (RedisMessageServerPing) new GenericRedisMessage.Builder()
                    .setType(RedisMessageType.PING)
                    .setOrigin(MessageOrigin.SERVER)
                    .setAddress(api.getService(ServerInfoService.class).getAddress())
                    .setParameter(RedisMessageServerPing.ValidParameters.INTENT, intent.toString())
                    .setParameter(RedisMessageServerPing.ValidParameters.FAMILY_NAME, api.getService(ServerInfoService.class).getFamily())
                    .setParameter(RedisMessageServerPing.ValidParameters.SERVER_NAME, api.getService(ServerInfoService.class).getName())
                    .setParameter(RedisMessageServerPing.ValidParameters.SOFT_CAP, String.valueOf(api.getService(ServerInfoService.class).getSoftPlayerCap()))
                    .setParameter(RedisMessageServerPing.ValidParameters.HARD_CAP, String.valueOf(api.getService(ServerInfoService.class).getHardPlayerCap()))
                    .setParameter(RedisMessageServerPing.ValidParameters.WEIGHT, String.valueOf(api.getService(ServerInfoService.class).getWeight()))
                    .buildSendable();
            api.getService(RedisService.class).publish(message);
        } catch (Exception e) {
            Lang.BOXED_MESSAGE_COLORED.send(PaperRustyConnector.getAPI().getLogger(), Component.text(e.getMessage()), NamedTextColor.RED);
        }
    }

    /**
     * Requests that the proxy moves this player to another server.
     * @param player The player to send.
     * @param familyName The name of the family to send to.
     */
    public void sendToOtherFamily(Player player, String familyName) {
        PaperAPI api = PaperRustyConnector.getAPI();

        RedisMessageSendPlayer message = (RedisMessageSendPlayer) new GenericRedisMessage.Builder()
                .setType(RedisMessageType.SEND_PLAYER)
                .setOrigin(MessageOrigin.SERVER)
                .setAddress(api.getService(ServerInfoService.class).getAddress())
                .setParameter(RedisMessageSendPlayer.ValidParameters.TARGET_FAMILY_NAME, familyName)
                .setParameter(RedisMessageSendPlayer.ValidParameters.PLAYER_UUID, player.getUniqueId().toString())
                .buildSendable();

        api.getService(RedisService.class).publish(message);
    }

    @Override
    public void kill() {}
}
