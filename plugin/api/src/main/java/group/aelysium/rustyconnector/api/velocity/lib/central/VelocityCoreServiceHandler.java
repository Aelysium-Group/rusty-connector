package group.aelysium.rustyconnector.api.velocity.lib.central;

import group.aelysium.rustyconnector.api.velocity.lib.family.IFamilyService;
import group.aelysium.rustyconnector.api.velocity.lib.players.IPlayerService;
import group.aelysium.rustyconnector.api.velocity.lib.server.IServerService;
import group.aelysium.rustyconnector.api.velocity.lib.serviceable.interfaces.ServiceHandler;
import group.aelysium.rustyconnector.api.velocity.lib.whitelist.IWhitelistService;

public interface VelocityCoreServiceHandler extends ServiceHandler {
    IFamilyService familyService();
    IServerService serverService();
    MessengerConnector<MessengerConnection> messenger();
    MySQLStorage storage();
    IPlayerService playerService();
    DataTransitService dataTransitService();
    MessageCacheService messageCacheService();
    IWhitelistService whitelistService();
    MagicLinkService magicLinkService();
    Optional<PartyService> partyService();
    Optional<FriendsService> friendsService();
    Optional<DynamicTeleportService> dynamicTeleportService();
    Optional<ViewportService> viewportService();
}