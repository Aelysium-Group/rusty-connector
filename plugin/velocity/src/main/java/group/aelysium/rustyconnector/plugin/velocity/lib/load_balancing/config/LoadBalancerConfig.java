package group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.config;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.toolkit.velocity.family.UnavailableProtocol;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.AlgorithmType;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

import java.io.File;
import java.text.ParseException;

public class LoadBalancerConfig extends YAML {
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

    public LoadBalancerConfig(String dataFolder, String balancerName) {
        super(new File(dataFolder, "load_balancers/"+balancerName+".yml"));
    }
    public void register() throws IllegalStateException {
        this.weighted = this.getNode(this.data,"weighted",Boolean.class);
        this.algorithm = AlgorithmType.valueOf(this.getNode(this.data,"algorithm",String.class));

        this.persistence_enabled = this.getNode(this.data,"first-connection.load-balancing.persistence.enabled",Boolean.class);
        this.persistence_attempts = this.getNode(this.data,"first-connection.load-balancing.persistence.attempts",Integer.class);
        if(this.persistence_enabled && this.persistence_attempts <= 0)
            throw new IllegalStateException("Load balancing persistence must allow at least 1 attempt.");
    }
}
