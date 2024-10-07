package group.aelysium.rustyconnector.toolkit.velocity.config;

import group.aelysium.rustyconnector.toolkit.velocity.whitelist.IWhitelistPlayerFilter;

import java.util.List;

public interface WhitelistConfig {
    boolean getUse_players();
    List<IWhitelistPlayerFilter> getPlayers();
    boolean getUse_permission();
    String getMessage();
    boolean isStrict();
    boolean isInverted();
}
