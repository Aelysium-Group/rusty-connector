package group.aelysium.rustyconnector.plugin.velocity.lib.module;

import com.velocitypowered.api.proxy.Player;

import java.util.Objects;
import java.util.UUID;

public class WhitelistPlayer {
    private UUID uuid = null;
    private String username = null;
    private String ip = null;

    public UUID getUUID() {
        return this.uuid;
    }

    public String getUsername() {
        return this.username;
    }

    public String getIP() {
        return this.ip;
    }

    public WhitelistPlayer(String username, UUID uuid, String ip) {
        this.username = username;
        this.uuid = uuid;
        this.ip = ip;
    }

    public static boolean validate(Whitelist whitelist, Player playerToValidate) {
        WhitelistPlayer player = whitelist.getPlayerManager().find(playerToValidate.getUsername());
        if(player == null) return false;

        if(player.getUUID() != null)
            if(!Objects.equals(player.getUUID().toString(), playerToValidate.getUniqueId().toString()))
                return false;

        if(player.getIP() != null)
            return Objects.equals(player.getIP(), playerToValidate.getRemoteAddress().getHostString());

        return true;
    }

    public String toString() {
        return "WhitelistPlayer: "+username+" "+uuid+" "+ip;
    }
}