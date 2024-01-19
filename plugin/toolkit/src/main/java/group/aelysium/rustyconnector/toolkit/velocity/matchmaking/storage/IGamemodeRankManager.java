package group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage;


import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IMySQLStorageService;

import java.util.UUID;

public interface IGamemodeRankManager {
    String name();

    /**
     * Fetches a ranked player from this game.
     * If no ranked player info exists for this game yet, this method will create a new entry if `createNew` is true.
     * If `createNew` is false, this method will return `null` if there is no ranked player.
     * @param storage The storage service.
     * @param uuid The uuid of the player to fetch.
     * @param createNew Should this method create a new Ranked Player if one doesn't already exist.
     * @return {@link IRankedPlayer} or `null` if `createNew` is false and there isn't a RankedPlayer for the requested UUID.
     */
    IRankedPlayer rankedPlayer(IMySQLStorageService storage, UUID uuid, boolean createNew);

    IPlayerRank<?> playerRank(IMySQLStorageService storage, IPlayer player) throws IllegalStateException;
}
