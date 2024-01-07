package group.aelysium.rustyconnector.toolkit.velocity.config;

import group.aelysium.rustyconnector.toolkit.core.config.IYAML;

public interface MagicMCLoaderConfig {
    String family();
    int weight();
    int playerCap_soft();
    int playerCap_hard();
}
