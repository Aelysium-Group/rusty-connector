package group.aelysium.rustyconnector.toolkit.velocity.config;

import java.util.List;

public interface FamiliesConfig {
    String rootFamilyName();
    Boolean shouldRootFamilyCatchDisconnectingPlayers();
    List<String> scalarFamilies();
    List<String> staticFamilies();
    List<String> rankedFamilies();
}
