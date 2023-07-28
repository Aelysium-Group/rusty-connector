package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageCoordinateRequestQueue;
import group.aelysium.rustyconnector.core.lib.serviceable.ServiceableService;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.commands.CommandTPA;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;

import java.util.*;

public class TPAService extends ServiceableService<TPAServiceHandler> {
    private TPASettings settings;
    private Map<BaseServerFamily, TPAHandler> tpaHandlers = Collections.synchronizedMap(new WeakHashMap<>());

    public TPAService(TPASettings settings) {
        super(new TPAServiceHandler());
        this.services.add(new TPACleaningService(settings.expiration()));
        this.settings = settings;
    }
    public void initCommand() {
        CommandManager commandManager = VelocityAPI.get().getServer().getCommandManager();
        if(!commandManager.hasCommand("tpa"))
            try {
                commandManager.register(
                        commandManager.metaBuilder("tpa").build(),
                        CommandTPA.create()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public TPASettings getSettings() {
        return this.settings;
    }

    public TPAHandler getTPAHandler(BaseServerFamily family) {
        TPAHandler tpaHandler = this.tpaHandlers.get(family);
        if(tpaHandler == null) {
            TPAHandler newTPAHandler = new TPAHandler();
            this.tpaHandlers.put(family, newTPAHandler);
            return newTPAHandler;
        }

        return tpaHandler;
    }
    public List<TPAHandler> getAllTPAHandlers() {
        return this.tpaHandlers.values().stream().toList();
    }

    /**
     * Attempts to directly connect a player to a server and then teleport that player to another player.
     * @param source The player requesting to tpa.
     * @param target The player to tpa to.
     * @param targetServer The server to send the player to.
     * @throws NullPointerException If the server doesn't exist in the family.
     */
    public void tpaSendPlayer(Player source, Player target, PlayerServer targetServer) {
        VelocityAPI api = VelocityAPI.get();

        RedisMessageCoordinateRequestQueue message = (RedisMessageCoordinateRequestQueue) new GenericRedisMessage.Builder()
                .setType(RedisMessageType.COORDINATE_REQUEST_QUEUE)
                .setOrigin(MessageOrigin.PROXY)
                .setAddress(targetServer.getAddress())
                .setParameter(RedisMessageCoordinateRequestQueue.ValidParameters.TARGET_SERVER, targetServer.getAddress())
                .setParameter(RedisMessageCoordinateRequestQueue.ValidParameters.TARGET_USERNAME, target.getUsername())
                .setParameter(RedisMessageCoordinateRequestQueue.ValidParameters.SOURCE_USERNAME, source.getUsername())
                .buildSendable();

        api.services().redisService().publish(message);

        try {
            PlayerServer senderServer = api.services().serverService().findServer(source.getCurrentServer().orElseThrow().getServerInfo());

            if (senderServer.equals(targetServer)) return;
        } catch (Exception ignore) {}

        try {
            targetServer.connect(source);
        } catch (Exception e) {
            source.sendMessage(VelocityLang.TPA_FAILURE.build(target.getUsername()));
        }
    }

    @Override
    public void kill() {
        this.getAllTPAHandlers().forEach(TPAHandler::decompose);
        this.tpaHandlers.clear();
        super.kill();
    }

    /**
     * The services that are valid for this service provider.
     */
    public enum ValidServices {
        /**
         * Represents {@link TPACleaningService}
         * This service is required and must always be set.
         */
        TPA_CLEANING_SERVICE,
    }
}
