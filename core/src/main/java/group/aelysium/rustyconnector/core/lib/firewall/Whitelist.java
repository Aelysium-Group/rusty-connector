package group.aelysium.rustyconnector.core.lib.firewall;

import group.aelysium.rustyconnector.core.lib.managers.WhitelistPlayerManager;

import java.util.*;

public class Whitelist {
    public String getName() {
        return name;
    }

    private String name;
    private final WhitelistPlayerManager whitelistPlayerManager;
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

        this.whitelistPlayerManager = new WhitelistPlayerManager();
    }

    public WhitelistPlayerManager getPlayerManager() {
        return this.whitelistPlayerManager;
    }
    public void registerCountry(String country) {
        this.countries.add(country);
    }

    /**
     * Validate a player against the whitelist.
     * @param player The player to validate.
     * @return `true` if the player is whitelisted. `false` otherwise.
     */
    public boolean validate(WhitelistPlayer player) {
        boolean valid = false;
        if(this.usesPlayers()) valid = WhitelistPlayer.validate(this, player);
        // if(this.usesCountries()) valid = this.validateCountry(ipAddress);

        // TODO Add permission handling
        if(this.usesPermission()) valid = false;
        return valid;
    }

    public boolean validateCountry(String ipAddress) {
        return this.countries.contains(ipAddress);
    }

}
