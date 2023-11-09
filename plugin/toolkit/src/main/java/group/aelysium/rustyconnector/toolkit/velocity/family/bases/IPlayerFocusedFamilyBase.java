package group.aelysium.rustyconnector.toolkit.velocity.family.bases;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.toolkit.velocity.server.IPlayerServer;
import group.aelysium.rustyconnector.toolkit.velocity.whitelist.IWhitelist;

/**
 * This class should never be used directly.
 * Player-focused families offer features such as /tpa, whitelists, load-balancing, and direct connection.
 */
public interface IPlayerFocusedFamilyBase<S extends IPlayerServer> extends IBaseFamily<S> {
    /**
     * Connect a player to this family
     * @param player The player to connect
     * @return A PlayerServer on successful connection.
     * @throws RuntimeException If the connection cannot be made.
     */
    S connect(Player player);

    /**
     * Get the whitelist for this family, or `null` if there isn't one.
     * @return The whitelist or `null` if there isn't one.
     */
    IWhitelist whitelist();
}
