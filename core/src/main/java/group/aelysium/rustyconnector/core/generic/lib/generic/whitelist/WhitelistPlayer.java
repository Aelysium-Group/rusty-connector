package group.aelysium.rustyconnector.core.generic.lib.generic.whitelist;

import java.util.Objects;
import java.util.UUID;

public class WhitelistPlayer {
    private UUID uuid = null;
    private String username = null;
    private String ip_address = null;

    public UUID getUUID() {
        return this.uuid;
    }

    public String getUsername() {
        return this.username;
    }

    public String getIP() {
        return this.ip_address;
    }

    public WhitelistPlayer(String username, UUID uuid, String ip_address) {
        this.username = username;
        this.uuid = uuid;
        this.ip_address = ip_address;
    }

    public static boolean validate(Whitelist whitelist, WhitelistPlayer playerToValidate) {
        WhitelistPlayer player = whitelist.findPlayer(playerToValidate.getUsername());
        if(player == null) return false;

        if(player.getUUID() != null)
            if(player.getUUID() != playerToValidate.getUUID())
                return false;

        if(player.getIP() != null)
            return Objects.equals(player.getIP(), playerToValidate.getIP());

        return true;
    }
}