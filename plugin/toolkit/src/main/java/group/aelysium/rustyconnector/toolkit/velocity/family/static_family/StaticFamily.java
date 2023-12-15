package group.aelysium.rustyconnector.toolkit.velocity.family.static_family;

import group.aelysium.rustyconnector.toolkit.velocity.family.Family;
import group.aelysium.rustyconnector.toolkit.velocity.family.InitiallyConnectableFamily;
import group.aelysium.rustyconnector.toolkit.velocity.family.UnavailableProtocol;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.server.MCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

import java.util.UUID;

public interface StaticFamily<TMCLoader extends MCLoader, TPlayer extends Player, TLoadBalancer extends ILoadBalancer<TMCLoader>> extends Family<TMCLoader, TPlayer, TLoadBalancer>, InitiallyConnectableFamily<TMCLoader, TPlayer, TLoadBalancer> {
    /**
     * Gets the {@link UnavailableProtocol} for this family. {@link UnavailableProtocol} governs what happens when a player's resident server is unavailable.
     * @return {@link UnavailableProtocol}
     */
    UnavailableProtocol unavailableProtocol();

    /**
     * Gets the {@link LiquidTimestamp resident server expiration} for this family.
     * Server expiration governs how long a server counts as a {@link com.velocitypowered.api.proxy.Player player's} residence before it no longer counts for that player.
     * You can set this option to save resources on your network.
     * For example, you can set an expiration of 30 days, after that time if a player doesn't join this family, their record will be removed.
     * @return {@link LiquidTimestamp}
     */
    LiquidTimestamp homeServerExpiration();

    /**
     * Gets the {@link IResidenceDataEnclave} for this {@link StaticFamily}.
     * Data enclave gives you an interface between this family and the remote storage connector that this family uses.
     * @return {@link IResidenceDataEnclave}
     */
    <TResidenceDataEnclave extends IResidenceDataEnclave<TMCLoader, TPlayer, StaticFamily<TMCLoader, TPlayer, TLoadBalancer>>> TResidenceDataEnclave dataEnclave();
}