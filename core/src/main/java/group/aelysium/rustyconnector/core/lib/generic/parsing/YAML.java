package group.aelysium.rustyconnector.core.lib.generic.parsing;

import ninja.leaping.configurate.ConfigurationNode;

import java.util.Arrays;

public class YAML {
    public static ConfigurationNode get(ConfigurationNode node, String path) {
        String[] steps = path.split("\\.");

        final ConfigurationNode[] currentNode = {node};
        Arrays.stream(steps).forEach(step -> {
            currentNode[0] = currentNode[0].getNode(step);
        });

        if(currentNode[0] == null) throw new IllegalArgumentException("The called YAML node `"+path+"` was null.");

        return currentNode[0];
    }
}
