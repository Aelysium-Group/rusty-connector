package group.aelysium.rustyconnector.core.central;

import group.aelysium.rustyconnector.core.lib.database.Redis;

public interface PluginRuntime<API extends PluginAPI<?>> {
    Redis redis = null;

    static PluginAPI<?> getAPI() {
        return null;
    }

    static PluginLifecycle getLifecycle() {
        return null;
    }
}
