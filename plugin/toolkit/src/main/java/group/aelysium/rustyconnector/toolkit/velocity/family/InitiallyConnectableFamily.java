package group.aelysium.rustyconnector.toolkit.velocity.family;

import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import group.aelysium.rustyconnector.toolkit.velocity.family.scalar_family.ScalarFamily;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.server.MCLoader;

public interface InitiallyConnectableFamily<TMCLoader extends MCLoader, TPlayer extends Player, TLoadBalancer extends ILoadBalancer<TMCLoader>> extends Family<TMCLoader, TPlayer, TLoadBalancer> {
    /**
     * Connects a {@link com.velocitypowered.api.proxy.Player} to this {@link ScalarFamily} in accordance with it's {@link ILoadBalancer}.
     * This method primarily exists as compatibility with Velocity's built-in events' system.
     * @param event The {@link PlayerChooseInitialServerEvent} which initialized this request.
     * @return The {@link MCLoader} that the {@link PlayerChooseInitialServerEvent} was connected to.
     * @throws RuntimeException If there was an issue connecting the player to this family.
     */
    TMCLoader connect(PlayerChooseInitialServerEvent event);
}
