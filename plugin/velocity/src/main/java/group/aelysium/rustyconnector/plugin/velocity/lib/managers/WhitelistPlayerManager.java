package group.aelysium.rustyconnector.plugin.velocity.lib.managers;

import group.aelysium.rustyconnector.core.lib.model.NodeManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.WhitelistPlayerFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WhitelistPlayerManager implements NodeManager<WhitelistPlayerFilter> {
    private final Map<String, WhitelistPlayerFilter> registeredPlayers = new HashMap<>();

    /**
     * Get a player via their username.
     * @param username The username of the player to get.
     * @return A player.
     */
    @Override
    public WhitelistPlayerFilter find(String username) { return this.registeredPlayers.get(username); }

    /**
     * Add a player to this manager.
     * @param player The player to add to this manager.
     */
    @Override
    public void add(WhitelistPlayerFilter player) {
        this.registeredPlayers.put(player.username(),player);
    }

    /**
     * Remove a player from this manager.
     * @param player The player to remove from this manager.
     */
    @Override
    public void remove(WhitelistPlayerFilter player) {
        this.remove(player.username());
    }

    @Override
    public List<WhitelistPlayerFilter> dump() {
        return this.registeredPlayers.values().stream().toList();
    }

    /**
     * Remove a player from this manager.
     * @param username The username of the player to remove from this manager.
     */
    public void remove(String username) {
        this.registeredPlayers.remove(username);
    }

    @Override
    public void clear() {
        this.registeredPlayers.clear();
    }
}
