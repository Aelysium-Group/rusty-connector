package group.aelysium.rustyconnector.toolkit.velocity.config;

import group.aelysium.rustyconnector.toolkit.core.packet.PacketStatus;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;

import java.util.List;

public interface DataTransitConfig {
    int maxPacketLength();
    int cache_size();
    List<PacketType.Mapping> cache_ignoredTypes();
    List<PacketStatus> cache_ignoredStatuses();
    boolean whitelist_enabled();
    List<String> whitelist_addresses();
    boolean denylist_enabled();
    List<String> denylist_addresses();
}
