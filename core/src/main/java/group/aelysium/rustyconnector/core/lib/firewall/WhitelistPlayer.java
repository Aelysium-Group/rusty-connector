package group.aelysium.rustyconnector.core.lib.firewall;

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

    public static boolean validate(Whitelist whitelist, WhitelistPlayer playerToValidate) {
        WhitelistPlayer player = whitelist.getPlayerManager().find(playerToValidate.getUsername());
        if(player == null) return false;

        if(player.getUUID() != null)
            if(!Objects.equals(player.getUUID().toString(), playerToValidate.getUUID().toString()))
                return false;

        if(player.getIP() != null)
            return Objects.equals(player.getIP(), playerToValidate.getIP());

        return true;
    }

    public String toString() {
        return "WhitelistPlayer: "+username+" "+uuid+" "+ip;
    }
}