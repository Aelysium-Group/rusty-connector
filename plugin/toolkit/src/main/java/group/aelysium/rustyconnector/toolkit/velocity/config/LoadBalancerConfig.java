package group.aelysium.rustyconnector.toolkit.velocity.config;

import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.AlgorithmType;

public interface LoadBalancerConfig {
    boolean isWeighted();
    AlgorithmType getAlgorithm();
    boolean isPersistence_enabled();
    int getPersistence_attempts();
}
