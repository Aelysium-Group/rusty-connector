package group.aelysium.rustyconnector.plugin.velocity.central;

import group.aelysium.rustyconnector.api.velocity.central.ICoreServiceHandler;
import group.aelysium.rustyconnector.api.core.serviceable.ServiceHandler;
import group.aelysium.rustyconnector.core.lib.messenger.MessengerConnection;
import group.aelysium.rustyconnector.core.lib.messenger.MessengerConnector;
import group.aelysium.rustyconnector.core.lib.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.data_transit.DataTransitService;
import group.aelysium.rustyconnector.api.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancingService;
import group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.MagicLinkService;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.PlayerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.MySQLStorage;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.ViewportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.WhitelistService;

import java.util.Map;
import java.util.Optional;

public class CoreServiceHandler extends ServiceHandler implements ICoreServiceHandler {
    public CoreServiceHandler(Map<Class<? extends Service>, Service> services) {
        super(services);
    }

    public FamilyService family() {
        return this.find(FamilyService.class).orElseThrow();
    }
    public ServerService server() {
        return this.find(ServerService.class).orElseThrow();
    }
    public MessengerConnector<MessengerConnection> messenger() {
        return (MessengerConnector<MessengerConnection>) this.find(MessengerConnector.class).orElseThrow();
    }
    public MySQLStorage storage() {
        return this.find(MySQLStorage.class).orElseThrow();
    }
    public PlayerService player() {
        return this.find(PlayerService.class).orElseThrow();
    }
    public DataTransitService dataTransitService() {
        return this.find(DataTransitService.class).orElseThrow();
    }
    public MessageCacheService messageCache() {
        return this.find(MessageCacheService.class).orElseThrow();
    }
    public WhitelistService whitelist() {
        return this.find(WhitelistService.class).orElseThrow();
    }
    public LoadBalancingService loadBalancingService() {
        return this.find(LoadBalancingService.class).orElseThrow();
    }
    public MagicLinkService magicLinkService() {
        return this.find(MagicLinkService.class).orElseThrow();
    }
    public Optional<PartyService> party() {
        return this.find(PartyService.class);
    }
    public Optional<FriendsService> friends() {
        return this.find(FriendsService.class);
    }
    public Optional<DynamicTeleportService> dynamicTeleport() {
        return this.find(DynamicTeleportService.class);
    }
    public Optional<ViewportService> viewportService() {
        return this.find(ViewportService.class);
    }
}