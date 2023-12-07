package group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage;

import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IRandomizedPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IWinLossPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IWinRatePlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IMySQLStorageService;

public interface IScoreCard<TMySQLStorageService extends IMySQLStorageService> {
    /**
     * Stores the specified rank into this scorecard.
     * This method will also store the rank into the remote storage resource.
     * @param storage The storage resource to store in.
     * @param rank The rank to store.
     */
    <TPlayerRank extends IPlayerRank<?>> void store(TMySQLStorageService storage, TPlayerRank rank);

    /**
     * Fetches the player's current rank based on the schema provided.
     * If no rank exists for that schema, will create a new rank entry and return that.
     * @param schema The schema to search for.
     * @return {@link TPlayerRank}
     * @throws IllegalStateException If there was a fatal exception while attempting to get the user's rank.
     */
    <TPlayerRank extends IPlayerRank<?>> TPlayerRank fetch(TMySQLStorageService storage, IRankSchema.Type<?> schema);

    interface IRankSchema {
        Type<Class<IRandomizedPlayerRank>> RANDOMIZED = new Type<>(IRandomizedPlayerRank.class);
        Type<Class<IWinLossPlayerRank>> WIN_LOSS = new Type<>(IWinLossPlayerRank.class);
        Type<Class<IWinRatePlayerRank>> WIN_RATE = new Type<>(IWinRatePlayerRank.class);

        class Type<T extends Class<? extends IPlayerRank<?>>> {
            T holder;

            public Type(T holder) {
                this.holder = holder;
            }

            public T get() {
                return this.holder;
            }
        }

        static Type<?> valueOf(String type) {
            switch (type.toUpperCase()) {
                case "WIN_LOSS" -> {
                    return WIN_LOSS;
                }
                case "WIN_RATE" -> {
                    return WIN_RATE;
                }
            }
            return RANDOMIZED;
        }
    }
}
