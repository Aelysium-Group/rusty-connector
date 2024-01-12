package group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage;


import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IMySQLStorageService;

import java.util.UUID;

public interface IRankedGame {
    String name();

    IRankedPlayer rankedPlayer(IMySQLStorageService storage, UUID uuid);

    IPlayerRank<?> playerRank(IMySQLStorageService storage, IPlayer player) throws IllegalStateException;
}
