package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage;

import group.aelysium.rustyconnector.plugin.velocity.lib.storage.MySQLStorage;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IScoreCard;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IPlayerRank;

import java.util.HashMap;

/**
 * A RankedGame is a representation of all variations of a player's rank within a specific gamemode.
 * If, over the time of this game existing, it has ranked players based on both ELO and WIN_RATE, both of those
 * ranks are saved here and can be retrieved.
 */
public class ScoreCard implements IScoreCard<MySQLStorage> {
    protected final HashMap<Class<? extends IPlayerRank<?>>, IPlayerRank<?>> ranks = new HashMap<>();

    public <TPlayerRank extends IPlayerRank<?>> void store(MySQLStorage storage, TPlayerRank rank) {
        this.ranks.put(rank.type().get(), rank);

        storage.store(ranks);
    }

    @SuppressWarnings("unchecked")
    public <TPlayerRank extends IPlayerRank<?>> TPlayerRank fetch(MySQLStorage storage, IRankSchema.Type<?> schema) {
        try {
            TPlayerRank rank = (TPlayerRank) this.ranks.get(schema.get());
            if (rank == null) {
                TPlayerRank newRank = (TPlayerRank) schema.get().getDeclaredConstructor().newInstance();
                this.ranks.put(schema.get(), newRank);

                storage.store(this.ranks);

                return newRank;
            }

            return rank;
        } catch (Exception ignore) {
        }

        throw new IllegalStateException();
    }
}
