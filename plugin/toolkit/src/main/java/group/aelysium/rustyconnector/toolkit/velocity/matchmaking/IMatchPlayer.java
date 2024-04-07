package group.aelysium.rustyconnector.toolkit.velocity.matchmaking;

import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ISortable;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;

public interface IMatchPlayer<PlayerRank extends IPlayerRank> extends ISortable {
    void markWin();
    void markLoss();
    JsonObject rankToJSON();
    String rankSchemaName();

    IPlayer player();
    double rank();
    String gameId();
}
