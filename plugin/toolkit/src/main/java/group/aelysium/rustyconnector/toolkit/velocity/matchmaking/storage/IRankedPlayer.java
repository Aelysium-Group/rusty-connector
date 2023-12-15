package group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage;

import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ISortable;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.players.Player;

import java.util.Optional;
import java.util.UUID;

public interface IRankedPlayer<TPlayer extends Player, TPlayerRank extends IPlayerRank<?>> extends ISortable {
    /**
     * The UUID of the player.
     * @return {@link UUID}
     */
    UUID uuid();

    /**
     * A convenience method which attempts to resolve {@link #uuid()} into an actual player.
     * @return {@link java.util.Optional< Player >}
     */
    Optional<TPlayer> player();

    /**
     * Gets the rank of this player.
     * Specifically, it calls {@link IPlayerRank#type()}.
     * @return The player's rank.
     */
    TPlayerRank rank();
}
