package group.aelysium.rustyconnector.api.velocity.family.scalar_family;

import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.api.velocity.family.bases.IPlayerFocusedFamilyBase;
import group.aelysium.rustyconnector.api.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.api.velocity.server.IPlayerServer;

public interface IScalarFamily<S extends IPlayerServer> extends IPlayerFocusedFamilyBase<S> {
    /**
     * Connects a {@link Player} to this {@link IScalarFamily} in accordance with it's {@link ILoadBalancer}.
     * @param player The {@link Player} to connect.
     * @return The {@link IPlayerServer} that the {@link Player} was connected to.
     * @throws RuntimeException If there was an issue connecting the player to this family.
     */
    S connect(Player player) throws RuntimeException;
    /**
     * Connects a {@link Player} to this {@link IScalarFamily} in accordance with it's {@link ILoadBalancer}.
     * This method primarily exists as compatibility with Velocity's built-in events' system.
     * @param event The {@link PlayerChooseInitialServerEvent} which initialized this request.
     * @return The {@link IPlayerServer} that the {@link PlayerChooseInitialServerEvent} was connected to.
     * @throws RuntimeException If there was an issue connecting the player to this family.
     */
    S connect(PlayerChooseInitialServerEvent event);

    /**
     * Fetches the current server that this family's {@link ILoadBalancer} is pointing to.
     * @param player {@link Player}
     * @return {@link IPlayerServer}
     * @throws RuntimeException If there was an issue fetching a server.
     */
    S fetchAny(Player player) throws RuntimeException;
}
