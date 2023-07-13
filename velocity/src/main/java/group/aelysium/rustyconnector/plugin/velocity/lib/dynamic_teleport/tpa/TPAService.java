package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageTPAQueuePlayer;
import group.aelysium.rustyconnector.core.lib.model.Service;
import group.aelysium.rustyconnector.core.lib.model.ServiceableService;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.commands.CommandTPA;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;

import java.util.*;

import static group.aelysium.rustyconnector.plugin.velocity.central.Processor.ValidServices.*;

public class TPAService extends ServiceableService {
    private TPASettings settings;
    private Map<BaseServerFamily, TPAHandler> tpaHandlers = Collections.synchronizedMap(new WeakHashMap<>());

    public TPAService(TPASettings settings) {
        super(new HashMap<>());
        this.services.put(TPACleaningService.class, new TPACleaningService(settings.expiration()));
        this.settings = settings;
    }
    public void initCommand() {
        CommandManager commandManager = VelocityRustyConnector.getAPI().getServer().getCommandManager();
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

    public Optional<TPAHandler> getTPAHandler(BaseServerFamily family) {
        try {
            return Optional.of(Objects.requireNonNull(this.tpaHandlers.get(family)));
        } catch (Exception ignore) {}
        return Optional.empty();
    }
    public List<TPAHandler> getAllTPAHandlers() {
        return this.tpaHandlers.values().stream().toList();
    }

    /**
     * Attempts to directly connect a player to a server and then teleport that player to another player.
     * @param source The player requesting to tpa.
     * @param target The player to tpa to.
     * @param targetServerInfo The server to send the player to.
     * @throws NullPointerException If the server doesn't exist in the family.
     */
    public void tpaSendPlayer(Player source, Player target, ServerInfo targetServerInfo) {
        VelocityAPI api = VelocityRustyConnector.getAPI();

        ServerInfo senderServerInfo = source.getCurrentServer().orElseThrow().getServerInfo();

        PlayerServer targetServer = api.getService(SERVER_SERVICE).orElseThrow().findServer(targetServerInfo);
        if(targetServer == null) throw new NullPointerException();


        RedisMessageTPAQueuePlayer message = (RedisMessageTPAQueuePlayer) new GenericRedisMessage.Builder()
                .setType(RedisMessageType.TPA_QUEUE_PLAYER)
                .setOrigin(MessageOrigin.PROXY)
                .setParameter(RedisMessageTPAQueuePlayer.ValidParameters.TARGET_SERVER, targetServer.getAddress())
                .setParameter(RedisMessageTPAQueuePlayer.ValidParameters.TARGET_USERNAME, target.getUsername())
                .setParameter(RedisMessageTPAQueuePlayer.ValidParameters.SOURCE_USERNAME, source.getUsername())
                .buildSendable();

        api.getService(REDIS_SERVICE).orElseThrow().publish(message);


        if(senderServerInfo.equals(targetServerInfo)) return;

        ConnectionRequestBuilder connection = source.createConnectionRequest(targetServer.getRegisteredServer());
        try {
            connection.connect().get().isSuccessful();
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
     * Services marked as @Optional should be handled accordingly.
     * If a service is not marked @Optional it should be impossible for that service to be unavailable.
     */
    public static class ValidServices {
        public static Class<TPACleaningService> TPA_CLEANING_SERVICE = TPACleaningService.class;

        public static boolean isOptional(Class<? extends Service> clazz) {
            return false;
        }
    }
}
