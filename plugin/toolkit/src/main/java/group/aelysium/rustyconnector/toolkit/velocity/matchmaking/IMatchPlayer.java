package group.aelysium.rustyconnector.toolkit.velocity.matchmaking;

import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ISortable;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;

public interface IMatchPlayer extends ISortable {
    IPlayer player();
    IPlayerRank gameRank();
    String gameId();
}
