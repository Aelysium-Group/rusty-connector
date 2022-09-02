package rustyconnector.generic.lib.generic.parsing;

import ninja.leaping.configurate.ConfigurationNode;
import org.intellij.lang.annotations.RegExp;
import rustyconnector.RustyConnector;

import java.util.Arrays;

public class YAML {
    public static ConfigurationNode get(ConfigurationNode node, String path) {
        String[] steps = path.split("\\.");

        final ConfigurationNode[] currentNode = {node};
        Arrays.stream(steps).forEach(step -> {
            currentNode[0] = currentNode[0].getNode(step);
        });

        return currentNode[0];
    }
}
