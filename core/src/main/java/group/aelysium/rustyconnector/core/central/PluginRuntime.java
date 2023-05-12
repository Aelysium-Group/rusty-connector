package group.aelysium.rustyconnector.core.central;

import group.aelysium.rustyconnector.core.lib.database.redis.RedisSubscriber;

public interface PluginRuntime {
    RedisSubscriber redis = null;

    static PluginAPI<?> getAPI() {
        return null;
    }

    static PluginLifecycle getLifecycle() {
        return null;
    }
}
