package group.aelysium.rustyconnector.toolkit.velocity.family;

import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

public interface IRootConnectable<TMCLoader extends IMCLoader, TPlayer extends IPlayer> extends IInitialEventConnectable<TMCLoader, TPlayer> {}
