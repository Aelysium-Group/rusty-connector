package group.aelysium.rustyconnector.toolkit.velocity.family.bases;

import group.aelysium.rustyconnector.toolkit.velocity.players.IRustyPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IPlayerServer;
import group.aelysium.rustyconnector.toolkit.velocity.whitelist.IWhitelist;

/**
 * This class should never be used directly.
 * Player-focused families offer features such as /tpa, whitelists, load-balancing, and direct connection.
 */
public interface IPlayerFocusedFamilyBase<TPlayerServer extends IPlayerServer, TResolvablePlayer extends IRustyPlayer> extends IBaseFamily<TPlayerServer, TResolvablePlayer> {
    /**
     * Get the whitelist for this family, or `null` if there isn't one.
     * @return The whitelist or `null` if there isn't one.
     */
    IWhitelist whitelist();
}
