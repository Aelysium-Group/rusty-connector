package group.aelysium.rustyconnector.api.velocity.family.static_family;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.api.velocity.family.UnavailableProtocol;
import group.aelysium.rustyconnector.api.velocity.family.bases.IPlayerFocusedFamilyBase;
import group.aelysium.rustyconnector.api.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.api.velocity.server.IPlayerServer;
import group.aelysium.rustyconnector.api.velocity.util.LiquidTimestamp;

public interface IStaticFamily<S extends IPlayerServer> extends IPlayerFocusedFamilyBase<S> {
    /**
     * Gets the {@link UnavailableProtocol} for this family. {@link UnavailableProtocol} governs what happens when a player's resident server is unavailable.
     * @return {@link UnavailableProtocol}
     */
    UnavailableProtocol unavailableProtocol();

    /**
     * Gets the {@link LiquidTimestamp resident server expiration} for this family.
     * Server expiration governs how long a server counts as a {@link Player player's} residence before it no longer counts for that player.
     * You can set this option to save resources on your network.
     * For example, you can set an expiration of 30 days, after that time if a player doesn't join this family, their record will be removed.
     * @return {@link LiquidTimestamp}
     */
    LiquidTimestamp homeServerExpiration();

    /**
     * Gets the {@link IResidenceDataEnclave} for this {@link IStaticFamily}.
     * Data enclave gives you an interface between this family and the remote storage connector that this family uses.
     * @return {@link IResidenceDataEnclave}
     */
    IResidenceDataEnclave dataEnclave();

    /**
     * Connects a {@link Player} to this {@link IStaticFamily} in accordance with it's {@link ILoadBalancer}.
     * @param player The {@link Player} to connect.
     * @return The {@link IPlayerServer} that the {@link Player} was connected to.
     * @throws RuntimeException If there was an issue connecting the player to this family.
     */
    S connect(Player player) throws RuntimeException;
}