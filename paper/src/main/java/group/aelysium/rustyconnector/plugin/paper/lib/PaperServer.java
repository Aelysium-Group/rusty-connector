package group.aelysium.rustyconnector.plugin.paper.lib;

import group.aelysium.rustyconnector.core.lib.message.RedisMessage;
import group.aelysium.rustyconnector.core.lib.message.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.hash.MD5;
import group.aelysium.rustyconnector.core.lib.message.cache.MessageCache;
import group.aelysium.rustyconnector.core.lib.model.Server;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.lib.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.paper.lib.database.Redis;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;

public class PaperServer implements Server {
    private Redis redis;
    private String family;
    private int playerCount = 0;
    private int weight = 0;
    private int softPlayerCap = 20;
    private int hardPlayerCap = 30;
    private final String name;

    private final String privateKey;
    private final InetSocketAddress address;

    public PaperServer(String name, String privateKey, String address, String family, Redis redis) {
        if(name.equals("")) {
            name = MD5.generatePrivateKey(); // Generate a custom string to be the server's name
        }
        this.name = name;
        this.privateKey = privateKey;

        String[] addressSplit = address.split(":");

        this.address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));

        this.family = family;

        this.redis = redis;
    }

    public boolean validatePrivateKey(String keyToValidate) {
        return this.privateKey.equals(keyToValidate);
    }

    public MessageCache getMessageCache() {
        return this.redis.getMessageCache();
    }

    public InetSocketAddress getAddress() { return this.address; }

    @Override
    public int getPlayerCount() {
        return playerCount;
    }

    @Override
    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
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

    /**
     * Set the player cap for this server. If soft cap is larger than hard cap. Set soft cap to be the same value as hard cap.
     * @param softPlayerCap The soft player cap
     * @param hardPlayerCap The hard player cap
     */
    public void setPlayerCap(int softPlayerCap, int hardPlayerCap) {

        PaperRustyConnector.getInstance().getServer().setMaxPlayers(hardPlayerCap);

        if(softPlayerCap >= hardPlayerCap) {
            this.hardPlayerCap = hardPlayerCap;
            this.softPlayerCap = hardPlayerCap;
            PaperRustyConnector.getInstance().logger().log("soft-player-cap was set to be larger than hard-player-cap. Running in `player-limit` mode.");
            return;
        }
        this.hardPlayerCap = hardPlayerCap;
        this.softPlayerCap = softPlayerCap;
    }

    /**
     * Validates the player against the server's current player count.
     * If the server is full or the player doesn't have permissions to bypass soft and hard player caps. They will be kicked
     * @param player The player to validate
     * @return `true` if the player is able to join. `false` otherwise.
     */
    public boolean validatePlayer(Player player) {
        if(this.playerCount != this.softPlayerCap) return true; // If the player count is NOT at soft-player-cap

        if(!Permission.validate(
                player,
                "rustyconnector.softCapBypass",
                Permission.constructNode("rustyconnector.<server name>.softCapBypass",this.name),
                Permission.constructNode("rustyconnector.<family name>.<server name>.softCapBypass", this.family, this.name)
                )) return false; // If soft-player-cap has been reached and the player doesn't have permission to bypass

        if(this.playerCount != this.hardPlayerCap) return true; // If the player count is NOT at hard-player-cap

        if(Permission.validate(
                player,
                "rustyconnector.admin.hardCapBypass"
        )) return true; // If hard-player-cap has been reached and the player has permission to bypass, let them in.

        return false; // If something somehow breaks with the previous checks. The method should fail to closed.
    }

    public void registerToProxy() {
        RedisMessage registrationMessage = new RedisMessage(
                this.privateKey,
                RedisMessageType.REG,
                this.address.getHostName()+":"+this.address.getPort(),
                false
        );
        registrationMessage.addParameter("family",this.family);
        registrationMessage.addParameter("name",this.name);
        registrationMessage.addParameter("soft-cap", String.valueOf(this.softPlayerCap));
        registrationMessage.addParameter("hard-cap", String.valueOf(this.hardPlayerCap));
        registrationMessage.addParameter("weight", String.valueOf(this.weight));

        registrationMessage.dispatchMessage(this.redis);
    }

    public void unregisterFromProxy() {
        RedisMessage registrationMessage = new RedisMessage(
                this.privateKey,
                RedisMessageType.UNREG,
                this.address.getHostName()+":"+this.address.getPort(),
                false
        );
        registrationMessage.addParameter("family",this.family);
        registrationMessage.addParameter("name",this.name);

        registrationMessage.dispatchMessage(this.redis);
    }

    /**
     * Requests that the proxy moves this player to another server.
     * @param player The player to send.
     * @param familyName The name of the family to send to.
     */
    public void sendToOtherFamily(Player player, String familyName) {
        RedisMessage registrationMessage = new RedisMessage(
                this.privateKey,
                RedisMessageType.SEND,
                this.address.getHostName()+":"+this.address.getPort(),
                false
        );
        registrationMessage.addParameter("uuid",player.getUniqueId().toString());
        registrationMessage.addParameter("family",familyName);

        registrationMessage.dispatchMessage(this.redis);
    }

    /**
     * Sends a pong message to the proxy.
     */
    public void pong() {
        RedisMessage registrationMessage = new RedisMessage(
                this.privateKey,
                RedisMessageType.PONG,
                this.address.getHostName()+":"+this.address.getPort(),
                false
        );
        registrationMessage.addParameter("name", this.name);

        registrationMessage.dispatchMessage(this.redis);
    }

    public static PaperServer init(DefaultConfig config) throws IllegalAccessException {
        PaperRustyConnector plugin = PaperRustyConnector.getInstance();

        if((config.getPrivate_key().equals(""))) throw new IllegalArgumentException("You must define a `private-key` for this server! Copy the private key from your proxy and paste it in `config.yml`!");

        if((config.getServer_address().equals(""))) throw new IllegalArgumentException("You must define an `address`. Copy the IP Address associated with this server and paste it into the `address` field in your `config.yml`");
        if((config.getServer_name().equals(""))) throw new IllegalArgumentException("You must define a `name` for this server!");

        plugin.logger().log("---------| Preparing Redis...");
        Redis redis = new Redis();

        redis.setConnection(
                config.getRedis_host(),
                config.getRedis_port(),
                config.getRedis_password(),
                config.getRedis_dataChannel()
        );
        redis.connect(plugin);

        PaperServer server = new PaperServer(
                config.getServer_name(),
                config.getPrivate_key(),
                config.getServer_address(),
                config.getServer_family(),
                redis
        );

        server.setPlayerCap(
                config.getServer_playerCap_soft(),
                config.getServer_playerCap_hard()
        );

        return server;
    }
}
