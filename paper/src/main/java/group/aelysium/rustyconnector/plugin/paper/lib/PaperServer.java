package group.aelysium.rustyconnector.plugin.paper.lib;

import group.aelysium.rustyconnector.core.lib.message.RedisMessage;
import group.aelysium.rustyconnector.core.lib.message.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.hash.MD5;
import group.aelysium.rustyconnector.core.lib.message.cache.MessageCache;
import group.aelysium.rustyconnector.core.lib.model.Server;
import group.aelysium.rustyconnector.core.lib.firewall.Whitelist;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.lib.Permission;
import group.aelysium.rustyconnector.plugin.paper.lib.database.Redis;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;

public class PaperServer implements Server {
    private Redis redis;
    private String family;
    private int playerCount = 0;
    private int priorityIndex = 0;
    private int softPlayerCap = 20;
    private int hardPlayerCap = 30;
    private final String name;

    private final String privateKey;
    private final InetSocketAddress address;
    private Whitelist whitelist = null;

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
    public int getPriorityIndex() {
        return this.priorityIndex;
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

    public boolean hasWhitelist() {
        if(this.whitelist == null) return false;
        return true;
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

    public void registerWhitelist(Whitelist whitelist) {
        this.whitelist = whitelist;
    }
    public Whitelist getWhitelist() { return this.whitelist; }

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
        registrationMessage.addParameter("player-count", String.valueOf(this.playerCount));
        registrationMessage.addParameter("priority", String.valueOf(this.priorityIndex));

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
        registrationMessage.addParameter("player-count", String.valueOf(this.playerCount));

        registrationMessage.dispatchMessage(this.redis);
    }
}
