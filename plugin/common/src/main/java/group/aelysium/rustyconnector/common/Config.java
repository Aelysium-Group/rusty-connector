package group.aelysium.rustyconnector.common;

import org.spongepowered.configurate.ConfigurationNode;

public abstract class Config {
    public abstract ConfigurationNode getData();

    /**
     * Load the config. If it doesn't exist, create it.
     */
    public abstract boolean generate();

    /**
     * Reload the config at the defined path.
     */
    public abstract void reload();

    /**
     * Save config data to the config at this location. If the config doesn't already exist it will be created.
     * @param data The data to be saved to the config.
     */
    public abstract void save(String data);
}
