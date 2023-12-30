package group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing;

import group.aelysium.rustyconnector.core.lib.algorithm.WeightOnlyQuickSort;

public class RoundRobin extends LoadBalancer {
    public RoundRobin(Settings settings) {
        super(settings);
    }

    @Override
    public String toString() {
        return "LoadBalancer (RoundRobin): "+this.size()+" items";
    }

    @Override
    public void completeSort() {
        if(this.weighted()) WeightOnlyQuickSort.sort(this.unlockedServers);
    }

    @Override
    public void singleSort() {}
}
