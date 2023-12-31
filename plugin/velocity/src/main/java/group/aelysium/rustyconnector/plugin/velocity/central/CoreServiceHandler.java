package group.aelysium.rustyconnector.plugin.velocity.central;

import group.aelysium.rustyconnector.core.lib.events.EventManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.toolkit.core.packet.VelocityPacketBuilder;
import group.aelysium.rustyconnector.toolkit.velocity.central.ICoreServiceHandler;
import group.aelysium.rustyconnector.toolkit.core.serviceable.ServiceHandler;
import group.aelysium.rustyconnector.core.lib.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.data_transit.DataTransitService;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.core.lib.messenger.implementors.redis.RedisConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancingService;
import group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.MagicLinkService;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.PlayerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageService;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.WhitelistService;

import java.util.Map;
import java.util.Optional;

public class CoreServiceHandler extends ServiceHandler implements ICoreServiceHandler {
    public CoreServiceHandler(Map<Class<? extends Service>, Service> services) {
        super(services);
    }

    public EventManager events() {
        return this.find(EventManager.class).orElseThrow();
    }
    public FamilyService family() {
        return this.find(FamilyService.class).orElseThrow();
    }
    public ServerService server() {
        return this.find(ServerService.class).orElseThrow();
    }
    public MagicLinkService magicLink() {
        return this.find(MagicLinkService.class).orElseThrow();
    }
    public StorageService storage() {
        return this.find(StorageService.class).orElseThrow();
    }
    public PlayerService player() {
        return this.find(PlayerService.class).orElseThrow();
    }
    public ConfigService config() {
        return this.find(ConfigService.class).orElseThrow();
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
    public VelocityPacketBuilder packetBuilder() {
        return this.find(VelocityPacketBuilder.class).orElseThrow();
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
}