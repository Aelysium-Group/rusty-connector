package group.aelysium.rustyconnector.plugin.velocity.central;


import group.aelysium.rustyconnector.core.lib.database.redis.RedisService;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.data_transit.DataTransitService;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancingService;
import group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.MagicLinkService;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.PlayerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.WhitelistService;

import java.util.Map;
import java.util.Optional;

public class ProcessorServiceHandler extends group.aelysium.rustyconnector.core.lib.serviceable.ServiceHandler {
    public ProcessorServiceHandler(Map<Class<? extends Service>, Service> services) {
        super(services);
    }
    public ProcessorServiceHandler() {
        super();
    }

    public FamilyService familyService() {
        return this.find(FamilyService.class).orElseThrow();
    }
    public ServerService serverService() {
        return this.find(ServerService.class).orElseThrow();
    }
    public RedisService redisService() {
        return this.find(RedisService.class).orElseThrow();
    }
    public DataTransitService dataTransitService() {
        return this.find(DataTransitService.class).orElseThrow();
    }
    public MessageCacheService messageCacheService() {
        return this.find(MessageCacheService.class).orElseThrow();
    }
    public WhitelistService whitelistService() {
        return this.find(WhitelistService.class).orElseThrow();
    }
    public LoadBalancingService loadBalancingService() {
        return this.find(LoadBalancingService.class).orElseThrow();
    }
    public MagicLinkService magicLinkService() {
        return this.find(MagicLinkService.class).orElseThrow();
    }
    public Optional<PartyService> partyService() {
        return this.find(PartyService.class);
    }
    public Optional<FriendsService> friendsService() {
        return this.find(FriendsService.class);
    }
    public Optional<PlayerService> playerService() {
        return this.find(PlayerService.class);
    }
    public Optional<DynamicTeleportService> dynamicTeleportService() {
        return this.find(DynamicTeleportService.class);
    }
}