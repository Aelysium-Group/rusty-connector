package group.aelysium.rustyconnector.plugin.paper.lib;

import group.aelysium.rustyconnector.core.lib.database.redis.RedisClient;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisPublisher;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisService;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageSendPlayer;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageServerPong;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageServerRegisterRequest;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageServerUnregisterRequest;
import group.aelysium.rustyconnector.core.lib.hash.MD5;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.cache.MessageCache;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.core.lib.model.PlayerServer;
import group.aelysium.rustyconnector.core.lib.model.VirtualProcessor;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.paper.config.PrivateKeyConfig;
import group.aelysium.rustyconnector.plugin.paper.lib.database.RedisSubscriber;
import group.aelysium.rustyconnector.plugin.paper.lib.tpa.TPAQueue;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.io.File;
import java.net.InetSocketAddress;

public class VirtualServerProcessor implements PlayerServer, VirtualProcessor {
    private final TPAQueue tpaQueue = new TPAQueue();
    private MessageCache messageCache;
    private final RedisService redisService;
    private final String family;
    private final int weight = 0;
    private int softPlayerCap = 20;
    private int hardPlayerCap = 30;
    private final String name;
    private final InetSocketAddress address;

    public VirtualServerProcessor(String name, String address, String family, RedisService redisService) {
        if(name.equals("")) {
            name = MD5.generatePrivateKey(); // Generate a custom string to be the server's name
        }
        this.name = name;

        String[] addressSplit = address.split(":");

        this.address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));

        this.family = family;

        this.redisService = redisService;
    }

    /**
     * Set the message cache for the proxy. Once this is set it cannot be changed.
     * @param max The max number of messages the cache will accept.
     */
    public void setMessageCache(int max) throws IllegalStateException {
        if(this.messageCache != null) throw new IllegalStateException("This has already been set! You can't set this twice!");

        if(max <= 0) max = 0;
        if(max > 500) max = 500;
        this.messageCache = new MessageCache(max);
    }

    public MessageCache getMessageCache() {
        return this.messageCache;
    }

    public String getAddress() {
        return this.address.getHostName() + ":" + this.address.getPort();
    }

    public String getFamily() { return this.family; }

    @Override
    public int getPlayerCount() {
        return PaperRustyConnector.getAPI().getServer().getOnlinePlayers().size();
    }

    @Override
    public int getSortIndex() {
        return 0;
    }

    @Override
    public int getWeight() {
        return this.weight;
    }

    @Override
    public int getSoftPlayerCap() {
        return this.softPlayerCap;
    }

    @Override
    public int getHardPlayerCap() {
        return this.hardPlayerCap;
    }

    public void closeRedis() {
        this.redisService.kill();
    }
    public RedisService getRedisService() { return this.redisService; }

    /**
     * Set the player cap for this server. If soft cap is larger than hard cap. Set soft cap to be the same value as hard cap.
     * @param softPlayerCap The soft player cap
     * @param hardPlayerCap The hard player cap
     */
    public void setPlayerCap(int softPlayerCap, int hardPlayerCap) {
        PaperAPI api = PaperRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        api.getServer().setMaxPlayers(hardPlayerCap);

        if(softPlayerCap >= hardPlayerCap) {
            this.hardPlayerCap = hardPlayerCap;
            this.softPlayerCap = hardPlayerCap;
            logger.log("soft-player-cap was set to be larger than hard-player-cap. Running in `player-limit` mode.");
            return;
        }
        this.hardPlayerCap = hardPlayerCap;
        this.softPlayerCap = softPlayerCap;
    }

    public void registerToProxy() {
        try {
            System.out.println("building...");
            RedisMessageServerRegisterRequest message = (RedisMessageServerRegisterRequest) new GenericRedisMessage.Builder()
                    .setType(RedisMessageType.REG)
                    .setOrigin(MessageOrigin.SERVER)
                    .setAddress(this.getAddress())
                    .setParameter(RedisMessageServerRegisterRequest.ValidParameters.FAMILY_NAME, this.family)
                    .setParameter(RedisMessageServerRegisterRequest.ValidParameters.SERVER_NAME, this.name)
                    .setParameter(RedisMessageServerRegisterRequest.ValidParameters.SOFT_CAP, String.valueOf(this.softPlayerCap))
                    .setParameter(RedisMessageServerRegisterRequest.ValidParameters.HARD_CAP, String.valueOf(this.hardPlayerCap))
                    .setParameter(RedisMessageServerRegisterRequest.ValidParameters.WEIGHT, String.valueOf(this.weight))
                    .buildSendable();
            System.out.println("built.");

            System.out.println("sending...");
            this.getRedisService().publish(message);
        } catch (Exception e) {
            Lang.BOXED_MESSAGE_COLORED.send(PaperRustyConnector.getAPI().getLogger(), Component.text(e.getMessage()), NamedTextColor.RED);
        }
    }

    public void unregisterFromProxy() {
        RedisMessageServerUnregisterRequest message = (RedisMessageServerUnregisterRequest) new GenericRedisMessage.Builder()
                .setType(RedisMessageType.UNREG)
                .setOrigin(MessageOrigin.SERVER)
                .setAddress(this.getAddress())
                .setParameter(RedisMessageServerUnregisterRequest.ValidParameters.FAMILY_NAME, this.family)
                .setParameter(RedisMessageServerUnregisterRequest.ValidParameters.SERVER_NAME, this.name)
                .buildSendable();

        this.getRedisService().publish(message);
    }

    /**
     * Requests that the proxy moves this player to another server.
     * @param player The player to send.
     * @param familyName The name of the family to send to.
     */
    public void sendToOtherFamily(Player player, String familyName) {
        RedisMessageSendPlayer message = (RedisMessageSendPlayer) new GenericRedisMessage.Builder()
                .setType(RedisMessageType.SEND)
                .setOrigin(MessageOrigin.SERVER)
                .setAddress(this.getAddress())
                .setParameter(RedisMessageSendPlayer.ValidParameters.TARGET_FAMILY_NAME, familyName)
                .setParameter(RedisMessageSendPlayer.ValidParameters.PLAYER_UUID, player.getUniqueId().toString())
                .buildSendable();

        this.getRedisService().publish(message);
    }

    /**
     * Sends a pong message to the proxy.
     */
    public void pong() {
        RedisMessageServerPong message = (RedisMessageServerPong) new GenericRedisMessage.Builder()
                .setType(RedisMessageType.PONG)
                .setOrigin(MessageOrigin.SERVER)
                .setAddress(this.getAddress())
                .setParameter(RedisMessageServerPong.ValidParameters.SERVER_NAME, this.name)
                .setParameter(RedisMessageServerPong.ValidParameters.PLAYER_COUNT, String.valueOf(this.getPlayerCount()))
                .buildSendable();

        this.getRedisService().publish(message);
    }

    public static VirtualServerProcessor init(DefaultConfig config) throws IllegalAccessException {
        PaperAPI api = PaperRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        // Setup private key
        PrivateKeyConfig privateKeyConfig = PrivateKeyConfig.newConfig(new File(String.valueOf(api.getDataFolder()), "private.key"));
        if(!privateKeyConfig.generate())
            throw new IllegalStateException("Unable to load or create private.key!");
        char[] privateKey = null;
        try {
            privateKey = privateKeyConfig.get();
        } catch (Exception ignore) {}
        if(privateKey == null) throw new IllegalAccessException("There was a fatal error while reading private.key!");


        logger.log("Preparing Redis...");

        // Setup Redis
        RedisClient.Builder redisClientBuilder = new RedisClient.Builder()
                .setHost(config.getRedis_host())
                .setPort(config.getRedis_port())
                .setUser(config.getRedis_user())
                .setDataChannel(config.getRedis_dataChannel());

        if(!config.getRedis_password().equals(""))
            redisClientBuilder.setPassword(config.getRedis_password());

        logger.log("Finished setting up redis");


        VirtualServerProcessor server = new VirtualServerProcessor(
                config.getServer_name(),
                config.getServer_address(),
                config.getServer_family(),
                new RedisService(redisClientBuilder, privateKey)
        );
        server.setMessageCache(50);

        server.setPlayerCap(
                config.getServer_playerCap_soft(),
                config.getServer_playerCap_hard()
        );

        server.getRedisService().start(RedisSubscriber.class);

        return server;
    }

    public TPAQueue getTPAQueue() {
        return this.tpaQueue;
    }
}
