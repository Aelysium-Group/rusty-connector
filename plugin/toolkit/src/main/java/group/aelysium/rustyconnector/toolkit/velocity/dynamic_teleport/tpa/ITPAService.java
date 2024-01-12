package group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

import java.util.List;

public interface ITPAService extends Service {
    /**
     * Gets the settings that this {@link ITPAService} abides by.
     * @return {@link TPAServiceSettings}
     */
    TPAServiceSettings settings();

    /**
     * Gets the {@link ITPACleaningService} which is responsible for cleaning old requests.
     * @return {@link ITPACleaningService}
     */
    ITPACleaningService cleaner();

    /**
     * Returns the {@link ITPAHandler} that's used to manage the tpa requests for the specified family.
     * @param family The family to get the tpa handler for.
     * @return {@link ITPAHandler}
     */
    ITPAHandler tpaHandler(IFamily family);

    /**
     * Gets a list of all TPA handlers.
     * @return {@link List<ITPAHandler>}
     */
    List<ITPAHandler> allTPAHandlers();

    /**
     * Teleports a player `source` to another player `target` even if their on totally different servers.
     * This method works by checking to see if the players are on the same server. If they aren't it will teleport player `source` to the target server.
     * This method then ships a packet request to the target server telling it to teleport the `source` player to the `target` player once the `source` player has connected.
     * <p>
     * Once the `source` player has joined the target server, this method no-longer has any knowledge of what's happening. It's RC-MCLoader's job to make sure `source` player teleported to the `target` player.
     * @param source The player that was invited to teleport.
     * @param target The player that sent the invite and is going to have `source` teleported to their location.
     * @param targetServer The server that `target` is currently playing on.
     */
    void tpaSendPlayer(IPlayer source, IPlayer target, IMCLoader targetServer);
}
