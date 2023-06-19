package group.aelysium.rustyconnector.core.central;

import java.io.InputStream;

public abstract class PluginAPI<S> {
    /**
     * Gets a resource by name and returns it as a stream.
     * @param filename The name of the resource to get.
     * @return The resource as a stream.
     */
    abstract public InputStream getResourceAsStream(String filename);

    abstract public S getScheduler();

    abstract public PluginLogger getLogger();

    abstract public String getDataFolder();
}
