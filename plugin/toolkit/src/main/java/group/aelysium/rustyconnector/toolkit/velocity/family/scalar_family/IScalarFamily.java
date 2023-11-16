package group.aelysium.rustyconnector.toolkit.velocity.family.scalar_family;

import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.toolkit.velocity.family.bases.IPlayerFocusedFamilyBase;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.IRustyPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IPlayerServer;

public interface IScalarFamily<TPlayerServer extends IPlayerServer, TResolvablePlayer extends IRustyPlayer> extends IPlayerFocusedFamilyBase<TPlayerServer, TResolvablePlayer> {
    /**
     * Connects a {@link Player} to this {@link IScalarFamily} in accordance with it's {@link ILoadBalancer}.
     * This method primarily exists as compatibility with Velocity's built-in events' system.
     * @param event The {@link PlayerChooseInitialServerEvent} which initialized this request.
     * @return The {@link IPlayerServer} that the {@link PlayerChooseInitialServerEvent} was connected to.
     * @throws RuntimeException If there was an issue connecting the player to this family.
     */
    TPlayerServer connect(PlayerChooseInitialServerEvent event);

    /**
     * Fetches the current server that this family's {@link ILoadBalancer} is pointing to.
     * @param player {@link TResolvablePlayer}
     * @return {@link IPlayerServer}
     * @throws RuntimeException If there was an issue fetching a server.
     */
    TPlayerServer fetchAny(TResolvablePlayer player) throws RuntimeException;
}
