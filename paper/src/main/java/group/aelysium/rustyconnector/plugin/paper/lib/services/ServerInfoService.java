package group.aelysium.rustyconnector.plugin.paper.lib.services;

import group.aelysium.rustyconnector.core.lib.hash.MD5;
import group.aelysium.rustyconnector.core.lib.model.PlayerServer;
import group.aelysium.rustyconnector.core.lib.model.Service;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;

import java.net.InetSocketAddress;

public class ServerInfoService extends Service implements PlayerServer {
    private String name;
    private InetSocketAddress address;
    private String family;
    private Integer softPlayerCap;
    private Integer hardPlayerCap;
    private Integer weight;

    public ServerInfoService(String name, InetSocketAddress address, String family, int softPlayerCap, int hardPlayerCap, int weight) {
        super(true);

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
        PaperAPI api = PaperRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        api.getServer().setMaxPlayers(hardPlayerCap);

        if(softPlayerCap >= hardPlayerCap) {
            this.hardPlayerCap = hardPlayerCap;
            this.softPlayerCap = hardPlayerCap;
            logger.log("soft-player-cap was set to be larger than hard-player-cap. Running in `player-limit` mode.");
            return;
        }
        this.hardPlayerCap = hardPlayerCap;
        this.softPlayerCap = softPlayerCap;
    }

    public String getAddress() {
        return this.address.getHostName() + ":" + this.address.getPort();
    }

    public String getName() {
        return this.name;
    }

    public String getFamily() { return this.family; }

    @Override
    public int getPlayerCount() {
        return PaperRustyConnector.getAPI().getServer().getOnlinePlayers().size();
    }

    @Override
    public int getSortIndex() {
        return 0;
    }

    @Override
    public int getWeight() {
        return this.weight;
    }

    @Override
    public int getSoftPlayerCap() {
        return this.softPlayerCap;
    }

    @Override
    public int getHardPlayerCap() {
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
