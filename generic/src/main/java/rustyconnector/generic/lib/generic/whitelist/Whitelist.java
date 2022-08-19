package rustyconnector.generic.lib.generic;

import java.util.*;

public class Whitelist {
    private String name;
    private final List<WhitelistPlayer> players = new ArrayList<>();

    public Whitelist(String name) {
        this.name = name;
    }

    public void addPlayer(WhitelistPlayer player) {
        this.players.add(player);
    }

    /**
     * Validate if a user is whitelisted or not
     * @param uuid The uuid of the user to validate
     * @param ip The IP address of the user to validate
     * @return `boolean` - `true` If the user is valid. `false` If the user is invalid. If a user has an IP Address defined. They must connect from that IP for their connection to be valid.
     */
    public boolean validate(UUID uuid, String ip) {
        WhitelistPlayer whitelistPlayer = this.find(uuid);

        if(whitelistPlayer == null) return false;

        if(!whitelistPlayer.hasIP()) return true;

        if(whitelistPlayer.getIP().equals(ip)) return true;

        return false;
    }


    public WhitelistPlayer find(UUID uuid) {
        Optional<WhitelistPlayer> response = players.stream().filter(player -> Objects.equals(player.getUUID(), uuid)).findFirst();
        return response.orElse(null);
    }
}
