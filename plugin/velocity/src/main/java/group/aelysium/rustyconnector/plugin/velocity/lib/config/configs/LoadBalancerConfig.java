package group.aelysium.rustyconnector.plugin.velocity.lib.config.configs;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.toolkit.core.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.AlgorithmType;

import java.nio.file.Path;

public class LoadBalancerConfig extends YAML implements group.aelysium.rustyconnector.toolkit.velocity.config.LoadBalancerConfig {
    private boolean weighted = false;

    private AlgorithmType algorithm = AlgorithmType.ROUND_ROBIN;
    private boolean persistence_enabled = false;
    private int persistence_attempts = 5;

    public boolean isWeighted() {
        return weighted;
    }

    public AlgorithmType getAlgorithm() {
        return algorithm;
    }

    public boolean isPersistence_enabled() {
        return persistence_enabled;
    }

    public int getPersistence_attempts() {
        return persistence_attempts;
    }

    protected LoadBalancerConfig(Path dataFolder, String target, String name, LangService lang) {
        super(dataFolder, target, name, lang, LangFileMappings.PROXY_LOAD_BALANCER_TEMPLATE);
    }

    @Override
    public IConfigService.ConfigKey key() {
        return new IConfigService.ConfigKey(LoadBalancerConfig.class, name());
    }

    protected void register() throws IllegalStateException {
        this.weighted = IYAML.getValue(this.data,"weighted",Boolean.class);
        this.algorithm = AlgorithmType.valueOf(IYAML.getValue(this.data,"algorithm",String.class));

        this.persistence_enabled = IYAML.getValue(this.data,"persistence.enabled",Boolean.class);
        this.persistence_attempts = IYAML.getValue(this.data,"persistence.attempts",Integer.class);
        if(this.persistence_enabled && this.persistence_attempts <= 0)
            throw new IllegalStateException("Load balancing persistence must allow at least 1 attempt.");
    }

    public static LoadBalancerConfig construct(Path dataFolder, String balancerName, LangService lang) {
        LoadBalancerConfig config = new LoadBalancerConfig(dataFolder, "load_balancers/"+balancerName+".yml", balancerName, lang);
        config.register();
        return config;
    }
}
