package group.aelysium.rustyconnector.toolkit.velocity.config;

import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import java.util.List;

public interface WhitelistConfig {
    boolean getUse_players();
    List<Object> getPlayers();
    boolean getUse_permission();
    boolean getUse_country();
    List<String> getCountries();
    String getMessage();
    boolean isStrict();
    boolean isInverted();
}
