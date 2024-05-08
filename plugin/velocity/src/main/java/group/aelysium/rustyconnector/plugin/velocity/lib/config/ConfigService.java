package group.aelysium.rustyconnector.plugin.velocity.lib.config;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.configs.*;
import group.aelysium.rustyconnector.toolkit.core.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import group.aelysium.rustyconnector.toolkit.velocity.config.IProxyConfigService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConfigService implements IProxyConfigService {
    protected int version;
    protected Map<IConfigService.ConfigKey, YAML> configs = new HashMap<>();

    public ConfigService(int version) {
        this.version = version;
    }

    @Override
    public int version() {
        return this.version;
    }
    @Override
    public void put(IYAML config) {
        this.configs.put(config.key(), (YAML) config);
    }
    @Override
    public <TConfig extends IYAML> Optional<TConfig> get(ConfigKey key) {
        TConfig config = (TConfig) this.configs.get(key);
        if(config == null) return Optional.empty();
        return Optional.of(config);
    }
    @Override
    public void remove(ConfigKey key) {
        this.configs.remove(key);
    }

    @Override
    public DefaultConfig root() {
        return (DefaultConfig) get(ConfigKey.singleton(DefaultConfig.class)).orElseThrow();
    }

    @Override
    public FamiliesConfig families() {
        return (FamiliesConfig) get(ConfigKey.singleton(FamiliesConfig.class)).orElseThrow();
    }

    @Override
    public FriendsConfig friends() {
        return (FriendsConfig) get(ConfigKey.singleton(FriendsConfig.class)).orElseThrow();
    }

    public DataTransitConfig dataTransit() {
        return (DataTransitConfig) get(ConfigKey.singleton(DataTransitConfig.class)).orElseThrow();
    }

    @Override
    public DynamicTeleportConfig dynamicTeleport() {
        return (DynamicTeleportConfig) get(ConfigKey.singleton(DynamicTeleportConfig.class)).orElseThrow();
    }

    @Override
    public LoggerConfig logger() {
        return (LoggerConfig) get(ConfigKey.singleton(LoggerConfig.class)).orElseThrow();
    }

    @Override
    public PartyConfig party() {
        return (PartyConfig) get(ConfigKey.singleton(PartyConfig.class)).orElseThrow();
    }

    @Override
    public Optional<MagicMCLoaderConfig> magicMCLoaderConfig(String name) {
        return get(new ConfigKey(MagicMCLoaderConfig.class, name));
    }

    @Override
    public Optional<LoadBalancerConfig> loadBalancer(String name) {
        return get(new ConfigKey(LoadBalancerConfig.class, name));
    }

    @Override
    public Optional<MatchmakerConfig> matchmaker(String name) {
        return get(new ConfigKey(MatchmakerConfig.class, name));
    }

    @Override
    public Optional<WhitelistConfig> whitelist(String name) {
        return get(new ConfigKey(WhitelistConfig.class, name));
    }

    @Override
    public Optional<ScalarFamilyConfig> scalarFamily(String id) {
        return get(new ConfigKey(ScalarFamilyConfig.class, id));
    }

    @Override
    public Optional<StaticFamilyConfig> staticFamily(String id) {
        return get(new ConfigKey(StaticFamilyConfig.class, id));
    }

    @Override
    public Optional<RankedFamilyConfig> rankedFamily(String id) {
        return get(new ConfigKey(RankedFamilyConfig.class, id));
    }

    @Override
    public void kill() {
        this.configs.clear();
    }
}
