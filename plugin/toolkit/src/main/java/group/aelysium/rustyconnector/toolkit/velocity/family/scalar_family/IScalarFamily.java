package group.aelysium.rustyconnector.toolkit.velocity.family.scalar_family;

import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

public interface IScalarFamily<TMCLoader extends IMCLoader, TPlayer extends IPlayer> extends IFamily<TMCLoader, TPlayer> {
    /**
     * Connects a {@link Player} to this {@link IScalarFamily} in accordance with it's {@link ILoadBalancer}.
     * This method primarily exists as compatibility with Velocity's built-in events' system.
     * @param event The {@link PlayerChooseInitialServerEvent} which initialized this request.
     * @return The {@link IMCLoader} that the {@link PlayerChooseInitialServerEvent} was connected to.
     * @throws RuntimeException If there was an issue connecting the player to this family.
     */
    TMCLoader connect(PlayerChooseInitialServerEvent event);

    /**
     * Fetches the current server that this family's {@link ILoadBalancer} is pointing to.
     * @param player {@link TPlayer}
     * @return {@link IMCLoader}
     * @throws RuntimeException If there was an issue fetching a server.
     */
    TMCLoader fetchAny(TPlayer player) throws RuntimeException;
}
