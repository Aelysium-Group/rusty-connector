package group.aelysium.rustyconnector.core.lib.managers;

import group.aelysium.rustyconnector.core.lib.firewall.WhitelistPlayer;
import group.aelysium.rustyconnector.core.lib.model.NodeManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WhitelistPlayerManager implements NodeManager<WhitelistPlayer> {
    private Map<String, WhitelistPlayer> registeredPlayers = new HashMap<>();

    /**
     * Get a player via their username.
     * @param username The username of the player to get.
     * @return A player.
     */
    @Override
    public WhitelistPlayer find(String username) { return this.registeredPlayers.get(username); }

    /**
     * Add a player to this manager.
     * @param player The player to add to this manager.
     */
    @Override
    public void add(WhitelistPlayer player) {
        this.registeredPlayers.put(player.getUsername(),player);
    }

    /**
     * Remove a player from this manager.
     * @param player The player to remove from this manager.
     */
    @Override
    public void remove(WhitelistPlayer player) {
        this.remove(player.getUsername());
    }

    @Override
    public List<WhitelistPlayer> dump() {
        return this.registeredPlayers.values().stream().toList();
    }

    /**
     * Remove a player from this manager.
     * @param username The username of the player to remove from this manager.
     */
    public void remove(String username) {
        this.registeredPlayers.remove(username);
    }
}
