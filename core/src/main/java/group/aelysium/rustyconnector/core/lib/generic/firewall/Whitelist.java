package group.aelysium.rustyconnector.core.lib.generic.firewall;

import java.util.*;

public class Whitelist {
    private String name;
    private final List<WhitelistPlayer> players = new ArrayList<>();
    private final List<String> countries = new ArrayList<>();

    public boolean usesPlayers() {
        return usePlayers;
    }

    public boolean usesCountries() {
        return useCountries;
    }

    public boolean usesPermission() {
        return usePermission;
    }

    private boolean usePlayers = false;
    private boolean useCountries = false;
    private boolean usePermission = false;

    public Whitelist(String name, boolean usePlayers, boolean usePermission, boolean useCountries) {
        this.name = name;
        this.usePlayers = usePlayers;
        this.useCountries = useCountries;
        this.usePermission = usePermission;
    }

    public void registerPlayer(WhitelistPlayer player) {
        this.players.add(player);
    }
    public void registerCountry(String country) {
        this.countries.add(country);
    }

    public boolean validate(WhitelistPlayer player) {
        boolean valid = false;
        if(this.usesPlayers()) valid = WhitelistPlayer.validate(this, player);
        // if(this.usesCountries()) valid = this.validateCountry(ipAddress);

        // TODO Add permission handling
        if(this.usesPermission()) valid = false;
        return valid;
    }

    public WhitelistPlayer findPlayer(String username) {
        Optional<WhitelistPlayer> response = this.players.stream().filter(player -> Objects.equals(player.getUsername(), username)).findFirst();
        return response.orElse(null);
    }
    public boolean validateCountry(String ipAddress) {
        return this.countries.contains(ipAddress);
    }

}
