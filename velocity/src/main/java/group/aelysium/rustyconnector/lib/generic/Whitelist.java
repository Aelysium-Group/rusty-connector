package group.aelysium.rustyconnector.lib.generic;

import com.velocitypowered.api.proxy.Player;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Whitelist {
    private String name;
    private List<WhitelistPlayer> players;

    public Whitelist(String name) {
        this.name = name;
    }

    public void addPlayer(WhitelistPlayer player) {
        this.players.add(player);
    }

    /**
     * Validate if a user is whitelisted or not
     * @param player The user to validate
     * @return `boolean` - `true` If the user is valid. `false` If the user is invalid. If a user has an IP Address defined. They must connect from that IP for their connection to be valid.
     */
    public boolean validate(Player player) {
        WhitelistPlayer whitelistPlayer = this.find(player.getUniqueId());

        if(whitelistPlayer == null) return false;

        if(!whitelistPlayer.hasIP()) return true;

        String ip = player.getRemoteAddress().getHostString();
        if(whitelistPlayer.getIP().equals(ip)) return true;

        return false;
    }


    public WhitelistPlayer find(UUID uuid) {
        Optional<WhitelistPlayer> response = players.stream().findFirst(player -> Objects.equals(player.getUUID(), uuid));
        return response.orElse(null);
    }
}
