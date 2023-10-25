package group.aelysium.rustyconnector.core.plugin.lib.server_info;

import group.aelysium.rustyconnector.api.core.logger.PluginLogger;
import group.aelysium.rustyconnector.api.mc_loader.central.MCLoaderTinder;
import group.aelysium.rustyconnector.api.mc_loader.server_info.IServerInfoService;
import group.aelysium.rustyconnector.core.lib.crypt.MD5;
import group.aelysium.rustyconnector.api.velocity.util.AddressUtil;
import group.aelysium.rustyconnector.core.TinderAdapterForCore;

import java.net.InetSocketAddress;

public class ServerInfoService implements IServerInfoService {
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
        MCLoaderTinder api = TinderAdapterForCore.getTinder();
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

    public int playerCount() {
        return TinderAdapterForCore.getTinder().onlinePlayerCount();
    }

    public int weight() {
        return this.weight;
    }

    public int softPlayerCap() {
        return this.softPlayerCap;
    }

    public int hardPlayerCap() {
        return this.hardPlayerCap;
    }

    public void kill() {
        name = null;
        address = null;
        family = null;
        softPlayerCap = null;
        hardPlayerCap = null;
        weight = null;
    }
}
