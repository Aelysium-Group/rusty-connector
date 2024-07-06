package group.aelysium.rustyconnector.common.config;

import java.util.HashMap;
import java.util.Map;

public class MigrationDirections {
    private static final Map<Integer, String> directions = new HashMap<>();

    public static void init() {
        directions.put(1 + 2, "https://wiki.aelysium.group/rusty-connector/docs/updating/update-from-config-v1-to-v2");
        directions.put(2 + 3, "https://wiki.aelysium.group/rusty-connector/docs/updating/update-from-config-v2-to-v3");
        directions.put(3 + 4, "https://wiki.aelysium.group/rusty-connector/docs/updating/update-from-config-v3-to-v4");
        directions.put(4 + 5, "https://wiki.aelysium.group/rusty-connector/docs/updating/update-from-config-v4-to-v5");
    }

    public static String findUpgradeDirections(int from, int to)  {
        String url = directions.get(from + to);
        if(url == null) return "https://wiki.aelysium.group/rusty-connector/docs/updating/";
        return url;
    }
}
