package group.aelysium.rustyconnector.toolkit.velocity.family.static_family;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.toolkit.velocity.players.IRustyPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IPlayerServer;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

import java.util.Optional;

public interface IResidenceDataEnclave<TPlayerServer extends IPlayerServer, TResolvablePlayer extends IRustyPlayer, TStaticFamily extends IStaticFamily<TPlayerServer, TResolvablePlayer>> {
    /**
     * Fetches the {@link IServerResidence residence} of a {@link Player} inside of this {@link IStaticFamily family}.
     * @param player The {@link Player} to fetch.
     * @param family The {@link IStaticFamily family} to look in.
     * @return {@link Optional<IServerResidence>}
     */
    Optional<? extends IServerResidence> fetch(Player player, TStaticFamily family);

    /**
     * Save a new {@link IServerResidence} for a {@link Player}.
     * @param player The {@link Player} to save the residence for.
     * @param server The {@link IPlayerServer} to assign as the residence.
     * @param family The {@link IStaticFamily} to assign the residence in.
     */
    void save(Player player, TPlayerServer server, TStaticFamily family);

    /**
     * Delete a specific {@link IServerResidence} from a family.
     * @param player The {@link Player} to delete the residence from.
     * @param family The {@link IStaticFamily} to delete the residence from.
     */
    void delete(Player player, TStaticFamily family);

    /**
     * Update the expirations for the {@link IServerResidence residences} in this {@link IStaticFamily}.
     * If there are residence mappings that are set to never expire, but {@link LiquidTimestamp} is set to expire, these residences will be updated to expire after that time.
     * If there are residence mappings that are set to expire eventually, but {@link LiquidTimestamp} is set to never expire, these residences will be updated to never expire.
     * @param expiration
     * @param family
     * @throws Exception
     */
    void updateExpirations(LiquidTimestamp expiration, TStaticFamily family) throws Exception;

    /**
     * Deletes all {@link IServerResidence} that are expired.
     * @param family The family to search in.
     */
    void purgeExpired(TStaticFamily family);
}
