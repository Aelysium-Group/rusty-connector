package group.aelysium.rustyconnector.toolkit.velocity.config;

import group.aelysium.rustyconnector.toolkit.core.config.IYAML;

public interface DefaultConfig {
    boolean whitelist_enabled();
    String whitelist_name();
    Integer magicLink_serverTimeout();
    Integer magicLink_serverPingInterval();
}
