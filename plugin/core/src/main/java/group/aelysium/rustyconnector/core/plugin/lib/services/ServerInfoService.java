package group.aelysium.rustyconnector.core.plugin.lib.services;

import group.aelysium.rustyconnector.core.central.PluginLogger;
import group.aelysium.rustyconnector.core.central.Tinder;
import group.aelysium.rustyconnector.core.lib.hash.MD5;
import group.aelysium.rustyconnector.core.lib.model.PlayerServer;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import group.aelysium.rustyconnector.core.plugin.Plugin;

import java.net.InetSocketAddress;

public class ServerInfoService extends Service implements PlayerServer {
    private String name;
    private InetSocketAddress address;
    private String family;
    private Integer softPlayerCap;
    private Integer hardPlayerCap;
    private Integer weight;

    public ServerInfoService(String name, InetSocketAddress address, String family, int softPlayerCap, int hardPlayerCap, int weight) {
        if(name.equals(""))
            name = family + "-" + MD5.generateMD5(); // Generate a custom string to be the server's name
        this.name = name;

        this.address = address;
        this.family = family;

        this.setPlayerCap(softPlayerCap, hardPlayerCap);

        this.weight = weight;
    }

    /**
     * Set the player cap for this server. If soft cap is larger than hard cap. Set soft cap to be the same value as hard cap.
     * @param softPlayerCap The soft player cap
     * @param hardPlayerCap The hard player cap
     */
    private void setPlayerCap(int softPlayerCap, int hardPlayerCap) {
        Tinder api = Plugin.getAPI();
        PluginLogger logger = api.logger();

        api.setMaxPlayers(hardPlayerCap);

        if(softPlayerCap >= hardPlayerCap) {
            this.hardPlayerCap = hardPlayerCap;
            this.softPlayerCap = hardPlayerCap;
            logger.log("soft-player-cap was set to be larger than hard-player-cap. Running in `player-limit` mode.");
            return;
        }
        this.hardPlayerCap = hardPlayerCap;
        this.softPlayerCap = softPlayerCap;
    }

    public String address() {
        return AddressUtil.addressToString(this.address);
    }

    public String name() {
        return this.name;
    }

    public String family() { return this.family; }

    @Override
    public int playerCount() {
        return Plugin.getAPI().onlinePlayerCount();
    }

    @Override
    public int sortIndex() {
        return 0;
    }

    @Override
    public int weight() {
        return this.weight;
    }

    @Override
    public int softPlayerCap() {
        return this.softPlayerCap;
    }

    @Override
    public int hardPlayerCap() {
        return this.hardPlayerCap;
    }

    @Override
    public void kill() {
        name = null;
        address = null;
        family = null;
        softPlayerCap = null;
        hardPlayerCap = null;
        weight = null;
    }
}
