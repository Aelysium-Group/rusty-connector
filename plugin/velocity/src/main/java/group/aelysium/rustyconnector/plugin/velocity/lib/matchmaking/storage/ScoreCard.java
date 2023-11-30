package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage;

import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.IPlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.RandomizedPlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.WinLossPlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.WinRatePlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.MySQLStorage;

import java.util.HashMap;
import java.util.Optional;

/**
 * A RankedGame is a representation of all variations of a player's rank within a specific gamemode.
 * If, over the time of this game existing, it has ranked players based on both ELO and WIN_RATE, both of those
 * ranks are saved here and can be retrieved.
 */
public class ScoreCard {
    protected final HashMap<Class<? extends IPlayerRank<?>>, IPlayerRank<?>> ranks = new HashMap<>();

    public <TRankHolder extends IPlayerRank<?>> void store(MySQLStorage storage, TRankHolder rank) {
        this.ranks.put(rank.type().get(), rank);

        storage.store(ranks);
    }

    /**
     * Fetches the player's current rank based on the schema provided.
     * If no rank exists for that schema, will create a new rank entry and return that.
     * @param schema The schema to search for.
     * @return {@link TRankHolder}
     * @param <TRankHolder> The schema to search using.
     * @throws IllegalStateException If there was a fatal exception while attempting to get the user's rank.
     */
    @SuppressWarnings("unchecked")
    public <TRankHolder extends IPlayerRank<?>> TRankHolder fetch(MySQLStorage storage, RankSchema.Type<?> schema) {
        try {
            TRankHolder rank = (TRankHolder) this.ranks.get(schema.get());
            if (rank == null) {
                TRankHolder newRank = (TRankHolder) schema.get().getDeclaredConstructor().newInstance();
                this.ranks.put(schema.get(), newRank);

                storage.store(this.ranks);

                return newRank;
            }

            return rank;
        } catch (Exception ignore) {}

        throw new IllegalStateException();
    }

    public interface RankSchema {
        Type<Class<RandomizedPlayerRank>> RANDOMIZED = new Type<>(RandomizedPlayerRank.class);
        Type<Class<WinLossPlayerRank>> WIN_LOSS = new Type<>(WinLossPlayerRank.class);
        Type<Class<WinRatePlayerRank>> WIN_RATE = new Type<>(WinRatePlayerRank.class);

        class Type<T extends Class<? extends IPlayerRank<?>>> {
            T holder;

            public Type(T holder) {
                this.holder = holder;
            }

            public T get() {
                return this.holder;
            }
        }
    }
}
