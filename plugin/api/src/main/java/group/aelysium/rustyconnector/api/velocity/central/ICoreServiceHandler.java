package group.aelysium.rustyconnector.api.velocity.central;

import group.aelysium.rustyconnector.api.velocity.family.IFamilyService;
import group.aelysium.rustyconnector.api.velocity.family.bases.IBaseFamily;
import group.aelysium.rustyconnector.api.velocity.friends.IFriendRequest;
import group.aelysium.rustyconnector.api.velocity.friends.IFriendsService;
import group.aelysium.rustyconnector.api.velocity.parties.IParty;
import group.aelysium.rustyconnector.api.velocity.parties.IPartyInvite;
import group.aelysium.rustyconnector.api.velocity.parties.IPartyService;
import group.aelysium.rustyconnector.api.velocity.players.IPlayerService;
import group.aelysium.rustyconnector.api.velocity.players.IResolvablePlayer;
import group.aelysium.rustyconnector.api.velocity.server.IPlayerServer;
import group.aelysium.rustyconnector.api.velocity.server.IServerService;
import group.aelysium.rustyconnector.api.core.serviceable.interfaces.ServiceHandler;
import group.aelysium.rustyconnector.api.velocity.storage.IMySQLStorageService;
import group.aelysium.rustyconnector.api.velocity.whitelist.IWhitelistService;

import java.util.Optional;

public interface ICoreServiceHandler extends ServiceHandler {
    /**
     * Gets the {@link IFamilyService family service} which allows access to server families and other family related logic.
     * @return {@link IFamilyService}
     */
    IFamilyService family();

    /**
     * Gets the {@link IServerService server service} which allows access to server registration, unregistration, connection, and other server related logic.
     * @return {@link IServerService}
     */
    <TPlayerServer extends IPlayerServer, TBaseFamily extends IBaseFamily<TPlayerServer>>
        IServerService<TPlayerServer, TBaseFamily> server();

    /**
     * Gets RustyConnector's {@link IMySQLStorageService remote storage connector service} which allows direct access to database storage.
     * @return {@link IMySQLStorageService}
     */
    IMySQLStorageService storage();

    /**
     * Gets the {@link IPlayerService player service} which allows access to player resolving logic for usage when dealing with offline player data.
     * @return {@link IPlayerService}
     */
    IPlayerService player();

    /**
     * Gets the {@link IWhitelistService whitelist service} which allows access to the proxy's configured whitelists.
     * @return {@link IWhitelistService}
     */
    IWhitelistService whitelist();

    /**
     * Gets the {@link IPartyService party service}.
     * The party module may not always be enabled, hence this returns an {@link Optional<IPartyService>}
     * @return {@link Optional<IPartyService>}
     */
    <TResolvablePlayer extends IResolvablePlayer, TPlayerServer extends IPlayerServer, TParty extends IParty<TPlayerServer>, TPartyInvite extends IPartyInvite<TResolvablePlayer>>
        Optional<IPartyService<TResolvablePlayer, TPlayerServer, TParty, TPartyInvite>> party();

    /**
     * Gets the {@link IFriendsService friends service}.
     * The friends module may not always be enabled, hence this returns an {@link Optional<IFriendsService>}
     * @return {@link Optional<IFriendsService>}
     */
    <TResolvablePlayer extends IResolvablePlayer, TFriendRequest extends IFriendRequest>
        Optional<? extends IFriendsService<TResolvablePlayer, TFriendRequest>> friends();

    /**
     * Gets the {@link IDynamicTeleportService dynamic teleport service}.
     * The dynamic teleport module may not always be enabled, hence this returns an {@link Optional<IDynamicTeleportService>}
     * @return {@link Optional<IDynamicTeleportService>}
     */
    Optional<IDynamicTeleportService> dynamicTeleport();
}