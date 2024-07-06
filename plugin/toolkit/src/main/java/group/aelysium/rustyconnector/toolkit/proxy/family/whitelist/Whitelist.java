package group.aelysium.rustyconnector.toolkit.proxy.family.whitelist;

import group.aelysium.rustyconnector.toolkit.RC;
import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.proxy.Permission;
import group.aelysium.rustyconnector.toolkit.proxy.player.IPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;

public class Whitelist implements Particle {
    private final String message;
    private final String name;
    private final String permission;
    private final List<Filter> playerFilters = new ArrayList<>();

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

    /**
     * Fetches a list of player filters.
     * @return {@link List<Filter>}
     */
    public List<Filter> playerFilters() {
        return this.playerFilters;
    }

    /**
     * Validate a player against the {@link Whitelist}.
     * @param player The {@link IPlayer} to validate.
     * @return `true` if the player is whitelisted. `false` otherwise.
     */
    public boolean validate(IPlayer player) {
        Callable<Boolean> validate = () -> {
            if (Whitelist.this.strict)
                return validateStrict(player);
            else
                return validateSoft(player);
        };

        try {
            if (this.inverted)
                return !validate.call();
            else
                return validate.call();
        } catch (Exception ignore) {}
        return false;
    }

    protected boolean validateStrict(IPlayer player) {
        boolean playersValid = true;
        boolean countryValid = true;
        boolean permissionValid = true;


        if (this.usesPlayers())
            if (!Filter.validate(this, player))
                playersValid = false;

        if (this.usesPermission())
            if (!Permission.validate(player, this.permission))
                permissionValid = false;


        return (playersValid && countryValid && permissionValid);
    }

    protected boolean validateSoft(IPlayer player) {
        if (this.usesPlayers())
            if (Filter.validate(this, player))
                return true;

        if (this.usesPermission())
            return Permission.validate(player, this.permission);

        return false;
    }

    @Override
    public void close() throws Exception {
        this.playerFilters.clear();
    }

    public record Settings(
            String name,
            boolean usePlayers,
            boolean usePermission,
            String message,
            boolean strict,
            boolean inverted
    ) {}

    public static class Tinder extends Particle.Tinder<Whitelist> {
        private final Settings settings;

        public Tinder(Settings settings) {
            this.settings = settings;
        }

        @Override
        public @NotNull Whitelist ignite() throws Exception {
            return new Whitelist(
                    this.settings.name(),
                    this.settings.usePlayers(),
                    this.settings.usePermission(),
                    this.settings.message(),
                    this.settings.strict(),
                    this.settings.inverted()
            );
        }
    }

    public static class Filter {
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

        public Filter(String username, UUID uuid, String ip) {
            this.username = username;
            this.uuid = uuid;
            this.ip = ip;
        }

        public static boolean validate(Whitelist whitelist, IPlayer playerToValidate) {
            Filter player = whitelist.playerFilters().stream()
                    .filter(whitelistPlayerFilter -> whitelistPlayerFilter.username().equals(playerToValidate.username()))
                    .findAny().orElse(null);
            if(player == null) return false;

            if(player.uuid() != null)
                if(!Objects.equals(player.uuid().toString(), playerToValidate.uuid().toString()))
                    return false;

            if(player.ip() != null) {
                try {
                    return Objects.equals(player.ip(), RC.P.Adapter().extractHostname(playerToValidate));
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
}
