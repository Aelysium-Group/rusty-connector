package group.aelysium.rustyconnector.api.velocity.family.bases;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.api.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.api.velocity.server.IPlayerServer;
import group.aelysium.rustyconnector.api.velocity.whitelist.IWhitelist;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.*;

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
