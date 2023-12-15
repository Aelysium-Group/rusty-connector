package group.aelysium.rustyconnector.toolkit.velocity.family.static_family;

import group.aelysium.rustyconnector.toolkit.velocity.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.server.MCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.util.Reference;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

import java.util.Optional;
import java.util.UUID;

public interface IResidenceDataEnclave<TMCLoader extends MCLoader, TPlayer extends Player, TStaticFamily extends StaticFamily<TMCLoader, TPlayer, ?>> {
    /**
     * Fetches the {@link IServerResidence residence} of a {@link com.velocitypowered.api.proxy.Player} inside of this {@link StaticFamily family}.
     * @param player The {@link com.velocitypowered.api.proxy.Player} to fetch.
     * @param family The {@link StaticFamily family} to look in.
     * @return {@link Optional<IServerResidence>}
     */
    Optional<? extends IServerResidence> fetch(Player.Reference player, TStaticFamily family);

    /**
     * Save a new {@link IServerResidence} for a {@link com.velocitypowered.api.proxy.Player}.
     * @param player The {@link com.velocitypowered.api.proxy.Player} to save the residence for.
     * @param server The {@link MCLoader} to assign as the residence.
     * @param family The {@link StaticFamily} to assign the residence in.
     */
    void save(Player.Reference player, TMCLoader server, TStaticFamily family);

    /**
     * Delete a specific {@link IServerResidence} from a family.
     * @param player The {@link com.velocitypowered.api.proxy.Player} to delete the residence from.
     * @param family The {@link StaticFamily} to delete the residence from.
     */
    void delete(Player.Reference player, TStaticFamily family);

    /**
     * Update the expirations for the {@link IServerResidence residences} in this {@link StaticFamily}.
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
