package group.aelysium.rustyconnector.core;

import group.aelysium.rustyconnector.core.lib.generic.database.Redis;
import group.aelysium.rustyconnector.core.lib.generic.util.logger.Logger;

import java.io.File;
import java.io.InputStream;

public interface RustyConnector {
    RustyConnector instance = null;
    Redis redis = null;

    static RustyConnector getInstance() {
        return instance;
    }

    /**
     * Get's the data folder containing the configuration files for this plugin.
     * @return The data folder
     */
    File getDataFolder();

    /**
     *
     */
    InputStream getResourceAsStream(String filename);

    /**
     * Load the configs for this plugin
     */
    boolean loadConfigs();

    boolean loadCommands();

    /**
     * Reload the plugin
     */
    void reload();

    Logger logger();
}
