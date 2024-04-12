package group.aelysium.rustyconnector.toolkit.velocity.matchmaking;

import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ISortable;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;

public interface IMatchPlayer extends ISortable {
    void markWin();
    void markLoss();
    void markTie();
    JsonObject rankToJSON();
    String rankSchemaName();
    boolean isRandomizedPlayerRank();

    IPlayer player();
    double rank();
    String gameId();
}
