package group.aelysium.rustyconnector.toolkit.velocity.family.scalar_family;

import group.aelysium.rustyconnector.toolkit.velocity.players.IRustyPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IPlayerServer;

public interface IRootFamily<TPlayerServer extends IPlayerServer, TResolvablePlayer extends IRustyPlayer> extends IScalarFamily<TPlayerServer, TResolvablePlayer> {}
