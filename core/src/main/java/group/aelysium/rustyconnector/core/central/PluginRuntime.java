package group.aelysium.rustyconnector.core.central;

import group.aelysium.rustyconnector.core.lib.database.RedisIO;

public interface PluginRuntime {
    RedisIO redis = null;

    static PluginAPI<?> getAPI() {
        return null;
    }

    static PluginLifecycle getLifecycle() {
        return null;
    }
}
