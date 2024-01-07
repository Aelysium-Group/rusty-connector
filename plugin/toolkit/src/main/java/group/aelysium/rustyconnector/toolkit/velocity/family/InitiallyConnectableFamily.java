package group.aelysium.rustyconnector.toolkit.velocity.family;

import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import group.aelysium.rustyconnector.toolkit.velocity.family.scalar_family.IScalarFamily;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

public interface InitiallyConnectableFamily {
    /**
     * Connects a {@link com.velocitypowered.api.proxy.Player} to this {@link IScalarFamily} in accordance with it's {@link ILoadBalancer}.
     * This method primarily exists as compatibility with Velocity's built-in events' system.
     * @param event The {@link PlayerChooseInitialServerEvent} which initialized this request.
     * @return The {@link IMCLoader} that the {@link PlayerChooseInitialServerEvent} was connected to.
     * @throws RuntimeException If there was an issue connecting the player to this family.
     */
    IMCLoader connect(PlayerChooseInitialServerEvent event);
}
