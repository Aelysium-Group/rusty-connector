package group.aelysium.rustyconnector.plugin.velocity.lib.whitelist;

import com.velocitypowered.api.proxy.Player;

import java.util.Objects;
import java.util.UUID;

public class WhitelistPlayer {
    private UUID uuid = null;
    private String username = null;
    private String ip = null;

    public UUID uuid() {
        return this.uuid;
    }

    public String username() {
        return this.username;
    }

    public String ip() {
        return this.ip;
    }

    public WhitelistPlayer(String username, UUID uuid, String ip) {
        this.username = username;
        this.uuid = uuid;
        this.ip = ip;
    }

    public static boolean validate(Whitelist whitelist, Player playerToValidate) {
        WhitelistPlayer player = whitelist.playerManager().find(playerToValidate.getUsername());
        if(player == null) return false;

        if(player.uuid() != null)
            if(!Objects.equals(player.uuid().toString(), playerToValidate.getUniqueId().toString()))
                return false;

        if(player.ip() != null)
            return Objects.equals(player.ip(), playerToValidate.getRemoteAddress().getHostString());

        return true;
    }

    public String toString() {
        return "WhitelistPlayer: "+username+" "+uuid+" "+ip;
    }
}