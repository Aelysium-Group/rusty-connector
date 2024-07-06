package group.aelysium.rustyconnector.toolkit.common.lang;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public interface IConfig<Settings> {
    /**
     * Returns the compiled settings built by this config.
     */
    Settings settings();

    static InputStream getResource(String path) {
        return IConfig.class.getClassLoader().getResourceAsStream(path);
    }

    static ConfigurationNode get(ConfigurationNode node, String path) {
        String[] steps = path.split("\\.");

        final ConfigurationNode[] currentNode = {node};
        Arrays.stream(steps).forEach(step -> {
            currentNode[0] = currentNode[0].node(step);
        });

        if(currentNode[0] == null) throw new IllegalArgumentException("The called YAML node `"+path+"` was null.");

        return currentNode[0];
    }

    /**
     * Retrieve data from a specific configuration node.
     * @param data The configuration data to search for a specific node.
     * @param node The node to search for.
     * @param type The type to convert the retrieved data to.
     * @return Data with a type matching `type`
     * @throws IllegalStateException If there was an issue while retrieving the data or converting it to `type`.
     */
    static <T> T getValue(ConfigurationNode data, String node, Class<? extends T> type) throws IllegalStateException {
        try {
            Object objectData = get(data,node).get(type);
            if(objectData == null) throw new NullPointerException();

            return type.cast(objectData);
        } catch (NullPointerException e) {
            throw new IllegalStateException("The node ["+node+"] is missing!");
        } catch (ClassCastException e) {
            throw new IllegalStateException("The node ["+node+"] is of the wrong data type! Make sure you are using the correct type of data!");
        } catch (Exception e) {
            throw new IllegalStateException("Unable to registerProxy the node: "+node);
        }
    }

    static ConfigurationNode loadYAML(File file) throws IOException {
        return YamlConfigurationLoader.builder()
                .indent(2)
                .path(file.toPath())
                .build().load();
    }

    void print(File location);
}
