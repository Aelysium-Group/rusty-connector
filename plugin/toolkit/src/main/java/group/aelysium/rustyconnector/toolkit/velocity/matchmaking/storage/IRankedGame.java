package group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage;


import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IMySQLStorageService;

import java.util.UUID;

public interface IRankedGame<TPlayer extends Player> {
    String name();

    <TPlayerRank extends IPlayerRank<?>, TMySQLStorage extends IMySQLStorageService> TPlayerRank rankedPlayer(TMySQLStorage storage, UUID uuid);

    <TPlayerRank extends IPlayerRank<?>, TMySQLStorage extends IMySQLStorageService> TPlayerRank playerRank(TMySQLStorage storage, TPlayer player) throws IllegalStateException;
}
