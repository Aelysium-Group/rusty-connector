package group.aelysium.rustyconnector.toolkit.velocity.family;

import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

public interface IConnectable<TMCLoader extends IMCLoader, TPlayer extends IPlayer> {
    String id();

    /**
     * Connects the player to this resource.
     * @param player The player to connect.
     * @return The server that the player was connected to.
     */
    TMCLoader connect(TPlayer player);
}
