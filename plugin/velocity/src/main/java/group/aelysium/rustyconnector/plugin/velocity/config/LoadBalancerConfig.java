package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.common.config.Config;
import group.aelysium.rustyconnector.common.lang.LangService;
import group.aelysium.rustyconnector.toolkit.common.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.common.lang.IConfig;
import group.aelysium.rustyconnector.toolkit.common.lang.LangFileMappings;
import group.aelysium.rustyconnector.toolkit.proxy.family.load_balancing.AlgorithmType;

import java.nio.file.Path;

public class LoadBalancerConfig extends Config implements group.aelysium.rustyconnector.toolkit.proxy.config.LoadBalancerConfig {
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
        this.weighted = IConfig.getValue(this.data,"weighted",Boolean.class);
        this.algorithm = AlgorithmType.valueOf(IConfig.getValue(this.data,"algorithm",String.class));

        this.persistence_enabled = IConfig.getValue(this.data,"persistence.enabled",Boolean.class);
        this.persistence_attempts = IConfig.getValue(this.data,"persistence.attempts",Integer.class);
        if(this.persistence_enabled && this.persistence_attempts <= 0)
            throw new IllegalStateException("Load balancing persistence must allow at least 1 attempt.");
    }

    public static LoadBalancerConfig construct(Path dataFolder, String balancerName, LangService lang) {
        LoadBalancerConfig config = new LoadBalancerConfig(dataFolder, "load_balancers/"+balancerName+".yml", balancerName, lang);
        config.register();
        return config;
    }
}
