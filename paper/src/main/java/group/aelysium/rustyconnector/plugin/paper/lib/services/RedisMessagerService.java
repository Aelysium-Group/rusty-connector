package group.aelysium.rustyconnector.plugin.paper.lib.services;

import group.aelysium.rustyconnector.core.lib.database.redis.RedisService;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageSendPlayer;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageServerPong;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageServerRegisterRequest;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageServerUnregisterRequest;
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

    public void registerToProxy() {
        PaperAPI api = PaperRustyConnector.getAPI();

        try {
            RedisMessageServerRegisterRequest message = (RedisMessageServerRegisterRequest) new GenericRedisMessage.Builder()
                    .setType(RedisMessageType.REGISTER_SERVER)
                    .setOrigin(MessageOrigin.SERVER)
                    .setAddress(api.getService(ServerInfoService.class).getAddress())
                    .setParameter(RedisMessageServerRegisterRequest.ValidParameters.FAMILY_NAME, api.getService(ServerInfoService.class).getFamily())
                    .setParameter(RedisMessageServerRegisterRequest.ValidParameters.SERVER_NAME, api.getService(ServerInfoService.class).getName())
                    .setParameter(RedisMessageServerRegisterRequest.ValidParameters.SOFT_CAP, String.valueOf(api.getService(ServerInfoService.class).getSoftPlayerCap()))
                    .setParameter(RedisMessageServerRegisterRequest.ValidParameters.HARD_CAP, String.valueOf(api.getService(ServerInfoService.class).getHardPlayerCap()))
                    .setParameter(RedisMessageServerRegisterRequest.ValidParameters.WEIGHT, String.valueOf(api.getService(ServerInfoService.class).getWeight()))
                    .buildSendable();
            api.getService(RedisService.class).publish(message);
        } catch (Exception e) {
            Lang.BOXED_MESSAGE_COLORED.send(PaperRustyConnector.getAPI().getLogger(), Component.text(e.getMessage()), NamedTextColor.RED);
        }
    }

    public void unregisterFromProxy() {
        PaperAPI api = PaperRustyConnector.getAPI();

        RedisMessageServerUnregisterRequest message = (RedisMessageServerUnregisterRequest) new GenericRedisMessage.Builder()
                .setType(RedisMessageType.UNREGISTER_SERVER)
                .setOrigin(MessageOrigin.SERVER)
                .setAddress(api.getService(ServerInfoService.class).getAddress())
                .setParameter(RedisMessageServerUnregisterRequest.ValidParameters.FAMILY_NAME, api.getService(ServerInfoService.class).getFamily())
                .setParameter(RedisMessageServerUnregisterRequest.ValidParameters.SERVER_NAME, api.getService(ServerInfoService.class).getName())
                .buildSendable();

        api.getService(RedisService.class).publish(message);
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

    /**
     * Sends a pong message to the proxy.
     */
    public void pong() {
        PaperAPI api = PaperRustyConnector.getAPI();

        int playerCount = PaperRustyConnector.getAPI().getServer().getOnlinePlayers().size();

        RedisMessageServerPong message = (RedisMessageServerPong) new GenericRedisMessage.Builder()
                .setType(RedisMessageType.PONG)
                .setOrigin(MessageOrigin.SERVER)
                .setAddress(api.getService(ServerInfoService.class).getAddress())
                .setParameter(RedisMessageServerPong.ValidParameters.SERVER_NAME, api.getService(ServerInfoService.class).getName())
                .setParameter(RedisMessageServerPong.ValidParameters.PLAYER_COUNT, String.valueOf(playerCount))
                .buildSendable();

        api.getService(RedisService.class).publish(message);
    }

    @Override
    public void kill() {}
}
