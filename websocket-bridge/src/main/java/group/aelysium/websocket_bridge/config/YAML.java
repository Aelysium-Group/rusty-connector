package group.aelysium.websocket_bridge.config;

import group.aelysium.websocket_bridge.WebSocketBridge;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;

public class YAML {
    protected File configPointer;
    protected String template;
    protected ConfigurationNode data;
    public YAML(File configPointer, String template) {
        this.configPointer = configPointer;
        this.template = template;
    }

    public ConfigurationNode getData() { return this.data; }
    public String getName() {
        return this.configPointer.getName();
    }
    protected static ConfigurationNode get(ConfigurationNode node, String path) {
        String[] steps = path.split("\\.");

        final ConfigurationNode[] currentNode = {node};
        Arrays.stream(steps).forEach(step -> currentNode[0] = currentNode[0].node(step));

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
    protected <T> T getNode(ConfigurationNode data, String node, Class<? extends T> type) throws IllegalStateException {
        try {
            T object = YAML.get(data,node).get(type);
            if(object == null) throw new NullPointerException();

            return object;
        } catch (NullPointerException e) {
            throw new IllegalStateException("The node ["+node+"] is missing!");
        } catch (ClassCastException e) {
            throw new IllegalStateException("The node ["+node+"] is of the wrong data type! Make sure you are using the correct type of data!");
        } catch (Exception e) {
            throw new IllegalStateException("Unable to register the node: "+node);
        }
    }

    public ConfigurationNode loadYAML(File file) throws IOException {
        return YamlConfigurationLoader.builder()
                .indent(2)
                .path(file.toPath())
                .build().load();
    }

    /**
     * Generate and then load the yaml file.
     * If it already exists, just load it.
     * @return `true` If the file successfully loaded. `false` otherwise.
     */
    public boolean generate() {
        if (!this.configPointer.exists()) {
            File parent = this.configPointer.getParentFile();
            if (!parent.exists())
                parent.mkdirs();

            InputStream templateStream = WebSocketBridge.instance().getResourceAsStream(this.template);
            if (templateStream == null) {
                System.out.println("!!!!! Unable to setup "+this.configPointer.getName()+". This config has no template !!!!!");
                return false;
            }

            try {
                Files.copy(templateStream, this.configPointer.toPath());
            } catch (IOException e) {
                System.out.println("!!!!! Unable to setup "+this.configPointer.getName()+" !!!!!");
                return false;
            }
        }

        try {
            this.data = this.loadYAML(this.configPointer);
            if(this.data == null) return false;
            System.out.println("Finished registering "+this.configPointer.getName());
            return true;
        } catch (Exception e) {
            System.out.println("Failed to register: "+this.configPointer.getName());
            return false;
        }
    }
}
