package group.aelysium.rustyconnector.toolkit.velocity.central;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.IServiceableService;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.IDynamicTeleportServiceHandler;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamilyService;
import group.aelysium.rustyconnector.toolkit.velocity.family.bases.IBaseFamily;
import group.aelysium.rustyconnector.toolkit.velocity.family.scalar_family.IRootFamily;
import group.aelysium.rustyconnector.toolkit.velocity.friends.IFriendRequest;
import group.aelysium.rustyconnector.toolkit.velocity.friends.IFriendsService;
import group.aelysium.rustyconnector.toolkit.velocity.parties.IParty;
import group.aelysium.rustyconnector.toolkit.velocity.parties.IPartyInvite;
import group.aelysium.rustyconnector.toolkit.velocity.parties.IPartyService;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayerService;
import group.aelysium.rustyconnector.toolkit.velocity.players.IResolvablePlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IPlayerServer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IServerService;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.IServiceHandler;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IMySQLStorageService;
import group.aelysium.rustyconnector.toolkit.velocity.whitelist.IWhitelist;
import group.aelysium.rustyconnector.toolkit.velocity.whitelist.IWhitelistService;

import java.util.Optional;

public interface ICoreServiceHandler extends IServiceHandler {
    /**
     * Gets the {@link IFamilyService family service} which allows access to server families and other family related logic.
     * @return {@link IFamilyService}
     */
    <TPlayerServer extends IPlayerServer, TRootFamily extends IRootFamily<TPlayerServer>, TBaseFamily extends IBaseFamily<TPlayerServer>>
        IFamilyService<TPlayerServer, TRootFamily, TBaseFamily> family();

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
    <TWhitelist extends IWhitelist>
        IWhitelistService<TWhitelist> whitelist();

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
     * Gets the {@link IServiceableService<IDynamicTeleportServiceHandler> dynamic teleport service}.
     * The dynamic teleport module may not always be enabled, hence this returns an {@link Optional<IServiceableService<IDynamicTeleportServiceHandler>>}
     * @return {@link Optional<IServiceableService>}
     */
    Optional<? extends IServiceableService<?>> dynamicTeleport();
}