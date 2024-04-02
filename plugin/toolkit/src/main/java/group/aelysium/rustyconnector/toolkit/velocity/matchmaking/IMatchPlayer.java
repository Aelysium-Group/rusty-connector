package group.aelysium.rustyconnector.toolkit.velocity.matchmaking;

import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ISortable;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;

public interface IMatchPlayer<PlayerRank extends IPlayerRank> extends ISortable {
    IPlayer player();
    PlayerRank rank();
    String gameId();
}
