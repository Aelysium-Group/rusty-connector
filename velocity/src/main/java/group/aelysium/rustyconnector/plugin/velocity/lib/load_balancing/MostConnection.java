package group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing;

import group.aelysium.rustyconnector.core.lib.algorithm.QuickSort;
import group.aelysium.rustyconnector.core.lib.algorithm.SingleSort;
import group.aelysium.rustyconnector.core.lib.algorithm.WeightedQuickSort;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PlayerServer;

import java.util.Collections;

public class MostConnection extends LeastConnection {
    @Override
    public void completeSort() {
        this.index = 0;
        if(this.isWeighted()) WeightedQuickSort.sort(this.items);
        else {
            QuickSort.sort(this.items);
            Collections.reverse(this.items);
        }
    }

    @Override
    public String toString() {
        return "LoadBalancer (MostConnection): "+this.size()+" items";
    }
}
