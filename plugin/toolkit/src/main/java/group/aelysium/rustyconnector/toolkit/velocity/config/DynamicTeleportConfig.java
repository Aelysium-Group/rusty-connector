package group.aelysium.rustyconnector.toolkit.velocity.config;

import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;
import java.util.List;
import java.util.Map;

public interface DynamicTeleportConfig {
    boolean isEnabled();
    boolean isTpa_enabled();
    boolean isTpa_friendsOnly();
    List<String> getTpa_enabledFamilies();
    boolean isTpa_ignorePlayerCap();
    LiquidTimestamp getTpa_expiration();
    boolean isFamilyAnchor_enabled();
    List<Map.Entry<String, String>> getFamilyAnchor_anchors();
    boolean isFamilyInjector_enabled();
    List<Map.Entry<String, String>> getFamilyInjector_injectors();
    boolean isHub_enabled();
    List<String> getHub_enabledFamilies();
}
