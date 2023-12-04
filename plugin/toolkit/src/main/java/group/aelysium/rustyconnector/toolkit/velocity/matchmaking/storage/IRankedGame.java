package group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage;


import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IMySQLStorageService;

import java.util.UUID;

/**
 * ScoreCard is a representation of a player's entire ranked game history.
 * All ranks associated with a player should be able to be fetched using their scorecard.
 */
public interface IRankedGame<TPlayer extends IPlayer, TMySQLStorage extends IMySQLStorageService> {
    String name();

    <TPlayerRank extends IPlayerRank<?>> TPlayerRank rankedPlayer(TMySQLStorage storage, UUID uuid);

    <TPlayerRank extends IPlayerRank<?>> TPlayerRank playerRank(TMySQLStorage storage, TPlayer player) throws IllegalStateException;
}
