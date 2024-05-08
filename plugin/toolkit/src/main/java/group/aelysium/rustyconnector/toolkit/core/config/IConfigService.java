package group.aelysium.rustyconnector.toolkit.core.config;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public interface IConfigService extends Service {
    /**
     * Gets the version that this config service is using.
     */
    int version();

    void put(IYAML config);

    <TConfig extends IYAML> Optional<TConfig> get(ConfigKey key);

    void remove(ConfigKey key);

    record ConfigKey(@NotNull Class<? extends IYAML> type, String name) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConfigKey configKey = (ConfigKey) o;
            return Objects.equals(type, configKey.type) && Objects.equals(name, configKey.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, name);
        }

        public static ConfigKey singleton(Class<? extends IYAML> type) {
            return new ConfigKey(type, null);
        }
    }
}