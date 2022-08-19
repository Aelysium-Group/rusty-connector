package rustyconnector.generic.lib.generic.whitelist;

import com.axlabs.ip2asn2cc.Ip2Asn2Cc;

import java.util.*;

public class Whitelist {
    private String name;
    private final List<Player> players = new ArrayList<>();
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

    public void registerPlayer(Player player) {
        this.players.add(player);
    }
    public void registerCountry(String country) {
        this.countries.add(country);
    }

    public boolean validate(String username, UUID uuid, String ipAddress) {
        boolean valid = false;
        if(this.usesPlayers()) valid = Player.validate(this, username, uuid, ipAddress);
        // if(this.usesCountries()) valid = this.validateCountry(ipAddress);

        // TODO Add permission handling
        if(this.usesPermission()) valid = ;
        return valid;
    }

    public Player findPlayer(String username) {
        Optional<Player> response = this.players.stream().filter(player -> Objects.equals(player.username, username)).findFirst();
        return response.orElse(null);
    }
    public boolean validateCountry(String ipAddress) {
        return this.countries.contains(ipAddress);
    }

    public class Player {
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

        public Player(String username, UUID uuid, String ip_address) {
            this.username = username;
            this.uuid = uuid;
            this.ip_address = ip_address;
        }

        public static boolean validate(Whitelist whitelist, String username, UUID uuid, String ipAddress) {
            Player player = whitelist.findPlayer(username);
            if(player == null) return false;

            if(player.getUUID() != null)
                if(player.getUUID() != uuid)
                    return false;

            if(player.getIP() != null)
                if(player.getIP() != ipAddress)
                    return false;

            return true;
        }
    }
}
