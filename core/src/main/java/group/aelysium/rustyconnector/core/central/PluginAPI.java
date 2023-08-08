package group.aelysium.rustyconnector.core.central;

import java.io.InputStream;

public abstract class PluginAPI<S> {
    /**
     * Gets a resource by name and returns it as a stream.
     * @param filename The name of the resource to get.
     * @return The resource as a stream.
     */
    abstract public InputStream resourceAsStream(String filename);

    abstract public S scheduler();

    abstract public PluginLogger logger();

    abstract public String dataFolder();

    abstract public String version();
}
