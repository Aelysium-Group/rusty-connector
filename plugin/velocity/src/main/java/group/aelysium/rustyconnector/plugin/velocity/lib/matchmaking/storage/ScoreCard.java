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

    @SuppressWarnings("unchecked")
    public <TRankHolder extends IPlayerRank<?>> Optional<TRankHolder> get(RankSchema.Type<?> schema) {
        try {
            TRankHolder s = (TRankHolder) this.ranks.get(schema.get());
            if (s == null) return Optional.empty();
            return Optional.of(s);
        } catch (Exception ignore) {}

        return Optional.empty();
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
