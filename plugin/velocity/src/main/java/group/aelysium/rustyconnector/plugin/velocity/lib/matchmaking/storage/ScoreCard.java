package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage;

import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageService;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IScoreCard;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IPlayerRank;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ScoreCard is a representation of a player's entire ranked game history.
 * All ranks associated with a player should be able to be fetched using their scorecard.
 */
public class ScoreCard implements IScoreCard<StorageService> {
    protected final Map<Class<? extends IPlayerRank<?>>, IPlayerRank<?>> ranks = new ConcurrentHashMap<>();

    public <TPlayerRank extends IPlayerRank<?>> void store(StorageService storage, TPlayerRank rank) {
        this.ranks.put(rank.type().get(), rank);

        storage.store(ranks);
    }

    @SuppressWarnings("unchecked")
    public <TPlayerRank extends IPlayerRank<?>> TPlayerRank fetch(StorageService storage, IRankSchema.Type<?> schema) {
        try {

            TPlayerRank rank = (TPlayerRank) this.ranks.get(schema.get());
            if (rank == null) {
                TPlayerRank newRank = (TPlayerRank) schema.get().getDeclaredConstructor().newInstance();
                this.ranks.put(schema.get(), newRank);

                storage.store(this.ranks);

                return newRank;
            }

            return rank;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public void quantize(StorageService storage, IRankSchema.Type<?> schema) {
        try {
            IPlayerRank<?> rank = this.ranks.get(schema.get());

            this.ranks.clear();
            this.ranks.put(schema.get(), rank);

            storage.store(this.ranks);
        } catch (Exception ignore) {
        }

        throw new IllegalStateException();
    }
}
