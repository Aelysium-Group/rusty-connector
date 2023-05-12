package group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing;

import group.aelysium.rustyconnector.core.lib.util.WeightOnlyQuickSort;

public class RoundRobin extends LoadBalancer {
    @Override
    public String toString() {
        return "LoadBalancer (RoundRobin): "+this.size()+" items";
    }

    @Override
    public void completeSort() {
        if(this.isWeighted()) WeightOnlyQuickSort.sort(this.items);
    }

    @Override
    public void singleSort() {}
}
