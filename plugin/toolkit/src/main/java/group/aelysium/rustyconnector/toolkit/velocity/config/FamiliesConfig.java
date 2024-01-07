package group.aelysium.rustyconnector.toolkit.velocity.config;

import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import java.util.List;

public interface FamiliesConfig {
    String rootFamilyName();
    Boolean shouldRootFamilyCatchDisconnectingPlayers();
    List<String> scalarFamilies();
    List<String> staticFamilies();
    List<String> rankedFamilies();
}
