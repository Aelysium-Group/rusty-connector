package group.aelysium.rustyconnector.plugin.velocity.lib.managers;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.model.NodeManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// This is a node manager of Players. The Players are of type: Velocity Player.
public class PlayerManager implements NodeManager<Player> {
    private final Map<UUID, Player> registeredPlayers = new HashMap<>();

    /**
     * Get a player via their username.
     * @param username The username of the player to get.
     * @return A player.
     */
    @Override
    @Deprecated
    public Player find(String username) { return null; }

    /**
     * Get a player via their uuid.
     * @param uuid A uuid to look for.
     * @return A player.
     */
    public Player find(UUID uuid) {
        return this.registeredPlayers.get(uuid);
    }

    /**
     * Add a player to this manager.
     * @param player The player to add to this manager.
     */
    @Override
    public void add(Player player) {
        this.registeredPlayers.put(player.getUniqueId(),player);
    }

    /**
     * Remove a player from this manager.
     * @param player The player to remove from this manager.
     */
    @Override
    public void remove(Player player) {
        this.remove(player.getUniqueId());
    }

    @Override
    public List<Player> dump() {
        return this.registeredPlayers.values().stream().toList();
    }

    /**
     * Remove a player from this manager.
     * @param uuid The uuid of the player to remove from this manager.
     */
    public void remove(UUID uuid) {
        this.registeredPlayers.remove(uuid);
    }

    @Override
    public void clear() {
        this.registeredPlayers.clear();
    }
}
