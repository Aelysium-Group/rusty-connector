package group.aelysium.rustyconnector.plugin.velocity.lib.module;

import com.google.gson.Gson;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.managers.WhitelistPlayerManager;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.WhitelistConfig;

import java.io.File;
import java.util.*;

public class Whitelist {
    private String message = "You aren't whitelisted on this server!";
    private String name;
    private String permission;
    private final WhitelistPlayerManager whitelistPlayerManager;
    private final List<String> countries = new ArrayList<>();

    private boolean usePlayers = false;
    private boolean useCountries = false;
    private boolean usePermission = false;
    private boolean strict = false;

    public Whitelist(String name, boolean usePlayers, boolean usePermission, boolean useCountries, String message, boolean strict) {
        this.name = name;
        this.usePlayers = usePlayers;
        this.useCountries = useCountries;
        this.usePermission = usePermission;
        this.message = message;
        this.strict = strict;
        this.permission = Permission.constructNode("rustyconnector.whitelist.<whitelist name>",this.name);

        this.whitelistPlayerManager = new WhitelistPlayerManager();
    }

    public boolean usesPlayers() {
        return usePlayers;
    }

    public boolean usesCountries() {
        return useCountries;
    }

    public boolean usesPermission() {
        return usePermission;
    }
    public String getName() {
        return name;
    }
    public String getMessage() {
        return message;
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
    public boolean validate(Player player) {
        if(this.strict) {
            boolean playersValid = true;
            boolean countryValid = true;
            boolean permissionValid = true;


            if (this.usesPlayers())
                if (!WhitelistPlayer.validate(this, player))
                    playersValid = false;


            // if(this.usesCountries()) valid = this.validateCountry(ipAddress);


            if (this.usesPermission())
                if (!Permission.validate(player, this.permission))
                    permissionValid = false;


            return (playersValid && countryValid && permissionValid);
        } else {
            if (this.usesPlayers())
                if (WhitelistPlayer.validate(this, player))
                    return true;

            // if(this.usesCountries()) valid = this.validateCountry(ipAddress);

            if (this.usesPermission())
                if(Permission.validate(player, this.permission))
                    return true;

            return false;
        }
    }

    public boolean validateCountry(String ipAddress) {
        return this.countries.contains(ipAddress);
    }


    /**
     * Initializes a whitelist based on a config.
     * @return A whitelist.
     */
    public static Whitelist init(String whitelistName) {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        WhitelistConfig whitelistConfig = WhitelistConfig.newConfig(
                whitelistName,
                new File(plugin.getDataFolder(), "whitelists/"+whitelistName+".yml"),
                "velocity_whitelist_template.yml"
        );
        if(!whitelistConfig.generate()) {
            throw new IllegalStateException("Unable to load or create whitelists/"+whitelistName+".yml!");
        }
        whitelistConfig.register();

        Whitelist whitelist = new Whitelist(
                whitelistName,
                whitelistConfig.getUse_players(),
                whitelistConfig.getUse_permission(),
                whitelistConfig.getUse_country(),
                whitelistConfig.getMessage(),
                whitelistConfig.isStrict()
        );
        if(whitelistConfig.getUse_players()) {
            List<Object> players = whitelistConfig.getPlayers();
            Gson gson = new Gson();
            players.forEach(entry -> {
                String json = gson.toJson(entry);
                WhitelistPlayer player = gson.fromJson(json, WhitelistPlayer.class);

                whitelist.getPlayerManager().add(player);
            });
        }
        if(whitelistConfig.getUse_country()) {
            List<String> countries = whitelistConfig.getCountries();
            countries.forEach(whitelist::registerCountry);
        };

        plugin.logger().log("Loaded whitelist: "+whitelistName);
        return whitelist;
    }
}
