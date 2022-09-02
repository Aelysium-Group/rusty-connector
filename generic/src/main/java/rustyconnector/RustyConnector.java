package rustyconnector;

import rustyconnector.generic.lib.hash.Snowflake;

import java.io.File;
import java.io.InputStream;

public interface RustyConnector {
    RustyConnector instance = null;
    Snowflake snowflakeGenerator = null;

    Long newSnowflake();

    static RustyConnector getInstance() {
        return instance;
    }

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
