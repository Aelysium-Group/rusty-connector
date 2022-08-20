package rustyconnector;

import java.io.File;
import java.io.InputStream;

public interface RustyConnector {
    /**
     * Get's the data folder containing the configuration files for this plugin.
     * @return The data folder
     */
    public File getDataFolder();

    /**
     *
     */
    public InputStream getResourceAsStream(String filename);

    /**
     * Load the configs for this plugin
     */
    public boolean loadConfigs();

    public Logger logger();
}
