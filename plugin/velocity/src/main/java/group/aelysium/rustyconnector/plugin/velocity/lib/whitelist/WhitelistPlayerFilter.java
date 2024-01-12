package group.aelysium.rustyconnector.plugin.velocity.lib.whitelist;

import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.whitelist.IWhitelistPlayerFilter;

import java.util.Objects;
import java.util.UUID;

public class WhitelistPlayerFilter implements IWhitelistPlayerFilter {
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

    public WhitelistPlayerFilter(String username, UUID uuid, String ip) {
        this.username = username;
        this.uuid = uuid;
        this.ip = ip;
    }

    public static boolean validate(Whitelist whitelist, IPlayer playerToValidate) {
        WhitelistPlayerFilter player = whitelist.playerFilters().stream()
                .filter(whitelistPlayerFilter -> whitelistPlayerFilter.username().equals(playerToValidate.username()))
                .findAny().orElse(null);
        if(player == null) return false;

        if(player.uuid() != null)
            if(!Objects.equals(player.uuid().toString(), playerToValidate.uuid().toString()))
                return false;

        if(player.ip() != null) {
            try {
                return Objects.equals(player.ip(), playerToValidate.resolve().orElseThrow().getRemoteAddress().getHostString());
            } catch (Exception ignore) {
                return false;
            }
        }

        return true;
    }

    public String toString() {
        return "WhitelistPlayer: "+username+" "+uuid+" "+ip;
    }
}