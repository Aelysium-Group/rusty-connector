package group.aelysium.rustyconnector.toolkit.velocity.central;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.IServiceableService;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.IDynamicTeleportServiceHandler;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamilyService;
import group.aelysium.rustyconnector.toolkit.velocity.family.Family;
import group.aelysium.rustyconnector.toolkit.velocity.family.scalar_family.RootFamily;
import group.aelysium.rustyconnector.toolkit.velocity.friends.IFriendRequest;
import group.aelysium.rustyconnector.toolkit.velocity.friends.IFriendsService;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.parties.IParty;
import group.aelysium.rustyconnector.toolkit.velocity.parties.IPartyInvite;
import group.aelysium.rustyconnector.toolkit.velocity.parties.IPartyService;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayerService;
import group.aelysium.rustyconnector.toolkit.velocity.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.server.MCLoader;
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
    <TMCLoader extends MCLoader, TPlayer extends Player, TLoadBalancer extends ILoadBalancer<TMCLoader>, TRootFamily extends RootFamily<TMCLoader, TPlayer, TLoadBalancer>, TBaseFamily extends Family<TMCLoader, TPlayer, TLoadBalancer>>
        IFamilyService<TMCLoader, TPlayer, TLoadBalancer, TRootFamily, TBaseFamily> family();

    /**
     * Gets the {@link IServerService server service} which allows access to server registration, unregistration, connection, and other server related logic.
     * @return {@link IServerService}
     */
    <TMCLoader extends MCLoader, TPlayer extends Player, TLoadBalancer extends ILoadBalancer<TMCLoader>, TBaseFamily extends Family<TMCLoader, TPlayer, TLoadBalancer>>
        IServerService<TMCLoader, TPlayer, TLoadBalancer, TBaseFamily> server();

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
    <TPlayer extends Player, TMCLoader extends MCLoader, TParty extends IParty<TMCLoader>, TPartyInvite extends IPartyInvite<TPlayer>>
        Optional<IPartyService<TPlayer, TMCLoader, TParty, TPartyInvite>> party();

    /**
     * Gets the {@link IFriendsService friends service}.
     * The friends module may not always be enabled, hence this returns an {@link Optional<IFriendsService>}
     * @return {@link Optional<IFriendsService>}
     */
    <TPlayer extends Player, TFriendRequest extends IFriendRequest>
        Optional<? extends IFriendsService<TPlayer, TFriendRequest>> friends();

    /**
     * Gets the {@link IServiceableService<IDynamicTeleportServiceHandler> dynamic teleport service}.
     * The dynamic teleport module may not always be enabled, hence this returns an {@link Optional<IServiceableService<IDynamicTeleportServiceHandler>>}
     * @return {@link Optional<IServiceableService>}
     */
    Optional<? extends IServiceableService<?>> dynamicTeleport();
}