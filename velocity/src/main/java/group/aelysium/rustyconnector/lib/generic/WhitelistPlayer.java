package group.aelysium.rustyconnector.lib.generic;

import java.util.*;

public class WhitelistPlayer {
    private static List<WhitelistPlayer> whitelistPlayers = new ArrayList<>();

    private UUID uuid;
    private String username;
    private String ip_address = null;

    public UUID getUUID() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getIP() {
        return ip_address;
    }

    public WhitelistPlayer(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }
    public WhitelistPlayer(UUID uuid, String username,String ip_address) {
        this.uuid = uuid;
        this.username = username;
        this.ip_address = ip_address;
    }

    public boolean hasIP() {
        return this.ip_address != null;
    }

    public static WhitelistPlayer find(UUID uuid) {
        Optional<WhitelistPlayer> response = whitelistPlayers.stream().filter(player -> Objects.equals(player.uuid, uuid)).findFirst();
        return response.orElse(null);
    }
}
