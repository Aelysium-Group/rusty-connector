package group.aelysium.rustyconnector.plugin.velocity.lib.whitelist;

import com.google.gson.Gson;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.whitelist.IWhitelist;
import group.aelysium.rustyconnector.core.lib.Callable;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.configs.WhitelistConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.Permission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.IOException;
import java.util.*;

public class Whitelist implements IWhitelist {
    private final String message;
    private final String name;
    private final String permission;
    private final List<WhitelistPlayerFilter> playerFilters = new ArrayList<>();

    private final boolean usePlayers;
    private final boolean usePermission;
    private final boolean strict;
    private final boolean inverted;

    public Whitelist(String name, boolean usePlayers, boolean usePermission, String message, boolean strict, boolean inverted) {
        this.name = name;
        this.usePlayers = usePlayers;
        this.usePermission = usePermission;
        this.message = message;
        this.strict = strict;
        this.inverted = inverted;
        this.permission = Permission.constructNode("rustyconnector.whitelist.<whitelist id>",this.name);
    }

    public boolean usesPlayers() {
        return usePlayers;
    }
    public boolean usesPermission() {
        return usePermission;
    }
    public String name() {
        return name;
    }
    public String message() {
        return message;
    }
    public boolean inverted() {
        return this.inverted;
    }

    public List<WhitelistPlayerFilter> playerFilters() {
        return this.playerFilters;
    }

    /**
     * Validate a player against the whitelist.
     * @param player The player to validate.
     * @return `true` if the player is whitelisted. `false` otherwise.
     */
    public boolean validate(IPlayer player) {
        Callable<Boolean> validate = () -> {
            if (Whitelist.this.strict)
                return validateStrict(player);
            else
                return validateSoft(player);
        };

        if(this.inverted)
            return !validate.execute();
        else
            return validate.execute();
    }

    private boolean validateStrict(IPlayer player) {
        boolean playersValid = true;
        boolean countryValid = true;
        boolean permissionValid = true;


        if (this.usesPlayers())
            if (!WhitelistPlayerFilter.validate(this, player))
                playersValid = false;

        if (this.usesPermission())
            if (!Permission.validate(player.resolve().orElse(null), this.permission))
                permissionValid = false;


        return (playersValid && countryValid && permissionValid);
    }

    private boolean validateSoft(IPlayer player) {
        if (this.usesPlayers())
            if (WhitelistPlayerFilter.validate(this, player))
                return true;

        if (this.usesPermission())
            return Permission.validate(player.resolve().orElse(null), this.permission);

        return false;
    }

    /**
     * Initializes a whitelist based on a config.
     * @return A whitelist.
     */
    public static Reference init(DependencyInjector.DI4<List<Component>, LangService, WhitelistService, ConfigService> deps, String whitelistName) throws IOException {
        Tinder api = Tinder.get();
        List<Component> bootOutput = deps.d1();

        bootOutput.add(Component.text(" | Registering whitelist "+whitelistName+"...", NamedTextColor.DARK_GRAY));

        WhitelistConfig whitelistConfig = WhitelistConfig.construct(api.dataFolder(), whitelistName, deps.d2(), deps.d4());

        Whitelist whitelist = new Whitelist(
                whitelistName,
                whitelistConfig.getUse_players(),
                whitelistConfig.getUse_permission(),
                whitelistConfig.getMessage(),
                whitelistConfig.isStrict(),
                whitelistConfig.isInverted()
        );
        if(whitelistConfig.getUse_players()) {
            List<Object> players = whitelistConfig.getPlayers();
            Gson gson = new Gson();
            players.forEach(entry -> {
                String json = gson.toJson(entry);
                WhitelistPlayerFilter player = gson.fromJson(json, WhitelistPlayerFilter.class);

                whitelist.playerFilters().add(player);
            });
        }

        bootOutput.add(Component.text(" | Registered whitelist: "+whitelistName, NamedTextColor.YELLOW));

        deps.d3().add(whitelist);
        return new Whitelist.Reference(whitelistName);
    }
}
